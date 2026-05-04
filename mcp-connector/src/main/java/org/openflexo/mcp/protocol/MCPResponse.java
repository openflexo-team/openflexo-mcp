
package org.openflexo.mcp.protocol;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class MCPResponse extends MCPMessage {

    private JsonElement result;

    private MCPError error;


    public MCPResponse(String id, JsonElement result) {
        setId(id);
        this.result = result;
        this.error = null;
    }


    public MCPResponse(String id, MCPError error) {
        setId(id);
        this.result = null;
        this.error = error;
    }


    public JsonElement getResult() {
        return result;
    }


    public void setResult(JsonElement result) {
        this.result = result;
        this.error = null; // Clear error if setting result
    }


    public MCPError getError() {
        return error;
    }


    public void setError(MCPError error) {
        this.error = error;
        this.result = null; // Clear result if setting error
    }


    public boolean isSuccess() {
        return error == null;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("jsonrpc", getJsonrpc());
        json.addProperty("id", getId());

        if (isSuccess()) {
            json.add("result", result);
        } else {
            json.add("error", error.toJson());
        }

        return json;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}