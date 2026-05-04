package org.openflexo.ta.mcp.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.resource.PamelaResourceImpl.IgnoreLoadingEdits;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.EditingContext;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.ta.mcp.rm.MCPServerResource;


public class MCPModelFactory extends PamelaModelFactory implements PamelaResourceModelFactory<MCPServerResource> {

    private static final Logger logger = Logger.getLogger(MCPModelFactory.class.getPackage().getName());

    private final MCPServerResource resource;
    private IgnoreLoadingEdits ignoreHandler = null;
    private FlexoUndoManager undoManager = null;

    public MCPModelFactory(MCPServerResource resource, EditingContext editingContext) throws ModelDefinitionException {
        super(PamelaMetaModelLibrary.retrieveMetaModel(MCPServer.class, MCPTool.class, MCPEnvVar.class));
        this.resource = resource;
        setEditingContext(editingContext);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Created MCP model factory"
                    + (resource != null ? " for resource: " + resource.getURI() : " (standalone)"));
        }
    }

    @Override
    public MCPServerResource getResource() {
        return resource;
    }


    public MCPServer makeMCPServer() {
        MCPServer server = newInstance(MCPServer.class);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Created new MCP server");
        }
        return server;
    }


    public MCPTool makeMCPTool(String name, String description) {
        MCPTool tool = newInstance(MCPTool.class);
        tool.setName(name);
        tool.setDescription(description);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Created MCP tool: name=" + name);
        }
        return tool;
    }
    public MCPEnvVar makeMCPEnvVar(String key, String value) {
        MCPEnvVar envVar = newInstance(MCPEnvVar.class);
        envVar.setKey(key);
        envVar.setValue(value);
        return envVar;
    }
    @Override
    public synchronized void startDeserializing() {
        if (resource == null || resource.getServiceManager() == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Cannot start deserializing: resource or service manager is null");
            }
            return;
        }

        EditingContext editingContext = resource.getServiceManager().getEditingContext();

        if (editingContext != null && editingContext.getUndoManager() instanceof FlexoUndoManager) {
            undoManager = (FlexoUndoManager) editingContext.getUndoManager();
            undoManager.addToIgnoreHandlers(ignoreHandler = new IgnoreLoadingEdits(resource));

            if (logger.isLoggable(Level.INFO)) {
                logger.info("Started loading MCP resource: " + resource.getURI());
            }
        }
    }

    @Override
    public synchronized void stopDeserializing() {
        if (ignoreHandler != null && undoManager != null) {
            undoManager.removeFromIgnoreHandlers(ignoreHandler);

            if (logger.isLoggable(Level.INFO)) {
                logger.info("Finished loading MCP resource: " + resource.getURI());
            }

            ignoreHandler = null;
            undoManager = null;
        }
    }

    @Override
    public String toString() {
        return "MCPModelFactory[resource=" + (resource != null ? resource.getURI() : "null") + "]";
    }
}