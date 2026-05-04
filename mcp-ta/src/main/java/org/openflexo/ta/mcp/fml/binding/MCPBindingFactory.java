package org.openflexo.ta.mcp.fml.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.foundation.fml.TechnologySpecificType;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterBindingFactory;
import org.openflexo.ta.mcp.model.MCPServer;
import org.openflexo.ta.mcp.model.MCPTool;


public final class MCPBindingFactory extends TechnologyAdapterBindingFactory {

    private static final Logger logger = Logger.getLogger(MCPBindingFactory.class.getPackage().getName());

    public MCPBindingFactory() {
        super();
    }

    @Override
    protected SimplePathElement<?> makeSimplePathElement(Object object, IBindingPathElement parent, Bindable bindable) {
        if (object instanceof MCPTool) {
            MCPTool tool = (MCPTool) object;
             logger.fine("makeSimplePathElement for MCPTool: " + tool.getName());
        }
        logger.warning("Unexpected object in makeSimplePathElement: " + object);
        return null;
    }



    @Override
    public boolean handleType(TechnologySpecificType<?> technologySpecificType) {
        if (technologySpecificType instanceof MCPServer) {
            return true;
        }
        if (technologySpecificType instanceof MCPTool) {
            return true;
        }
        return false;
    }

    @Override
    public List<? extends SimplePathElement<?>> getAccessibleSimplePathElements(
            IBindingPathElement parent, Bindable bindable) {

        List<SimplePathElement<?>> returned = new ArrayList<>();

         if (parent instanceof MCPServer) {
            MCPServer server = (MCPServer) parent;
            for (MCPTool tool : server.getTools()) {
                SimplePathElement<?> element = getSimplePathElement(tool, parent, bindable);
                if (element != null) {
                    returned.add(element);
                }
            }
        }

        return returned;
    }

    @Override
    public List<? extends FunctionPathElement<?>> getAccessibleFunctionPathElements(
            IBindingPathElement parent, Bindable bindable) {
        return Collections.emptyList();
    }
}