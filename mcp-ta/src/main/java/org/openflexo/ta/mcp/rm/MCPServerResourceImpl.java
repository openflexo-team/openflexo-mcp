package org.openflexo.ta.mcp.rm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FileWritingLock;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.StreamIODelegate;
import org.openflexo.ta.mcp.model.MCPModelFactory;
import org.openflexo.ta.mcp.model.MCPServer;
import org.openflexo.ta.mcp.model.MCPTool;
import org.openflexo.toolbox.FileUtils;


public abstract class MCPServerResourceImpl extends PamelaResourceImpl<MCPServer, MCPModelFactory>
        implements MCPServerResource {

    private static final Logger logger = Logger.getLogger(MCPServerResourceImpl.class.getPackage().getName());

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    MCPServerResourceImpl() {
        super();
    }

    @Override
    public MCPServer getMCPServer() {
        try {
            return getResourceData();
        } catch (Exception e) {
            logger.warning("Error getting MCP server data: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Class<MCPServer> getResourceDataClass() {
        return MCPServer.class;
    }


    @Override
    protected MCPServer performLoad() throws IOException, Exception {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Loading MCP server resource: " + getURI());
        }

        if (getFlexoIOStreamDelegate() == null) {
            throw new IOFlexoException("Cannot load MCP server with this IO delegate: " + getIODelegate());
        }

        notifyResourceWillLoad();

        MCPServer server;
        try {
            server = loadFromJson(getFlexoIOStreamDelegate());
        } catch (Exception e) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.severe("Exception loading MCP server config: " + e.getMessage());
            }
            throw e;
        }

        if (server == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Cannot retrieve resource data from: " + getIODelegate().toString());
            }
             server = getFactory().makeMCPServer();
        }

         server.setResource(this);
        notifyResourceLoaded();

        if (logger.isLoggable(Level.INFO)) {
            logger.info("Successfully loaded MCP server: " + server.getName());
        }

        return server;
    }


    @Override
    protected void performSave(boolean clearIsModified) throws SaveResourceException {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Saving MCP server resource: " + this);
        }

        if (getFlexoIOStreamDelegate() == null) {
            throw new SaveResourceException(getIODelegate(), new IllegalStateException("IO delegate is null"));
        }

        FileWritingLock lock = getFlexoIOStreamDelegate().willWriteOnDisk();

        try {
            if (getFlexoIOStreamDelegate() instanceof FileIODelegate) {
                saveToFile((FileIODelegate) getFlexoIOStreamDelegate());
            } else {
                writeJson(getOutputStream());
            }

            getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);

            if (clearIsModified) {
                notifyResourceStatusChanged();
            }

        } catch (SaveResourceException e) {
            getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
            throw e;
        } catch (Exception e) {
            getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
            if (logger.isLoggable(Level.SEVERE)) {
                logger.severe("Unexpected error saving MCP server config: " + e.getMessage());
            }
            throw new SaveResourceException(getIODelegate(), e);
        }
    }

    @Override
    public void unloadResourceData(boolean deleteResourceData) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Unloading resource data: " + getURI());
        }
        super.unloadResourceData(deleteResourceData);
    }

    @Override
    public String toString() {
        try {
             String uri = getURI();
            MCPServer data = getLoadedResourceData();
            if (data != null && data.getName() != null) {
                return "MCPServerResource: " + data.getName() + " (" + uri + ")";
            }
            return "MCPServerResource: " + (uri != null ? uri : "unknown");
        } catch (Exception e) {
             return super.toString();
        }
    }


    private <I> MCPServer loadFromJson(StreamIODelegate<I> ioDelegate) {
        if (ioDelegate == null) {
            return null;
        }

        try {
            if (!ioDelegate.exists()) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("File doesn't exist, creating empty MCP server");
                }
                return getFactory().makeMCPServer();
            }

            try (InputStream is = ioDelegate.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                com.google.gson.JsonElement element = JsonParser.parseReader(reader);

                 if (element == null || element.isJsonNull()) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("Empty or null JSON content, creating empty MCP server");
                    }
                    return getFactory().makeMCPServer();
                }

                JsonObject json = element.getAsJsonObject();

                MCPServer server = getFactory().makeMCPServer();

                if (json.has("name")) {
                    server.setName(json.get("name").getAsString());
                }
                if (json.has("command")) {
                    server.setCommand(json.get("command").getAsString());
                }
                if (json.has("args") && json.get("args").isJsonArray()) {
                    for (com.google.gson.JsonElement arg : json.getAsJsonArray("args")) {
                        server.addToArgs(arg.getAsString());
                    }
                }
                if (json.has("tools") && json.get("tools").isJsonArray()) {
                    for (com.google.gson.JsonElement toolEl : json.getAsJsonArray("tools")) {
                        JsonObject toolObj = toolEl.getAsJsonObject();
                        String name = toolObj.has("name") ? toolObj.get("name").getAsString() : "unknown";
                        String desc = toolObj.has("description") ? toolObj.get("description").getAsString() : "";
                        MCPTool tool = getFactory().makeMCPTool(name, desc);
                        tool.setServer(server);
                        server.addToTools(tool);
                    }
                }

                return server;
            }

        } catch (Exception e) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.severe("Error loading JSON: " + e.getMessage());
            }
            e.printStackTrace();
             if (logger.isLoggable(Level.INFO)) {
                logger.info("Returning empty MCP server as fallback after JSON error");
            }
            return getFactory().makeMCPServer();
        }
    }

    private void writeJson(OutputStream out) throws SaveResourceException {
        MCPServer server = getMCPServer();
        if (server == null) {
            throw new SaveResourceException(getIODelegate(), new IllegalStateException("Server is null"));
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            JsonObject json = new JsonObject();

            json.addProperty("name", server.getName() != null ? server.getName() : "");
            json.addProperty("command", server.getCommand() != null ? server.getCommand() : "");

            JsonArray argsArray = new JsonArray();
            if (server.getArgs() != null) {
                for (String arg : server.getArgs()) {
                    argsArray.add(arg);
                }
            }
            json.add("args", argsArray);

            JsonArray toolsArray = new JsonArray();
            if (server.getTools() != null) {
                for (MCPTool tool : server.getTools()) {
                    JsonObject toolObj = new JsonObject();
                    toolObj.addProperty("name", tool.getName());
                    toolObj.addProperty("description", tool.getDescription());
                    toolsArray.add(toolObj);
                }
            }
            json.add("tools", toolsArray);

            writer.write(GSON.toJson(json));
            writer.flush();

            if (logger.isLoggable(Level.INFO)) {
                logger.info("Wrote MCP server config: " + server.getName());
            }

        } catch (IOException e) {
            throw new SaveResourceException(getIODelegate(), e);
        }
    }

    private void saveToFile(FileIODelegate fileDelegate) throws SaveResourceException {
        File temporaryFile = null;
        try {
            File fileToSave = fileDelegate.getFile();
            makeLocalCopy(fileToSave);

            temporaryFile = fileDelegate.createTemporaryArtefact(".mcp");
            try (FileOutputStream fos = new FileOutputStream(temporaryFile)) {
                writeJson(fos);
            }

            FileUtils.rename(temporaryFile, fileToSave);

        } catch (IOException e) {
            if (temporaryFile != null && temporaryFile.exists()) {
                temporaryFile.delete();
            }
            throw new SaveResourceException(getIODelegate(), e);
        }
    }
}