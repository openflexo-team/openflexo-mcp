package org.openflexo.ta.mcp.fml.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificActionDefiningReceiver;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.ta.mcp. MCPModelSlot;
import org.openflexo.ta.mcp.model.MCPEnvVar;
import org.openflexo.ta.mcp.model.MCPModelFactory;
import org.openflexo.ta.mcp.model.MCPServer;


@ModelEntity
@ImplementationClass(ConnectMCPServer.ConnectMCPServerImpl.class)
@XMLElement
@FML("ConnectMCPServer")
public interface ConnectMCPServer
        extends TechnologySpecificActionDefiningReceiver<MCPModelSlot, MCPServer, MCPServer> {

    @PropertyIdentifier(type = DataBinding.class)
    String COMMAND_KEY = "command";

    @PropertyIdentifier(type = DataBinding.class)
    String ARGS_KEY = "args";

    @Getter(value = COMMAND_KEY)
    @XMLAttribute
    @FMLAttribute(value = COMMAND_KEY)
    DataBinding<String> getCommand();

    @Setter(COMMAND_KEY)
    void setCommand(DataBinding<String> command);

    @Getter(value = ARGS_KEY)
    @XMLAttribute
    @FMLAttribute(value = ARGS_KEY)
    DataBinding<String> getArgs();

    @Setter(ARGS_KEY)
    void setArgs(DataBinding<String> args);

    @PropertyIdentifier(type = DataBinding.class)
    String ENV_VARS_KEY = "envVars";

    @Getter(value = ENV_VARS_KEY)
    @XMLAttribute
    @FMLAttribute(value = ENV_VARS_KEY)
    DataBinding<String> getEnvVars();

    @Setter(ENV_VARS_KEY)
    void setEnvVars(DataBinding<String> envVars);

    public static abstract class ConnectMCPServerImpl
            extends TechnologySpecificActionDefiningReceiverImpl<MCPModelSlot, MCPServer, MCPServer>
            implements ConnectMCPServer {

        private static final Logger logger = Logger.getLogger(ConnectMCPServer.class.getPackage().getName());

        @Override
        public Type getAssignableType() {
            return MCPServer.class;
        }

        @Override
        public MCPServer execute(RunTimeEvaluationContext evaluationContext) {
            try {
                 String command = getCommand() != null ? getCommand().getBindingValue(evaluationContext) : null;
                String argsStr = getArgs() != null ? getArgs().getBindingValue(evaluationContext) : null;

                if (command == null || command.isEmpty()) {
                    logger.warning("Command is null or empty - cannot connect");
                    return null;
                }

                MCPModelFactory factory = new MCPModelFactory(null, null);
                MCPServer server = factory.makeMCPServer();

                server.setName(command);
                server.setCommand(command);

                 if (argsStr != null && !argsStr.isEmpty()) {
                    String[] args = argsStr.split(",");
                    for (String arg : args) {
                        server.addToArgs(arg.trim());
                    }
                }

                 if (logger.isLoggable(Level.INFO)) {
                    logger.info("Connecting to MCP server: " + command + " " + argsStr);
                }

                String envVarsStr = getEnvVars() != null ? getEnvVars().getBindingValue(evaluationContext) : null;
                if (envVarsStr != null && !envVarsStr.isEmpty()) {
                    String[] pairs = envVarsStr.split(",");
                    for (String pair : pairs) {
                        String[] kv = pair.split("=", 2);
                        if (kv.length == 2) {
                            MCPEnvVar envVar = factory.makeMCPEnvVar(kv[0].trim(), kv[1].trim());
                            server.addToEnvVars(envVar);
                        }
                    }
                }
                server.start();

                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Connected successfully to MCP server: " + server.getName());
                }

                return server;

            } catch (Exception e) {
                if (logger.isLoggable(Level.SEVERE)) {
                    logger.severe("Failed to connect to MCP server: " + e.getMessage());
                }
                e.printStackTrace();
                return null;
            }
        }
    }
}