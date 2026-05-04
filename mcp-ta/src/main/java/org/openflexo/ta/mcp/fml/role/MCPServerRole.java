package org.openflexo.ta.mcp.fml.role;

import java.lang.reflect.Type;

import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.ta.mcp.MCPTechnologyAdapter;
import org.openflexo.ta.mcp.fml.MCPActorReference;
import org.openflexo.ta.mcp.model.MCPServer;


@ModelEntity
@ImplementationClass(MCPServerRole.MCPServerRoleImpl.class)
@XMLElement
@FML("MCPServerRole")
public interface MCPServerRole extends FlexoRole<MCPServer> {

    public static abstract class MCPServerRoleImpl extends FlexoRoleImpl<MCPServer> implements MCPServerRole {

        @Override
        public Type getType() {
            return MCPServer.class;
        }

        @Override
        public Class<? extends TechnologyAdapter> getRoleTechnologyAdapterClass() {
            return MCPTechnologyAdapter.class;
        }

        @Override
        public ActorReference<MCPServer> makeActorReference(MCPServer server, FlexoConceptInstance fci) {

            PamelaResource<?, ?> vmiResource =
                    (PamelaResource<?, ?>) fci.getVirtualModelInstance().getResource();
            MCPActorReference<MCPServer> ref = vmiResource.getFactory().newInstance(MCPActorReference.class);
            ref.setFlexoRole(this);
            ref.setFlexoConceptInstance(fci);
            ref.setModellingElement(server);
            return (ActorReference<MCPServer>) ref;
        }
    }
}