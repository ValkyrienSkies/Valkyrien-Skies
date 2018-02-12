/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.addon.control.nodenetwork;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import valkyrienwarfare.physics.management.PhysicsObject;

/**
 * A class that keeps track of all the nodes attached to a network; gets
 * recalcuated upon a node being broken
 *
 * @author thebest108
 */
public class NodeNetwork {

    private final Set<Node> networkedNodes;
    private PhysicsObject parentEntity;

    public NodeNetwork(Set<Node> backingSet, PhysicsObject parentEntity) {
        this.networkedNodes = backingSet;
        this.parentEntity = parentEntity;
    }

    public NodeNetwork(PhysicsObject parentEntity) {
        this(new HashSet<Node>(), parentEntity);
    }

    /**
     * Removes the input node from the networks, and also replaces some of the node
     * network references with a new network if they're now longer connected
     *
     * @param node
     */
    public void recalculateNetworks(Node node) {
        networkedNodes.remove(node);
        List<Node> networkedNodesCopy = new ArrayList<Node>(networkedNodes);
        networkedNodes.clear();

        List<Set<Node>> listOfHashSetsOfNodes = new ArrayList<Set<Node>>();
        while (!networkedNodesCopy.isEmpty()) {
            Node startPoint = networkedNodesCopy.get(0);
            Set<Node> fullConnection = new HashSet<Node>();
            fillWithConnections(startPoint, fullConnection);
            listOfHashSetsOfNodes.add(fullConnection);
            networkedNodesCopy.removeAll(fullConnection);
        }

        for (Set<Node> nodeSet : listOfHashSetsOfNodes) {
            NodeNetwork network = new NodeNetwork(nodeSet, parentEntity);
            for (Node nodeToUpdate : nodeSet) {
                nodeToUpdate.updateParentNetwork(network);
            }
            // System.out.println("New network of Size " + nodeSet.size());
        }
    }

    /**
     * Merges the entire input merge into the first network
     *
     * @param networks
     * @return
     */
    public void mergeWithNetworks(NodeNetwork[] networks) {
        for (NodeNetwork network : networks) {
            networkedNodes.addAll(network.networkedNodes);
        }
        for (Node node : networkedNodes) {
            node.updateParentNetwork(this);
        }
        // System.out.println("New network of Size " + networkedNodes.size());
    }

    /**
     * Ideally this wouldn't exist because parentEntity would be final, however when
     * loading in from NBT it takes a while for the ship entity to be fully loaded,
     * which occurs after the network. So unfortunately this method has to exist.
     * 
     * @param physObj
     */
    public void setParentPhysicsObject(PhysicsObject physObj) {
        parentEntity = physObj;
    }

    public PhysicsObject getParentPhysicsObject() {
        return parentEntity;
    }

    public Set<Node> getNetworkedNodes() {
        return networkedNodes;
    }
    
    private static void fillWithConnections(Node start, Set<Node> toFill) {
        toFill.add(start);
        for (Node otherNodes : start.getConnectedNodes()) {
            if (!toFill.contains(otherNodes)) {
                fillWithConnections(otherNodes, toFill);
            }
        }
    }
}
