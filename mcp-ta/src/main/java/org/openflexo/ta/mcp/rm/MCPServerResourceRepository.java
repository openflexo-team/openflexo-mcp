package org.openflexo.ta.mcp.rm;

import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResourceRepository;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.ta.mcp.MCPTechnologyAdapter;
import org.openflexo.ta.mcp.model.MCPServer;

@ModelEntity
public interface MCPServerResourceRepository<I>
        extends TechnologyAdapterResourceRepository<MCPServerResource, MCPTechnologyAdapter, MCPServer, I> {


    public static <I> MCPServerResourceRepository<I> instanciateNewRepository(
            MCPTechnologyAdapter technologyAdapter,
            FlexoResourceCenter<I> resourceCenter) {
        try {
            PamelaModelFactory factory = new PamelaModelFactory(MCPServerResourceRepository.class);

            @SuppressWarnings("unchecked")
            MCPServerResourceRepository<I> newRepository = factory.newInstance(MCPServerResourceRepository.class);

            newRepository.setTechnologyAdapter(technologyAdapter);
            newRepository.setResourceCenter(resourceCenter);
            newRepository.setBaseArtefact(resourceCenter.getBaseArtefact());
            newRepository.getRootFolder().setRepositoryContext(null);

            return newRepository;

        } catch (ModelDefinitionException e) {
            e.printStackTrace();
        }

        return null;
    }
}