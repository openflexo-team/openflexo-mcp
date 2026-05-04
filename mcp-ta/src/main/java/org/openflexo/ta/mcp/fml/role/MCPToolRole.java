package org.openflexo.ta.mcp.fml.role;

import org.openflexo.foundation.fml.FlexoConcept;
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
import org.openflexo.ta.mcp.model.MCPTool;

import java.lang.reflect.Type;


@ModelEntity
@ImplementationClass(MCPToolRole.MCPToolRoleImpl.class)
@XMLElement
@FML("MCPToolRole")
public interface MCPToolRole extends FlexoRole<MCPTool> {


    public static abstract class MCPToolRoleImpl extends FlexoRoleImpl<MCPTool> implements MCPToolRole {

        @Override
        public Type getType() {
            return MCPTool.class;
        }

        @Override
        public Class<? extends TechnologyAdapter> getRoleTechnologyAdapterClass() {
            return MCPTechnologyAdapter.class;
        }

        public MCPTool getModellingElement(Object object) {
            if (object instanceof MCPTool) {
                return (MCPTool) object;
            }
            return null;
        }

        @Override
        public ActorReference<MCPTool> makeActorReference(MCPTool tool, FlexoConceptInstance fci) {
            PamelaResource<?, ?> vmiResource =
                    (PamelaResource<?, ?>) fci.getVirtualModelInstance().getResource();
            @SuppressWarnings("unchecked")
            MCPActorReference<MCPTool> ref = vmiResource.getFactory().newInstance(MCPActorReference.class);
            ref.setFlexoRole(this);
            ref.setFlexoConceptInstance(fci);
            ref.setModellingElement(tool);
            return ref;
        }

        public FlexoConceptInstance makeFlexoConceptInstance(MCPTool tool, FlexoConceptInstance containerFci, FlexoConcept flexoConcept) {

            return null;
        }
    }
}