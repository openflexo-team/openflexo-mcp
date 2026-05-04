package org.openflexo.mcp.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openflexo.mcp.model.Prompt;
import org.openflexo.mcp.model.Resource;
import org.openflexo.mcp.model.ServerCapabilities;
import org.openflexo.mcp.model.Tool;
import org.openflexo.mcp.protocol.MCPRequest;
import org.openflexo.mcp.protocol.MCPResponse;
import org.openflexo.mcp.transport.MCPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MCPClientImpl implements MCPClient {

    private static final Logger logger = LoggerFactory.getLogger(MCPClientImpl.class);

    private final MCPTransport transport;
    private final AtomicInteger requestIdCounter = new AtomicInteger(0);
    private ServerCapabilities capabilities;
    private boolean initialized = false;

    public MCPClientImpl(MCPTransport transport) {
        this.transport = transport;
        this.capabilities = new ServerCapabilities();
    }

    @Override
    public void connect() throws IOException {
        transport.connect();
        logger.info("Connected to MCP server via {}", transport.getTransportType());
    }

    @Override
    public void disconnect() throws IOException {
        transport.disconnect();
        initialized = false;
        logger.info("Disconnected from MCP server");
    }

    @Override
    public boolean isConnected() {
        return transport.isConnected();
    }

    @Override
    public void initialize(String clientName, String clientVersion) throws IOException {
        JsonObject params = new JsonObject();
        params.addProperty("protocolVersion", "2024-11-05");

        JsonObject clientInfo = new JsonObject();
        clientInfo.addProperty("name", clientName);
        clientInfo.addProperty("version", clientVersion);
        params.add("clientInfo", clientInfo);

        JsonObject capabilities = new JsonObject();
        params.add("capabilities", capabilities);

        MCPResponse response = sendRequest("initialize", params);

        if (!response.isSuccess()) {
            throw new IOException("Initialization failed: " + response.getError().getMessage());
        }

        parseServerCapabilities(response.getResult().getAsJsonObject());
        initialized = true;

        sendNotification("notifications/initialized");// todoNotify server that client is ready to receive notifications

        logger.info("Initialized MCP client. Server capabilities: {}", this.capabilities);
    }

    @Override
    public ServerCapabilities getServerCapabilities() {
        return capabilities;
    }

    @Override
    public List<Tool> listTools() throws IOException {
        ensureInitialized();

        MCPResponse response = sendRequest("tools/list", null);

        if (!response.isSuccess()) {
            throw new IOException("Failed to list tools: " + response.getError().getMessage());
        }

        List<Tool> tools = new ArrayList<>();
        JsonObject result = response.getResult().getAsJsonObject();
        JsonArray toolsArray = result.getAsJsonArray("tools");

        for (JsonElement elem : toolsArray) {
            JsonObject toolObj = elem.getAsJsonObject();
            String name = toolObj.get("name").getAsString();
            String description = toolObj.has("description") ? toolObj.get("description").getAsString() : "";
            JsonObject inputSchema = toolObj.has("inputSchema") ? toolObj.getAsJsonObject("inputSchema") : new JsonObject();

            tools.add(new Tool(name, description, inputSchema));
        }

        return tools;
    }

    @Override
    public JsonElement callTool(String toolName, JsonObject arguments) throws IOException {
        ensureInitialized();

        JsonObject params = new JsonObject();
        params.addProperty("name", toolName);
        if (arguments != null) {
            params.add("arguments", arguments);
        }

        MCPResponse response = sendRequest("tools/call", params);

        if (!response.isSuccess()) {
            throw new IOException("Tool call failed: " + response.getError().getMessage());
        }

        return response.getResult();
    }

    @Override
    public List<Resource> listResources() throws IOException {
        ensureInitialized();

        MCPResponse response = sendRequest("resources/list", null);

        if (!response.isSuccess()) {
            throw new IOException("Failed to list resources: " + response.getError().getMessage());
        }

        List<Resource> resources = new ArrayList<>();
        JsonObject result = response.getResult().getAsJsonObject();
        JsonArray resourcesArray = result.getAsJsonArray("resources");

        for (JsonElement elem : resourcesArray) {
            JsonObject resObj = elem.getAsJsonObject();
            String uri = resObj.get("uri").getAsString();
            String name = resObj.has("name") ? resObj.get("name").getAsString() : uri;
            String description = resObj.has("description") ? resObj.get("description").getAsString() : "";
            String mimeType = resObj.has("mimeType") ? resObj.get("mimeType").getAsString() : "";

            resources.add(new Resource(uri, name, description, mimeType));
        }

        return resources;
    }

    @Override
    public JsonElement readResource(String uri) throws IOException {
        ensureInitialized();

        JsonObject params = new JsonObject();
        params.addProperty("uri", uri);

        MCPResponse response = sendRequest("resources/read", params);

        if (!response.isSuccess()) {
            throw new IOException("Failed to read resource: " + response.getError().getMessage());
        }

        return response.getResult();
    }

    @Override
    public List<Prompt> listPrompts() throws IOException {
        ensureInitialized();

        MCPResponse response = sendRequest("prompts/list", null);

        if (!response.isSuccess()) {
            throw new IOException("Failed to list prompts: " + response.getError().getMessage());
        }

        List<Prompt> prompts = new ArrayList<>();
        JsonObject result = response.getResult().getAsJsonObject();
        JsonArray promptsArray = result.getAsJsonArray("prompts");

        for (JsonElement elem : promptsArray) {
            JsonObject promptObj = elem.getAsJsonObject();
            String name = promptObj.get("name").getAsString();
            String description = promptObj.has("description") ? promptObj.get("description").getAsString() : "";
            JsonObject arguments = promptObj.has("arguments") ? promptObj.getAsJsonObject("arguments") : null;

            prompts.add(new Prompt(name, description, arguments));
        }

        return prompts;
    }

    @Override
    public JsonElement getPrompt(String promptName, JsonObject arguments) throws IOException {
        ensureInitialized();

        JsonObject params = new JsonObject();
        params.addProperty("name", promptName);
        if (arguments != null) {
            params.add("arguments", arguments);
        }

        MCPResponse response = sendRequest("prompts/get", params);

        if (!response.isSuccess()) {
            throw new IOException("Failed to get prompt: " + response.getError().getMessage());
        }

        return response.getResult();
    }

    @Override
    public void setLogLevel(String level) throws IOException {
        JsonObject params = new JsonObject();
        params.addProperty("level", level);

        sendNotification("logging/setLevel", params);
    }

    private MCPResponse sendRequest(String method, JsonObject params) throws IOException {
        String requestId = String.valueOf(requestIdCounter.incrementAndGet());
        MCPRequest request = new MCPRequest(requestId, method, params);

        logger.info("Sending request - Method: {}, ID: {}", method, requestId);
        logger.debug("Request details: {}", request.toJson());

        transport.send(request);

        logger.info("Waiting for response to request ID: {}", requestId);
        MCPResponse response = transport.receive(60000); // 60 second timeout for debugging

        logger.info("Received response for request ID: {}", requestId);
        return response;
    }

    private void sendNotification(String method) throws IOException {
        sendNotification(method, null);
    }

    private void sendNotification(String method, JsonObject params) throws IOException {
        MCPRequest notification = new MCPRequest(null, method, params);
        transport.send(notification);
    }

    private void parseServerCapabilities(JsonObject result) {
        if (result.has("capabilities")) {
            JsonObject caps = result.getAsJsonObject("capabilities");

            if (caps.has("tools")) {
                capabilities.setSupportsTools(true);
            }
            if (caps.has("resources")) {
                capabilities.setSupportsResources(true);
            }
            if (caps.has("prompts")) {
                capabilities.setSupportsPrompts(true);
            }
            if (caps.has("logging")) {
                capabilities.setSupportsLogging(true);
            }
        }
    }

    private void ensureInitialized() throws IOException {
        if (!initialized) {
            throw new IOException("Client not initialized. Call initialize() first.");
        }
    }
}