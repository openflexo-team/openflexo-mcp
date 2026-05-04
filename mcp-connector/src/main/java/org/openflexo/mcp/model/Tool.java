package org.openflexo.mcp.model;

import com.google.gson.JsonObject;

public class Tool {

    private String name;
    private String description;
    private JsonObject inputSchema;

    public Tool(String name, String description, JsonObject inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JsonObject getInputSchema() {
        return inputSchema;
    }

    public void setInputSchema(JsonObject inputSchema) {
        this.inputSchema = inputSchema;
    }

    @Override
    public String toString() {
        return String.format("Tool[name=%s, description=%s]", name, description);
    }
}