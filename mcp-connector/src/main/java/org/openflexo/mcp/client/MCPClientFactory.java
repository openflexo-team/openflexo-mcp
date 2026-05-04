package org.openflexo.mcp.client;

import org.openflexo.mcp.transport.HttpTransport;
import org.openflexo.mcp.transport.MCPTransport;
import org.openflexo.mcp.transport.StdioTransport;

import java.io.IOException;
import java.util.Map;

public class MCPClientFactory {

    public static MCPClient createStdioClient(String... command) {
        MCPTransport transport = new StdioTransport(command);
        return new MCPClientImpl(transport);
    }
    public static MCPClient createStdioClient(Map<String, String> envVars, String... command) {
        MCPTransport transport = new StdioTransport(envVars, command);
        return new MCPClientImpl(transport);
    }
    public static MCPClient createClient(MCPTransport transport) {
        return new MCPClientImpl(transport);
    }
    public static MCPClient createHttpClient(String endpoint) throws IOException {
        HttpTransport transport = new HttpTransport(endpoint);
        return new MCPClientImpl(transport);
    }
}