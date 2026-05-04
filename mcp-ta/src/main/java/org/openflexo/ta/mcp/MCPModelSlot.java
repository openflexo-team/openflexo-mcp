package org.openflexo.ta.mcp;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.annotations.*;
import org.openflexo.foundation.technologyadapter.FreeModelSlot;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.ta.mcp.fml.MCPActorReference;
import org.openflexo.ta.mcp.fml.editionaction.CallMCPTool;
import org.openflexo.ta.mcp.fml.editionaction.ConnectMCPServer;
import org.openflexo.ta.mcp.fml.editionaction.SelectMCPTool;
import org.openflexo.ta.mcp.fml.role.MCPServerRole;
import org.openflexo.ta.mcp.fml.role.MCPToolRole;
import org.openflexo.ta.mcp.model.MCPObject;
import org.openflexo.ta.mcp.model.MCPServer;
import org.openflexo.ta.mcp.rm.MCPServerResource;


@DeclareActorReferences({ MCPActorReference.class })
@DeclareFlexoRoles({
        MCPServerRole.class,
        MCPToolRole.class
})
@DeclareEditionActions({
        ConnectMCPServer.class,
        SelectMCPTool.class,
        CallMCPTool.class
})
@DeclareFetchRequests({})
@ModelEntity
@ImplementationClass(MCPModelSlot.MCPModelSlotImpl.class)
@XMLElement
@FML("MCPModelSlot")
public interface MCPModelSlot extends FreeModelSlot<MCPServer, MCPServerResource> {

    @PropertyIdentifier(type = String.class)
    String SERVER_COMMAND_KEY = "serverCommand";

    @PropertyIdentifier(type = String.class)
    String SERVER_ARGS_KEY = "serverArgs";

    @Getter(value = SERVER_COMMAND_KEY)
    @XMLAttribute
    @FMLAttribute(SERVER_COMMAND_KEY)
    String getServerCommand();

    @Setter(SERVER_COMMAND_KEY)
    void setServerCommand(String command);

    @Getter(value = SERVER_ARGS_KEY)
    @XMLAttribute
    @FMLAttribute(SERVER_ARGS_KEY)
    String getServerArgs();

    @Setter(SERVER_ARGS_KEY)
    void setServerArgs(String args);

    abstract class MCPModelSlotImpl
            extends FreeModelSlotImpl<MCPServer, MCPServerResource>
            implements MCPModelSlot {


        private final Map<String, MCPObject> uriCache = new HashMap<>();

        @Override
        public Class<MCPTechnologyAdapter> getTechnologyAdapterClass() {
            return MCPTechnologyAdapter.class;
        }

        @Override
        public Type getType() {
            return MCPServer.class;
        }

        @Override
        public MCPTechnologyAdapter getModelSlotTechnologyAdapter() {
            return (MCPTechnologyAdapter) super.getModelSlotTechnologyAdapter();
        }


        @Override
        public <PR extends FlexoRole<?>> String defaultFlexoRoleName(Class<PR> patternRoleClass) {
            if (MCPServerRole.class.isAssignableFrom(patternRoleClass)) {
                return "server";
            }
            if (MCPToolRole.class.isAssignableFrom(patternRoleClass)) {
                return "tool";
            }
            return null;
        }


        public MCPObject getMCPObjectWithURI(String objectURI) {
            if (objectURI == null) {
                return null;
            }

             if (uriCache.containsKey(objectURI)) {
                return uriCache.get(objectURI);
            }

            return null;
        }

         public void registerObjectInCache(MCPObject object) {
            if (object != null && object.getURI() != null) {
                uriCache.put(object.getURI(), object);
            }
        }


        public void clearCache() {
            uriCache.clear();
        }
    }
}