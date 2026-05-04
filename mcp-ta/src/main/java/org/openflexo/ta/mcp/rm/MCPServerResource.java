package org.openflexo.ta.mcp.rm;

import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.ta.mcp.MCPTechnologyAdapter;
import org.openflexo.ta.mcp.model.MCPModelFactory;
import org.openflexo.ta.mcp.model.MCPServer;


@ModelEntity
@ImplementationClass(MCPServerResourceImpl.class)
public interface MCPServerResource extends
        TechnologyAdapterResource<MCPServer, MCPTechnologyAdapter>,
        PamelaResource<MCPServer, MCPModelFactory> {


    MCPServer getMCPServer();
}