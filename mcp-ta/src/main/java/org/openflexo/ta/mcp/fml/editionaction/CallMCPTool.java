package org.openflexo.ta.mcp.fml.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificActionDefiningReceiver;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.mcp.client.MCPClient;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.ta.mcp.MCPModelSlot;
import org.openflexo.ta.mcp.model.MCPServer;
import org.openflexo.ta.mcp.model.MCPTool;


@ModelEntity
@ImplementationClass(CallMCPTool.CallMCPToolImpl.class)
@XMLElement
@FML("CallMCPTool")
public interface CallMCPTool
        extends TechnologySpecificActionDefiningReceiver<MCPModelSlot, MCPServer, String> {

    @PropertyIdentifier(type = DataBinding.class)
    String TOOL_KEY = "tool";

    @PropertyIdentifier(type = DataBinding.class)
    String ARGUMENTS_KEY = "arguments";

    @Getter(value = TOOL_KEY)
    @XMLAttribute
    @FMLAttribute(value = TOOL_KEY)
    DataBinding<MCPTool> getTool();

    @Setter(TOOL_KEY)
    void setTool(DataBinding<MCPTool> tool);

    @Getter(value = ARGUMENTS_KEY)
    @XMLAttribute
    @FMLAttribute(value = ARGUMENTS_KEY)
    DataBinding<String> getArguments();

    @Setter(ARGUMENTS_KEY)
    void setArguments(DataBinding<String> arguments);

    public static abstract class CallMCPToolImpl
            extends TechnologySpecificActionDefiningReceiverImpl<MCPModelSlot, MCPServer, String>
            implements CallMCPTool {

        private static final Logger logger = Logger.getLogger(CallMCPTool.class.getPackage().getName());

        @Override
        public Type getAssignableType() {
            return String.class;
        }

        @Override
        public String execute(RunTimeEvaluationContext evaluationContext) {
            try {
                 MCPServer server = getReceiver(evaluationContext);
                if (server == null) {
                    logger.warning("Server is null");
                    return null;
                }

                 MCPTool tool = getTool() != null ? getTool().getBindingValue(evaluationContext) : null;
                if (tool == null) {
                    logger.warning("Tool is null");
                    return null;
                }

                 String argsJson = getArguments() != null ? getArguments().getBindingValue(evaluationContext) : "{}";
                if (argsJson == null || argsJson.trim().isEmpty()) {
                    argsJson = "{}";
                }

                String unescapedArgsJson = argsJson.replace("\\\"", "\"");
                unescapedArgsJson = unescapedArgsJson.replace("\\\\", "\\");
                 if (!server.isStarted()) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("Server not connected, connecting...");
                    }
                    server.start();
                }

                MCPClient client = server.getClient();
                if (client == null) {
                    logger.warning("Client is null after connection");
                    return null;
                }
                logger.info("Raw JSON string received: '" + argsJson + "'");


                JsonObject arguments = parseJsonArguments(unescapedArgsJson);

                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Calling tool: " + tool.getName() + " with args: " + arguments);
                }


                JsonElement result = client.callTool(tool.getName(), arguments);

                 if (result != null) {
                    String resultText = extractTextContent(result);
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("Tool result: " + resultText);
                    }
                    return resultText;
                }

                return null;

            } catch (Exception e) {
                if (logger.isLoggable(Level.SEVERE)) {
                    logger.severe("Error calling tool: " + e.getMessage());
                }
                e.printStackTrace();
                return null;
            }
        }


        private JsonObject parseJsonArguments(String json) {
            if (json == null || json.trim().isEmpty() || json.trim().equals("{}")) {
                return new JsonObject();
            }

            try {
                JsonElement element = JsonParser.parseString(json);
                if (element.isJsonObject()) {
                    return element.getAsJsonObject();
                } else {
                    logger.warning("Arguments is not a JSON object, using empty: " + json);
                    return new JsonObject();
                }
            } catch (Exception e) {
                logger.warning("Failed to parse JSON arguments: " + e.getMessage() + " - input: " + json);
                return new JsonObject();
            }
        }


        private String extractTextContent(JsonElement result) {
            try {
                if (result.isJsonObject()) {
                    JsonObject resultObj = result.getAsJsonObject();

                     if (resultObj.has("content") && resultObj.get("content").isJsonArray()) {
                        StringBuilder sb = new StringBuilder();
                        for (JsonElement contentItem : resultObj.getAsJsonArray("content")) {
                            if (contentItem.isJsonObject()) {
                                JsonObject item = contentItem.getAsJsonObject();
                                if (item.has("text")) {
                                    if (sb.length() > 0) {
                                        sb.append("\n");
                                    }
                                    sb.append(item.get("text").getAsString());
                                }
                            }
                        }
                        if (sb.length() > 0) {
                            return sb.toString();
                        }
                    }
                }
            } catch (Exception e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Could not extract structured text, using toString(): " + e.getMessage());
                }
            }

             return result.toString();
        }
    }
}