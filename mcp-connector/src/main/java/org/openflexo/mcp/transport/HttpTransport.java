package org.openflexo.mcp.transport;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openflexo.mcp.protocol.MCPError;
import org.openflexo.mcp.protocol.MCPRequest;
import org.openflexo.mcp.protocol.MCPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class HttpTransport implements MCPTransport {

    private static final Logger logger = LoggerFactory.getLogger(HttpTransport.class);
    private static final Gson gson = new Gson();

    private final String endpoint;
    private String sessionId;
    private volatile boolean connected = false;
    private final BlockingQueue<MCPResponse> responseQueue = new LinkedBlockingQueue<>();

    public HttpTransport(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void connect() throws IOException {
        connected = true;
        logger.info("HttpTransport ready for endpoint: {}", endpoint);
    }

    @Override
    public void disconnect() throws IOException {
        connected = false;
        sessionId = null;
        responseQueue.clear();
        logger.info("HttpTransport disconnected from: {}", endpoint);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String getTransportType() {
        return "http";
    }

    @Override
    public void send(MCPRequest request) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to MCP server");
        }

        String body = gson.toJson(request.toJson());
        logger.info("Sending HTTP POST to {}: {}", endpoint, body);

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json, text/event-stream");
        if (sessionId != null) {
            conn.setRequestProperty("Mcp-Session-Id", sessionId);
        }
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(60000);

        OutputStream os = conn.getOutputStream();
        os.write(body.getBytes("UTF-8"));
        os.flush();
        os.close();

        int statusCode = conn.getResponseCode();
        logger.info("HTTP response status: {}", statusCode);

         String newSessionId = conn.getHeaderField("Mcp-Session-Id");
        if (newSessionId != null && !newSessionId.isEmpty()) {
            this.sessionId = newSessionId;
            logger.info("Session ID received: {}", sessionId);
        }

         if (statusCode == 202) {
            logger.debug("Notification acknowledged (202), no response body");
            return;
        }

        String contentType = conn.getContentType();
        if (contentType != null && contentType.contains("text/event-stream")) {
            parseSSEResponse(conn);
        } else {
            parsePlainJsonResponse(conn);
        }
    }

    private void parsePlainJsonResponse(HttpURLConnection conn) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();

        String responseBody = sb.toString().trim();
        if (responseBody.isEmpty()) {
            logger.warn("Empty response body from server");
            return;
        }

        logger.info("Received HTTP response: {}", responseBody);
        try {
            JsonObject json = new JsonParser().parse(responseBody).getAsJsonObject();
            MCPResponse response = parseResponse(json);
            if (response != null) {
                responseQueue.put(response);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse HTTP response: {} - {}", responseBody, e.getMessage());
        }
    }

    private void parseSSEResponse(HttpURLConnection conn) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("data: ")) {
                String data = line.substring(6).trim();
                if (!data.isEmpty() && !data.equals("[DONE]")) {
                    logger.info("SSE event: {}", data);
                    try {
                        JsonObject json = new JsonParser().parse(data).getAsJsonObject();
                        MCPResponse response = parseResponse(json);
                        if (response != null) {
                            responseQueue.put(response);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse SSE data: {} - {}", data, e.getMessage());
                    }
                }
            }
        }
        reader.close();
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

    @Override
    public MCPResponse receive() throws IOException {
        try {
            return responseQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for HTTP response", e);
        }
    }

    @Override
    public MCPResponse receive(long timeoutMs) throws IOException {
        try {
            MCPResponse response = responseQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (response == null) {
                throw new IOException("Timeout waiting for HTTP response after " + timeoutMs + "ms");
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for HTTP response", e);
        }
    }
}