package org.openflexo.ta.mcp.fml;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FreeModelSlotInstance;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.ta.mcp.model.MCPObject;
import org.openflexo.ta.mcp.model.MCPServer;
import org.openflexo.ta.mcp.model.MCPTool;
import org.openflexo.ta.mcp.rm.MCPServerResource;


@ModelEntity
@ImplementationClass(MCPActorReference.MCPActorReferenceImpl.class)
@XMLElement
@FML("MCPActorReference")
public interface MCPActorReference<T extends MCPObject> extends ActorReference<T> {

    @PropertyIdentifier(type = String.class)
    String OBJECT_URI_KEY = "objectURI";

    @Getter(value = OBJECT_URI_KEY)
    @XMLAttribute
    String getObjectURI();

    @Setter(OBJECT_URI_KEY)
    void setObjectURI(String objectURI);

    public abstract static class MCPActorReferenceImpl<T extends MCPObject>
            extends ActorReferenceImpl<T>
            implements MCPActorReference<T> {

        private static final Logger logger = Logger.getLogger(MCPActorReference.class.getPackage().getName());

        private T object;
        private String objectURI;

        public MCPActorReferenceImpl() {
            super();
        }


        public MCPServer getMCPServer() {
            MCPServerResource resource = getMCPServerResource();
            if (resource != null) {
                try {
                    return resource.getResourceData();
                } catch (Exception e) {
                    logger.warning("Error loading MCP server resource: " + e.getMessage());
                }
            }
            return null;
        }


        public MCPServerResource getMCPServerResource() {
            FreeModelSlotInstance<?, ?, ?> msInstance = (FreeModelSlotInstance<?, ?, ?>) getModelSlotInstance();
            if (msInstance != null && msInstance.getResource() instanceof MCPServerResource) {
                return (MCPServerResource) msInstance.getResource();
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T getModellingElement(boolean forceLoading) {
            if (object != null) {
                return object;
            }

            if (objectURI != null) {
                logger.fine("Attempting to retrieve MCP object with URI: " + objectURI);

                MCPServer server = getMCPServer();
                if (server == null) {
                    logger.warning("Cannot retrieve object: MCP server is null");
                    return null;
                }

                 if (objectURI.equals(server.getURI())) {
                    object = (T) server;
                } else {
                     for (MCPTool tool : server.getTools()) {
                        if (objectURI.equals(tool.getURI())) {
                            object = (T) tool;
                            break;
                        }
                    }
                }

                if (object != null) {
                    logger.fine("Successfully retrieved MCP object: " + object.getURI());
                } else {
                    logger.warning("Could not retrieve MCP object with URI: " + objectURI);
                }
            }

            return object;
        }

        @Override
        public void setModellingElement(T object) {
            this.object = object;
            if (object != null) {
                objectURI = object.getURI();
            }
        }

        @Override
        public String getObjectURI() {
            if (object != null) {
                return object.getURI();
            }
            return objectURI;
        }

        @Override
        public void setObjectURI(String objectURI) {
            if ((objectURI == null && this.objectURI != null)
                    || (objectURI != null && !objectURI.equals(this.objectURI))) {
                String oldValue = this.objectURI;
                this.objectURI = objectURI;
                this.object = null; // Clear cache when URI changes
                getPropertyChangeSupport().firePropertyChange(OBJECT_URI_KEY, oldValue, objectURI);
            }
        }
    }
}