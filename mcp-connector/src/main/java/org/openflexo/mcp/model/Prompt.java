package org.openflexo.mcp.model;

import com.google.gson.JsonObject;

public class Prompt {

    private String name;
    private String description;
    private JsonObject arguments;

    public Prompt(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Prompt(String name, String description, JsonObject arguments) {
        this.name = name;
        this.description = description;
        this.arguments = arguments;
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

    public JsonObject getArguments() {
        return arguments;
    }

    public void setArguments(JsonObject arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return String.format("Prompt[name=%s, description=%s]", name, description);
    }
}