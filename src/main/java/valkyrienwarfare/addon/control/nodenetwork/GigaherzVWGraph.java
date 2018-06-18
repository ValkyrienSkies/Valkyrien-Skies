package valkyrienwarfare.addon.control.nodenetwork;

import gigaherz.graph.api.Graph;

/**
 * Basically just a compatibility layer between gigaherz's graphs and VW code.
 * 
 * @author thebest108
 *
 */
public class GigaherzVWGraph implements IVWGraph {

	Graph backingGraph = new Graph();
	
	@Override
	public Iterable<IVWNode> getNodesInGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addNode(IVWNode toAdd) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeNode(IVWNode toRemove) {
		// TODO Auto-generated method stub

	}

}
