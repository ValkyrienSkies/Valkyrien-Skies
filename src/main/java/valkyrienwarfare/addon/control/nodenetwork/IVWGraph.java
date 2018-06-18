package valkyrienwarfare.addon.control.nodenetwork;

/**
 * Keeps track of all nodes with a connection across them.
 * 
 * @author thebest108
 *
 */
public interface IVWGraph {

	/**
	 * Returns an Iterable<IVWNode> of all the nodes within this graph.
	 * 
	 * @return
	 */
	Iterable<IVWNode> getNodesInGraph();

	/**
	 * Adds the given node to the graph structure.
	 * @param toAdd
	 */
	void addNode(IVWNode toAdd);

	/**
	 * Sometimes this will need to split the graph, and when this happens new graphs
	 * are created to accommodate the unconnected nodes and those nodes must have
	 * their parent graph objects updated accordingly.
	 * 
	 * @param toRemove
	 */
	void removeNode(IVWNode toRemove);
}
