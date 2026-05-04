
package org.openflexo.ta.mcp;


import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.ta.mcp.model.MCPServer;
import org.openflexo.ta.mcp.rm.MCPServerResource;


public abstract class AbstractTestMCP extends OpenflexoProjectAtRunTimeTestCase {

    protected static final Logger logger = Logger.getLogger(AbstractTestMCP.class.getPackage().getName());


    protected MCPServerResource getMCPResource(String documentName, FlexoResourceCenter<?> resourceCenter) {
        String uri = resourceCenter.getDefaultBaseURI() + "/MCP/" + documentName;
        logger.info("Looking for MCP resource: " + uri);

        FlexoResource<?> resource = serviceManager.getResourceManager().getResource(uri);

        if (resource instanceof MCPServerResource) {
            logger.info("Found MCP resource: " + resource.getURI());
            return (MCPServerResource) resource;
        }

        logger.warning("MCP resource not found: " + uri);
        return null;
    }


    protected MCPServerResource getMCPResource(String documentName) {
        for (FlexoResourceCenter<?> rc : serviceManager.getResourceCenterService().getResourceCenters()) {
            MCPServerResource resource = getMCPResource(documentName, rc);
            if (resource != null) {
                return resource;
            }
        }
        logger.warning("MCP resource not found in any resource center: " + documentName);
        return null;
    }


    protected MCPServer getMCPServer(String documentName) {
        MCPServerResource resource = getMCPResource(documentName);
        if (resource == null) {
            return null;
        }
        try {
            MCPServer result = resource.getResourceData();
            return result;
        } catch (RuntimeException e) {
            logger.severe("RuntimeException loading resource: " + resource.getURI() + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.severe("File not found for resource: " + resource.getURI() + " - " + e.getMessage());
            e.printStackTrace();
        } catch (ResourceLoadingCancelledException e) {
            logger.severe("Loading cancelled for resource: " + resource.getURI() + " - " + e.getMessage());
            e.printStackTrace();
        } catch (FlexoException e) {
            logger.severe("Error loading resource: " + resource.getURI() + " - " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.severe("Unexpected error loading resource: " + resource.getURI() + " - " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    protected static void logTestSection(String sectionName) {
        logger.info("============================================================");
        logger.info(sectionName);
        logger.info("============================================================");
    }
}