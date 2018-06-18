package valkyrienwarfare.addon.control.nodenetwork;

import java.util.List;

import gigaherz.graph.api.Graph;
import gigaherz.graph.api.GraphObject;

public class VWThreadSafeGraph extends Graph {

	private List<GraphObject> cachedObjects;
	
	@Override
	public void remove(GraphObject object) {
		super.remove(object);
	}
}
