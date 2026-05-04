package org.openflexo.ta.mcp.model;

 import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.ta.mcp.MCPTechnologyAdapter;


@ModelEntity(isAbstract = true)
@ImplementationClass(MCPObject.MCPObjectImpl.class)
public interface MCPObject extends TechnologyObject<MCPTechnologyAdapter> {

    @PropertyIdentifier(type = String.class)
    String URI_KEY = "uri";

    @Getter(value = URI_KEY)
    String getURI();

    @Setter(URI_KEY)
    void setURI(String uri);


    abstract class MCPObjectImpl extends FlexoObjectImpl implements MCPObject {

        protected String uri;

        @Override
        public String getURI() {
            return uri;
        }

        @Override
        public void setURI(String uri) {
            if ((uri == null && this.uri != null)
                    || (uri != null && !uri.equals(this.uri))) {
                String oldValue = this.uri;
                this.uri = uri;
                getPropertyChangeSupport().firePropertyChange(URI_KEY, oldValue, uri);
            }
        }


        @Override
        public MCPTechnologyAdapter getTechnologyAdapter() {
            return null;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + (uri != null ? uri : "no-uri") + "]";
        }
    }
}