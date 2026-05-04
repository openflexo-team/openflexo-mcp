package org.openflexo.ta.mcp.fml.editionaction;

import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificActionDefiningReceiver;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.mcp.client.MCPClient;
import org.openflexo.mcp.model.Tool;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.ta.mcp.MCPModelSlot;
 import org.openflexo.ta.mcp.model.MCPModelFactory;
import org.openflexo.ta.mcp.model.MCPServer;
import org.openflexo.ta.mcp.model.MCPTool;
import org.openflexo.ta.mcp.rm.MCPServerResource;


@ModelEntity
@ImplementationClass(SelectMCPTool.SelectMCPToolImpl.class)
@XMLElement
@FML("SelectMCPTool")
public interface SelectMCPTool
        extends TechnologySpecificActionDefiningReceiver<MCPModelSlot, MCPServer, MCPTool> {

    @PropertyIdentifier(type = DataBinding.class)
    String TOOL_NAME_KEY = "toolName";

    @Getter(value = TOOL_NAME_KEY)
    @XMLAttribute
    @FMLAttribute(value = TOOL_NAME_KEY)
    DataBinding<String> getToolName();

    @Setter(TOOL_NAME_KEY)
    void setToolName(DataBinding<String> toolName);

    public static abstract class SelectMCPToolImpl
            extends TechnologySpecificActionDefiningReceiverImpl<MCPModelSlot, MCPServer, MCPTool>
            implements SelectMCPTool {

        private static final Logger logger = Logger.getLogger(SelectMCPTool.class.getPackage().getName());

        @Override
        public Type getAssignableType() {
            return MCPTool.class;
        }

        @Override
        public MCPTool execute(RunTimeEvaluationContext evaluationContext) {
            try {
                 MCPServer server = getReceiver(evaluationContext);
                if (server == null) {
                    logger.warning("Server is null");
                    return null;
                }

                 String toolName = getToolName() != null ? getToolName().getBindingValue(evaluationContext) : null;
                if (toolName == null || toolName.isEmpty()) {
                    logger.warning("Tool name is null or empty");
                    return null;
                }

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

                 List<Tool> tools = client.listTools();
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Found " + tools.size() + " tools on server");
                }

                 for (Tool tool : tools) {
                    if (tool.getName().equals(toolName)) {
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("Found tool: " + toolName);
                        }


                        MCPModelFactory factory = getMCPModelFactory(server);
                        MCPTool mcpTool = factory.makeMCPTool(tool.getName(), tool.getDescription());
                        mcpTool.setServer(server);

                         server.addToTools(mcpTool);

                        return mcpTool;
                    }
                }

                logger.warning("Tool not found: " + toolName);
                return null;

            } catch (Exception e) {
                if (logger.isLoggable(Level.SEVERE)) {
                    logger.severe("Error selecting tool: " + e.getMessage());
                }
                e.printStackTrace();
                return null;
            }
        }


        private MCPModelFactory getMCPModelFactory(MCPServer server) {

            if (server.getResource() instanceof MCPServerResource) {
                try {
                    return ((MCPServerResource) server.getResource()).getFactory();
                } catch (Exception e) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Could not get factory from resource, creating standalone: " + e.getMessage());
                    }
                }
            }

             try {
                return new MCPModelFactory(null, null);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create MCPModelFactory", e);
            }
        }
    }
}