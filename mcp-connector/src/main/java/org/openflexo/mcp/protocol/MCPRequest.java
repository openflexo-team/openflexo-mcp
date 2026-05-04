
package org.openflexo.mcp.protocol;

import com.google.gson.JsonObject;


public class MCPRequest extends MCPMessage {

    private String method;

    private JsonObject params;


    public MCPRequest(String id, String method) {
        setId(id);
        this.method = method;
        this.params = new JsonObject();
    }


    public MCPRequest(String id, String method, JsonObject params) {
        setId(id);
        this.method = method;
        this.params = params;
    }


    public String getMethod() {
        return method;
    }


    public void setMethod(String method) {
        this.method = method;
    }


    public JsonObject getParams() {
        return params;
    }


    public void setParams(JsonObject params) {
        this.params = params;
    }


    public void addParam(String key, Object value) {
        if (params == null) {
            params = new JsonObject();
        }
        if (value instanceof String) {
            params.addProperty(key, (String) value);
        } else if (value instanceof Number) {
            params.addProperty(key, (Number) value);
        } else if (value instanceof Boolean) {
            params.addProperty(key, (Boolean) value);
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("jsonrpc", getJsonrpc());
        if (getId() != null) {
            json.addProperty("id", getId());
        }
        json.addProperty("method", method);
        if (params != null && params.size() > 0) {
            json.add("params", params);
        }
        return json;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}