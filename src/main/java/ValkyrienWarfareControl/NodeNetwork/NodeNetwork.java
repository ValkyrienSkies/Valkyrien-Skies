package ValkyrienWarfareControl.NodeNetwork;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A class that keeps track of all the nodes attached to a network; gets recalcuated upon a node being broken
 *
 * @author thebest108
 */
public class NodeNetwork {

    public final HashSet<Node> networkedNodes;

    public NodeNetwork() {
        networkedNodes = new HashSet<Node>();
    }

    public NodeNetwork(HashSet<Node> backingSet) {
        networkedNodes = backingSet;
    }

    public NodeNetwork(Node parent) {
        this();
        networkedNodes.add(parent);
    }

    private static void fillWithConnections(Node start, HashSet<Node> toFill) {
        toFill.add(start);
        for (Node otherNodes : start.connectedNodes) {
            if (!toFill.contains(otherNodes)) {
                fillWithConnections(otherNodes, toFill);
            }
        }
    }

    /**
     * Removes the input node from the networks, and also replaces some of the node network refrences with a new network if they're now longer connected
     *
     * @param node
     */
    public void recalculateNetworks(Node node) {
        networkedNodes.remove(node);

        ArrayList<Node> networkedNodesCopy = new ArrayList<Node>(networkedNodes);

        networkedNodes.clear();

        ArrayList<HashSet<Node>> listOfHashSetsOfNodes = new ArrayList<HashSet<Node>>();

        while (!networkedNodesCopy.isEmpty()) {
            Node startPoint = networkedNodesCopy.get(0);
            HashSet<Node> fullConnection = new HashSet<Node>();
            fillWithConnections(startPoint, fullConnection);
            listOfHashSetsOfNodes.add(fullConnection);
            networkedNodesCopy.removeAll(fullConnection);
        }

        for (HashSet<Node> nodeSet : listOfHashSetsOfNodes) {
            NodeNetwork network = new NodeNetwork(nodeSet);

            for (Node nodeToUpdate : nodeSet) {
                nodeToUpdate.updateParentNetwork(network);
            }

//			System.out.println("New Network of Size " + nodeSet.size());
        }
    }

    /**
     * Merges the entire input merge into the first network
     *
     * @param networks
     * @return
     */
    public void mergeWithNetworks(NodeNetwork[] networks) {
        int totalSize = networks.length;

        for (int i = 0; i < totalSize; i++) {
            networkedNodes.addAll(networks[i].networkedNodes);
        }
        for (Node node : networkedNodes) {
            node.updateParentNetwork(this);
        }

//		System.out.println("New Network of Size " + networkedNodes.size());
    }
}
