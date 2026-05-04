package org.openflexo.ta.mcp.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.*;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.mcp.client.MCPClient;
import org.openflexo.mcp.client.MCPClientFactory;
import org.openflexo.pamela.annotations.*;
import org.openflexo.pamela.annotations.Getter.Cardinality;

import org.openflexo.ta.mcp.MCPTechnologyAdapter;
import org.openflexo.ta.mcp.rm.MCPServerResource;
import org.openflexo.ta.mcp.utils.MCPAdapterResultOptions;


@ModelEntity
@ImplementationClass(MCPServer.MCPServerImpl.class)
public interface MCPServer extends MCPObject, ResourceData<MCPServer> {

    @PropertyIdentifier(type = String.class)
    String NAME_KEY = "name";

    @PropertyIdentifier(type = String.class)
    String COMMAND_KEY = "command";

    @PropertyIdentifier(type = List.class)
    String ARGS_KEY = "args";

    @PropertyIdentifier(type = List.class)
    String TOOLS_KEY = "tools";

    @PropertyIdentifier(type = Boolean.class)
    String CONNECTED_KEY = "connected";

    @Getter(value = NAME_KEY)

    String getName();

    @Setter(NAME_KEY)
    void setName(String name);

    @Getter(value = COMMAND_KEY)
    String getCommand();

    @Setter(COMMAND_KEY)
    void setCommand(String command);

    @Getter(value = ARGS_KEY, cardinality = Cardinality.LIST)
    List<String> getArgs();

    @Setter(ARGS_KEY)
    void setArgs(List<String> args);

    @Adder(ARGS_KEY)
    void addToArgs(String arg);

    @Remover(ARGS_KEY)
    void removeFromArgs(String arg);

    @Getter(value = TOOLS_KEY, cardinality = Cardinality.LIST)
    List<MCPTool> getTools();

    @Setter(TOOLS_KEY)
    void setTools(List<MCPTool> tools);

    @Adder(TOOLS_KEY)
    void addToTools(MCPTool tool);

    @Remover(TOOLS_KEY)
    void removeFromTools(MCPTool tool);

    @Getter(value = CONNECTED_KEY, defaultValue = "false")
    boolean isStarted();

    @Setter(CONNECTED_KEY)
    void setConnected(boolean connected);

    @PropertyIdentifier(type = List.class)
    String ENV_KEY = "envVars";

    @Getter(value = ENV_KEY, cardinality = Getter.Cardinality.LIST)
    @XMLElement
    @FMLAttribute(ENV_KEY)
    List<MCPEnvVar> getEnvVars();

    @PropertyIdentifier(type = String.class)
    String URL_KEY = "url";

    @Getter(value = URL_KEY)
    @XMLElement
    @FMLAttribute(URL_KEY)
    String getUrl();

    @Setter(URL_KEY)
    void setUrl(String url);
    @Adder(ENV_KEY)
    void addToEnvVars(MCPEnvVar envVar);

    @Remover(ENV_KEY)
    void removeFromEnvVars(MCPEnvVar envVar);

    @Override
    MCPServerResource getResource();
    List<String> getToolNames() throws IOException;
    String callToolWithNamedArg(String toolName, String argKey, String argValue) throws IOException;
    String callToolWithTwoArgs(String toolName, String key1, String value1, String key2, String value2) throws IOException;
    MCPClient start() throws Exception;
    void disconnect();
    MCPClient getClient();
    String callToolWithArgs(String toolName, String jsonArgs) throws IOException;
    String callToolWithArgs(String toolName, String argKey, String argValue) throws IOException;
    public String extractTextFromResult(String mcpResult);
    public String callToolsWithArgsObject(String toolName, Map<String,Object> argMap) throws IOException ;
    Object parseToolResult(String mcpResult);



    abstract class MCPServerImpl extends MCPObjectImpl implements MCPServer {

        private MCPClient client;


        @Override
        public MCPTechnologyAdapter getTechnologyAdapter() {
            if (getResource() != null && getResource().getServiceManager() != null) {
                return getResource().getServiceManager()
                        .getTechnologyAdapterService()
                        .getTechnologyAdapter(MCPTechnologyAdapter.class);
            }
            return null;
        }


        public String callToolWithArgs(String toolName, String pathValue) throws IOException {
            JsonObject args = new JsonObject();
            args.addProperty("path", pathValue);
            JsonElement result = client.callTool(toolName, args);
            return result != null ? result.toString() : null;
        }
        public String callToolWithArgs(String toolName, String argKey, String argValue) throws IOException {
            JsonObject args = new JsonObject();
            args.addProperty(argKey, argValue);
            JsonElement result = client.callTool(toolName, args);
            return result != null ? result.toString() : null;
        }

        @Override
        public String callToolsWithArgsObject(String toolName, Map<String,Object> argMap) throws IOException {
            JsonObject args = parseArgs(argMap);
            JsonElement result = client.callTool(toolName, args);
            return result != null ? result.toString() : null;

        }
        /**
         * Function to call the tool and recieve a response not as a string but as a JsonElement, allowing to handle more complex responses with nested structures.
         * */
        public JsonElement callToolWithArgsAsJson(String toolName, Map<String,Object> argsMap) throws IOException {
            JsonObject args = parseArgs(argsMap);

            return client.callTool(toolName, args);
        }
        private JsonObject parseArgs(Map<String ,Object> args){
            JsonObject jsonArgs = new JsonObject();
            for (Map.Entry<String, Object> entry : args.entrySet()) {
                if (entry.getValue() instanceof String) {
                    jsonArgs.addProperty(entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() instanceof Number) {
                    jsonArgs.addProperty(entry.getKey(), (Number) entry.getValue());
                } else if (entry.getValue() instanceof Boolean) {
                    jsonArgs.addProperty(entry.getKey(), (Boolean) entry.getValue());
                } else {
                    // For complex objects, you might want to convert them to JSON strings or handle them differently
                    jsonArgs.add(entry.getKey(), new Gson().toJsonTree(entry.getValue()));
                }
            }
            return jsonArgs;
        }






        @Override
        public MCPClient start() throws Exception {
            if (client != null && isStarted()) {
                return client;
            }
            String url = getUrl();

            if (url != null && !url.isEmpty()) {
                // HTTP transport
                client = MCPClientFactory.createHttpClient(url);
                client.connect();
                client.initialize("OpenFlexo", "1.0.0");
                setConnected(true);
                return client;
            }

            String command = getCommand();
            List<String> args = getArgs();

            if (command == null) {
                throw new IllegalStateException("Either url or command must be set before connecting");
            }

            List<String> fullCommand = new ArrayList<>();
            fullCommand.add(command);
            if (args != null) {
                fullCommand.addAll(args);
            }
            java.util.Map<String, String> env = new java.util.HashMap<>();
            if (getEnvVars() != null) {
                for (MCPEnvVar envVar : getEnvVars()) {
                    env.put(envVar.getKey(), envVar.getValue());
                }
            }
            client = MCPClientFactory.createStdioClient(env, fullCommand.toArray(new String[0]));

            client.connect();
            client.initialize("OpenFlexo", "1.0.0");
            setConnected(true);

            return client;
        }
        @Override
        public List<String> getToolNames() throws IOException {
            if (client == null) return java.util.Collections.emptyList();
            return client.listTools().stream()
                    .map(t -> t.getName())
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public String callToolWithNamedArg(String toolName, String argKey, String argValue) throws IOException {
            JsonObject args = new JsonObject();
            args.addProperty(argKey, argValue);
            JsonElement result = client.callTool(toolName, args);
            return result != null ? result.toString() : null;
        }
        @Override
        public String callToolWithTwoArgs(String toolName, String key1, String value1, String key2, String value2) throws IOException {
            JsonObject args = new JsonObject();
            args.addProperty(key1, value1);
            args.addProperty(key2, value2);
            JsonElement result = client.callTool(toolName, args);
            return result != null ? result.toString() : null;
        }
        @Override
        public String extractTextFromResult(String mcpResult) {
            if (mcpResult == null) return null;
            try {
                JsonObject obj = JsonParser.parseString(mcpResult).getAsJsonObject();
                JsonArray content = obj.getAsJsonArray("content");
                if (content != null && content.size() > 0) {
                    JsonObject first = content.get(0).getAsJsonObject();
                    if (first.has("text")) {
                        return first.get("text").getAsString();
                    }
                }
            } catch (Exception e) {
                // not MCP wrapped, return as-is
            }
            return mcpResult;
        }

        @Override
        public void disconnect() {
            if (client != null) {
                try {
                    client.disconnect();
                } catch (Exception e) {
                    System.err.println("Error disconnecting: " + e.getMessage());
                }
                setConnected(false);
                client = null;
            }
        }

        @Override
        public MCPClient getClient() {
            return client;
        }

        @Override
        public String getURI() {
            String name = getName();
            if (name != null) {
                return "mcp://server/" + name;
            }
            return "mcp://server/unknown";
        }


    }




}