package valkyrienwarfare.addon.control.nodenetwork;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Sets;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Creates a more dynamic version of the IVWNode but that only exists
 * internally, as to prevent any issues with saving and loading.
 * 
 * @author thebest108
 *
 */
public class SimpleVWGraph implements IVWGraph {

	private final Map<BlockPos, SimpleVWGraphNode> nodes;

	public static SimpleVWGraph createVWGraphWithStart(IVWNode start) {
		SimpleVWGraph graph = new SimpleVWGraph();
		graph.addNode(start);
		// Now we need to go through all the connections to fill this graph.
		return graph;
	}

	@Override
	public void addNode(IVWNode toAdd) {
		addNode(new SimpleVWGraphNode(toAdd));
	}

	private void addNode(SimpleVWGraphNode graphNode) {
		// If we already have this node, then do nothing.
		if (!nodes.containsKey(graphNode.realWorldNode.getNodePos())) {
			nodes.put(graphNode.realWorldNode.getNodePos(), graphNode);
			World nodeWorld = graphNode.realWorldNode.getNodeWorld();
			for (BlockPos connectionPos : graphNode.realWorldNode.getLinkedNodesPos()) {
				if (!nodes.containsKey(connectionPos)) {
					// Add it:
					// First get the node from the world
					IVWNode nodeFromPos = VWNode_TileEntity.getVWNode_TileEntity(nodeWorld, connectionPos);
					// Then put a wrapper on it
					SimpleVWGraphNode connectionNode = new SimpleVWGraphNode(nodeFromPos);
					// Then add the node to connections
					graphNode.connections.add(connectionNode);
					connectionNode.connections.add(graphNode);
					// Then add all the other connections to this node
					addNode(connectionNode);
				} else {
					// We already have it! Be sure to update the state of connections.
					SimpleVWGraphNode connectionNodeFromPos = nodes.get(connectionPos);
					graphNode.connections.add(connectionNodeFromPos);
					connectionNodeFromPos.connections.add(graphNode);
				}
			}
			System.out.println(nodes.size());
		}
		graphNode.realWorldNode.setNodeGraph(this);
	}

	@Override
	public void removeNode(IVWNode toRemove) {
		removeNode(new SimpleVWGraphNode(toRemove));
	}
	
	private void removeNode(SimpleVWGraphNode graphNode) {
		// If we already don't have this node, then theres nothing to remove.
		if (nodes.containsKey(graphNode.realWorldNode.getNodePos())) {
			
		} else {
			throw new IllegalStateException();
		}
	}

	private SimpleVWGraph() {
		// The physics thread demands concurrency!
		this.nodes = new ConcurrentHashMap<BlockPos, SimpleVWGraphNode>();
	}

	@Override
	public Iterable<IVWNode> getNodesInGraph() {
		return new Iterable<IVWNode>() {
			@Override
			public Iterator<IVWNode> iterator() {
				return new CustomIterator();
			}
		};
	}

	private class SimpleVWGraphNode {
		final IVWNode realWorldNode;
		final Set<SimpleVWGraphNode> connections;

		SimpleVWGraphNode(IVWNode node) {
			this.realWorldNode = node;
			this.connections = new HashSet<SimpleVWGraphNode>();
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (other instanceof SimpleVWGraphNode) {
				return SimpleVWGraphNode.class.cast(other).realWorldNode.equals(realWorldNode);
			} else {
				return false;
			}
		}

		// Use this for contains() functionality by the graph
		@Override
		public int hashCode() {
			return realWorldNode.getNodePos().hashCode();
		}
	}

	private class CustomIterator implements Iterator<IVWNode> {

		private Iterator<SimpleVWGraphNode> nodesIterator = nodes.values().iterator();

		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return nodesIterator.hasNext();
		}

		@Override
		public IVWNode next() {
			// TODO Auto-generated method stub
			return nodesIterator.next().realWorldNode;
		}
	}

}
