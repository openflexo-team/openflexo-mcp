package org.openflexo.mcp.transport;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openflexo.mcp.protocol.MCPError;
import org.openflexo.mcp.protocol.MCPRequest;
import org.openflexo.mcp.protocol.MCPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;

public class StdioTransport implements MCPTransport {

    private static final Logger logger = LoggerFactory.getLogger(StdioTransport.class);
    private static final Gson gson = new Gson();
    private final Map<String, String> envVars;

    private final String[] command;
    private Process process;
    private BufferedWriter stdin;
    private BufferedReader stdout;
    private BufferedReader stderr;

    private final BlockingQueue<MCPResponse> responseQueue = new LinkedBlockingQueue<>();
    private Thread readerThread;
    private volatile boolean connected = false;

    public StdioTransport(String... command) {
        this(java.util.Collections.emptyMap(), command);
    }
    public StdioTransport(Map<String, String> envVars, String... command) {
        this.envVars = envVars != null ? envVars : java.util.Collections.emptyMap();
        this.command = resolveCommand(command);
    }

    /**
     * Resolves the command for the current OS.
     *
     * <p>On Windows, commands like {@code npx}, {@code python}, {@code uvx} are
     * typically batch scripts ({@code .cmd} / {@code .bat}) installed on the PATH.
     * Java's {@link ProcessBuilder} cannot launch these directly — it requires a
     * real executable. Prefixing with {@code cmd /c} delegates to the Windows
     * command interpreter, which handles script extensions transparently.</p>
     *
     * <p>On Unix/macOS the command array is returned unchanged.</p>
     *
     * @param command original command array from the caller
     * @return platform-appropriate command array
     */
    private static String[] resolveCommand(String[] command) {
        if (isWindows()) {
            String[] windowsCommand = new String[command.length + 2];
            windowsCommand[0] = "cmd";
            windowsCommand[1] = "/c";
            System.arraycopy(command, 0, windowsCommand, 2, command.length);
            logger.debug("Windows detected — wrapping command with cmd /c: {}",
                    String.join(" ", windowsCommand));
            return windowsCommand;
        }
        return command;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    @Override
    public void connect() throws IOException {
        if (connected) {
            return;
        }

        logger.info("Starting MCP server: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        if (!envVars.isEmpty()) {
            pb.environment().putAll(envVars);
        }
        try {
            process = pb.start();
            stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
            stdout = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            stderr = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

             if (!process.isAlive()) {
                throw new IOException("Process exited immediately after start");
            }

             connected = true;

            startReaderThread();
            startErrorReaderThread();

             logger.info("Waiting for server to initialize...");
            try {
                Thread.sleep(2000); //Todo - replace with a more robust initialization handshake or timeout mechanism
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Check if still alive
            if (!process.isAlive()) {
                throw new IOException("Process died during initialization");
            }

        } catch (IOException e) {
            logger.error("Failed to start MCP server", e);
            cleanup();
            throw e;
        }
    }

    @Override
    public void send(MCPRequest request) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to MCP server");
        }

        String jsonMessage = gson.toJson(request.toJson());
        logger.info("Sending: {}", jsonMessage);

        stdin.write(jsonMessage);
        stdin.newLine();
        stdin.flush();

        logger.debug("Message sent and flushed");
    }

    @Override
    public MCPResponse receive() throws IOException {
        try {
            MCPResponse response = responseQueue.take();
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for response", e);
        }
    }

    @Override
    public MCPResponse receive(long timeoutMs) throws IOException {
        try {
            MCPResponse response = responseQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (response == null) {
                throw new IOException("Timeout waiting for response");
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for response", e);
        }
    }

    @Override
    public boolean isConnected() {
        return connected && process != null && process.isAlive();
    }

    @Override
    public void disconnect() throws IOException {
        logger.info("Disconnecting from MCP server");
        connected = false;
        cleanup();
    }

    @Override
    public String getTransportType() {
        return "stdio";
    }

    private void startReaderThread() {
        readerThread = new Thread(() -> {
            logger.info("Reader thread started, waiting for stdout data...");
            try {
                String line;
                int nullCount = 0;

                while (connected) {
                    try {
                        line = stdout.readLine();

                        if (line == null) {
                            nullCount++;
                            logger.warn("Got null from readLine() (attempt {}/3)", nullCount);
                            if (nullCount >= 3) {
                                logger.error("Stream ended, stopping reader thread");
                                break;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            continue;
                        }

                        nullCount = 0;
                        line = line.trim();
                        if (line.isEmpty()) continue;

                        logger.info("Received line: {}", line);

                        try {
                            JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                            MCPResponse response = parseResponse(json);
                            if (response != null) {
                                responseQueue.put(response);
                            }
                        } catch (Exception e) {
                            logger.warn("Failed to parse response line: {} - {}", line, e.getMessage());
                        }

                    } catch (IOException e) {
                        if (connected) {
                            logger.error("Error reading from stdout", e);
                        }
                        break;
                    }
                }
            } finally {
                logger.info("Reader thread exiting (connected={}, isAlive={})",
                        connected, process != null && process.isAlive());
            }
        }, "mcp-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void startErrorReaderThread() {
        Thread errorThread = new Thread(() -> {
            try {
                String line;
                while ((line = stderr.readLine()) != null) {
                    logger.info("MCP server stderr: {}", line);
                }
            } catch (IOException e) {
                if (connected) {
                    logger.warn("Error reading stderr: {}", e.getMessage());
                }
            }
        }, "mcp-stderr-reader");
        errorThread.setDaemon(true);
        errorThread.start();
    }
    private MCPResponse parseResponse(JsonObject json) {
        String id = json.has("id") ? json.get("id").getAsString() : null;

        if (json.has("result")) {
            return new MCPResponse(id, json.get("result"));
        } else if (json.has("error")) {
            JsonObject errorObj = json.getAsJsonObject("error");
            int code = errorObj.get("code").getAsInt();
            String message = errorObj.get("message").getAsString();
            MCPError error = new MCPError(code, message);
            if (errorObj.has("data")) {
                error.setData(errorObj.get("data"));
            }
            return new MCPResponse(id, error);
        } else {
            MCPError error = MCPError.invalidRequest("Response has neither result nor error");
            return new MCPResponse(id, error);
        }
    }


    private void cleanup() {
        if (readerThread != null) {
            readerThread.interrupt();
            readerThread = null;
        }
        if (stdin != null) {
            try { stdin.close(); } catch (IOException ignored) {}
            stdin = null;
        }
        if (stdout != null) {
            try { stdout.close(); } catch (IOException ignored) {}
            stdout = null;
        }
        if (stderr != null) {
            try { stderr.close(); } catch (IOException ignored) {}
            stderr = null;
        }
        if (process != null) {
            process.destroyForcibly();
            process = null;
        }
        responseQueue.clear();
    }
}