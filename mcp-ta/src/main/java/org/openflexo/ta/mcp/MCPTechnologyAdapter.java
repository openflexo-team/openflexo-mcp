package org.openflexo.ta.mcp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.annotations.DeclareModelSlots;
import org.openflexo.foundation.fml.annotations.DeclareResourceFactories;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterBindingFactory;
import org.openflexo.ta.mcp.MCPModelSlot;
import org.openflexo.ta.mcp.fml.binding.MCPBindingFactory;
import org.openflexo.ta.mcp.rm.MCPServerResourceFactory;
import org.openflexo.ta.mcp.rm.MCPServerResourceRepository;


@DeclareModelSlots({ MCPModelSlot.class })
@DeclareResourceFactories({ MCPServerResourceFactory.class })
public class MCPTechnologyAdapter extends TechnologyAdapter<MCPTechnologyAdapter> {

    private static final Logger logger = Logger.getLogger(MCPTechnologyAdapter.class.getPackage().getName());

    private static final MCPBindingFactory BINDING_FACTORY = new MCPBindingFactory();

    public MCPTechnologyAdapter() {
        super();
    }

    @Override
    public String getName() {
        return "MCP Technology";
    }

    @Override
    public String getIdentifier() {
        return "MCP";
    }

    @Override
    protected String getLocalizationDirectory() {
        return "FlexoLocalization/MCPTechnologyAdapter";
    }


    @Override
    public TechnologyAdapterBindingFactory getTechnologyAdapterBindingFactory() {
        return BINDING_FACTORY;
    }


    @Override
    public void ensureAllRepositoriesAreCreated(FlexoResourceCenter<?> rc) {
        super.ensureAllRepositoriesAreCreated(rc);
        getMCPServerResourceRepository(rc);
    }

    @Override
    public <I> boolean isIgnorable(FlexoResourceCenter<I> resourceCenter, I contents) {
        return false;
    }


    public MCPServerResourceFactory getMCPServerResourceFactory() {
        return getResourceFactory(MCPServerResourceFactory.class);
    }


    @SuppressWarnings("unchecked")
    public <I> MCPServerResourceRepository<I> getMCPServerResourceRepository(
            FlexoResourceCenter<I> resourceCenter) {

        MCPServerResourceRepository<I> returned = resourceCenter.retrieveRepository(
                MCPServerResourceRepository.class, this);

        if (returned == null) {
            returned = MCPServerResourceRepository.instanciateNewRepository(this, resourceCenter);
            resourceCenter.registerRepository(returned, MCPServerResourceRepository.class, this);

            if (logger.isLoggable(Level.INFO)) {
                logger.info("Created new MCP resource repository for: " + resourceCenter.getDefaultBaseURI());
            }
        }

        return returned;
    }
}