package org.openflexo.mcp.model;

public class ServerCapabilities {

    private boolean supportsTools;
    private boolean supportsResources;
    private boolean supportsPrompts;
    private boolean supportsLogging;

    public ServerCapabilities() {
        this.supportsTools = false;
        this.supportsResources = false;
        this.supportsPrompts = false;
        this.supportsLogging = false;
    }

    public boolean supportsTools() {
        return supportsTools;
    }

    public void setSupportsTools(boolean supportsTools) {
        this.supportsTools = supportsTools;
    }

    public boolean supportsResources() {
        return supportsResources;
    }

    public void setSupportsResources(boolean supportsResources) {
        this.supportsResources = supportsResources;
    }

    public boolean supportsPrompts() {
        return supportsPrompts;
    }

    public void setSupportsPrompts(boolean supportsPrompts) {
        this.supportsPrompts = supportsPrompts;
    }

    public boolean supportsLogging() {
        return supportsLogging;
    }

    public void setSupportsLogging(boolean supportsLogging) {
        this.supportsLogging = supportsLogging;
    }

    @Override
    public String toString() {
        return String.format("ServerCapabilities[tools=%s, resources=%s, prompts=%s, logging=%s]",
                supportsTools, supportsResources, supportsPrompts, supportsLogging);
    }
}