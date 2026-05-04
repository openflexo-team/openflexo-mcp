package org.openflexo.ta.mcp.model;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;


@ModelEntity
@ImplementationClass(MCPTool.MCPToolImpl.class)
public interface MCPTool extends MCPObject {

    @PropertyIdentifier(type = String.class)
    String NAME_KEY = "name";

    @PropertyIdentifier(type = String.class)
    String DESCRIPTION_KEY = "description";

    @PropertyIdentifier(type = MCPServer.class)
    String SERVER_KEY = "server";

    @Getter(value = NAME_KEY)
    String getName();

    @Setter(NAME_KEY)
    void setName(String name);

    @Getter(value = DESCRIPTION_KEY)
    String getDescription();

    @Setter(DESCRIPTION_KEY)
    void setDescription(String description);

    @Getter(value = SERVER_KEY)
    MCPServer getServer();

    @Setter(SERVER_KEY)
    void setServer(MCPServer server);

    abstract class MCPToolImpl extends MCPObjectImpl implements MCPTool {

        @Override
        public String getURI() {
            String serverName = getServer() != null && getServer().getName() != null ? getServer().getName() : "unknown";
            String toolName = getName() != null ? getName() : "unknown";
            return "mcp://server/" + serverName + "/tool/" + toolName;
        }
    }
}