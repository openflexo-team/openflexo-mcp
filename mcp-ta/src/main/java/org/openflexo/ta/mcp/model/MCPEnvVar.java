package org.openflexo.ta.mcp.model;

import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.pamela.annotations.*;

@ModelEntity
@ImplementationClass(MCPEnvVar.MCPEnvVarImpl.class)
@XMLElement(xmlTag = "EnvVar")
public interface MCPEnvVar extends MCPObject {

    @PropertyIdentifier(type = String.class)
    String KEY_KEY = "key";

    @PropertyIdentifier(type = String.class)
    String VALUE_KEY = "value";

    @Getter(KEY_KEY)
    @XMLAttribute
    @FMLAttribute(KEY_KEY)
    String getKey();

    @Setter(KEY_KEY)
    void setKey(String key);

    @Getter(VALUE_KEY)
    @XMLAttribute
    @FMLAttribute(VALUE_KEY)
    String getValue();

    @Setter(VALUE_KEY)
    void setValue(String value);

    abstract class MCPEnvVarImpl extends MCPObjectImpl implements MCPEnvVar {
    }
}