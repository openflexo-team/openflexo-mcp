package org.openflexo.ta.mcp.rm;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.TechnologySpecificPamelaResourceFactory;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.ta.mcp.MCPTechnologyAdapter;
import org.openflexo.ta.mcp.model.MCPModelFactory;
import org.openflexo.ta.mcp.model.MCPServer;


public class MCPServerResourceFactory
        extends TechnologySpecificPamelaResourceFactory<MCPServerResource, MCPServer, MCPTechnologyAdapter, MCPModelFactory> {

    private static final Logger logger = Logger.getLogger(MCPServerResourceFactory.class.getPackage().getName());

    public static final String MCP_FILE_EXTENSION = ".mcp";

    public MCPServerResourceFactory() throws ModelDefinitionException {
        super(MCPServerResource.class);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Created MCPServerResourceFactory");
        }
    }


    @Override
    public MCPServer makeEmptyResourceData(MCPServerResource resource) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Creating empty MCP server for resource: " + resource.getURI());
        }
        MCPServer server = resource.getFactory().makeMCPServer();
        return server;
    }


    @Override
    public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
        if (serializationArtefact == null || resourceCenter == null) {
            return false;
        }

        String name = resourceCenter.retrieveName(serializationArtefact);
        if (name == null) {
            return false;
        }

        return name.toLowerCase().endsWith(MCP_FILE_EXTENSION) && !name.startsWith("~");
    }


    @Override
    public <I> MCPServerResource registerResource(MCPServerResource resource, FlexoResourceCenter<I> resourceCenter) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Registering MCP resource: " + resource.getURI());
        }

        super.registerResource(resource, resourceCenter);

        MCPTechnologyAdapter adapter = getTechnologyAdapter(resourceCenter.getServiceManager());
        if (adapter == null) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.severe("Cannot get MCP technology adapter");
            }
            return resource;
        }

        MCPServerResourceRepository<?> repository = adapter.getMCPServerResourceRepository(resourceCenter);
        if (repository == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Cannot get MCP resource repository for " + resourceCenter.getName());
            }
            return resource;
        }

        registerResourceInResourceRepository(resource, repository);

        if (logger.isLoggable(Level.INFO)) {
            logger.info("Successfully registered MCP resource: " + resource.getURI());
        }

        return resource;
    }


    @Override
    public MCPModelFactory makeModelFactory(MCPServerResource resource,
                                            TechnologyContextManager<MCPTechnologyAdapter> technologyContextManager)
            throws ModelDefinitionException {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Creating model factory for resource: " + resource.getURI());
        }

        if (technologyContextManager == null) {
            throw new IllegalArgumentException("Technology context manager cannot be null");
        }

        return new MCPModelFactory(
                resource,
                technologyContextManager.getServiceManager().getEditingContext()
        );
    }

    @Override
    public String toString() {
        return "MCPServerResourceFactory[extension=" + MCP_FILE_EXTENSION + "]";
    }
}