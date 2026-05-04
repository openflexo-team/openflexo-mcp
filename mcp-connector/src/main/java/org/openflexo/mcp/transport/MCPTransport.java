package org.openflexo.mcp.transport;

import org.openflexo.mcp.protocol.MCPRequest;
import org.openflexo.mcp.protocol.MCPResponse;

import java.io.IOException;


public interface MCPTransport {


    void connect() throws IOException;


    void send(MCPRequest request) throws IOException;


    MCPResponse receive() throws IOException;

    MCPResponse receive(long timeoutMs) throws IOException;


    boolean isConnected();


    void disconnect() throws IOException;


    String getTransportType();
}