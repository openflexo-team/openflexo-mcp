package org.openflexo.mcp.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openflexo.mcp.model.Prompt;
import org.openflexo.mcp.model.Resource;
import org.openflexo.mcp.model.ServerCapabilities;
import org.openflexo.mcp.model.Tool;

import java.io.IOException;
import java.util.List;


public interface MCPClient {

    // Connection lifecycle
    void connect() throws IOException;
    void disconnect() throws IOException;
    boolean isConnected();

    // Server initialization
    void initialize(String clientName, String clientVersion) throws IOException;
    ServerCapabilities getServerCapabilities();

    // Tools
    List<Tool> listTools() throws IOException;
    JsonElement callTool(String toolName, JsonObject arguments) throws IOException;

    // Resources
    List<Resource> listResources() throws IOException;
    JsonElement readResource(String uri) throws IOException;

    // Prompts
    List<Prompt> listPrompts() throws IOException;
    JsonElement getPrompt(String promptName, JsonObject arguments) throws IOException;

    // Logging
    void setLogLevel(String level) throws IOException;
}