/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.addon.control.nodenetwork;

import valkyrienwarfare.physicsmanagement.PhysicsObject;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A class that keeps track of all the nodes attached to a network; gets recalcuated upon a node being broken
 *
 * @author thebest108
 */
public class NodeNetwork {

	public final HashSet<Node> networkedNodes;
	private PhysicsObject parentEntity;

	public NodeNetwork(PhysicsObject parentEntity) {
		networkedNodes = new HashSet<Node>();
		this.parentEntity = parentEntity;
	}

	public NodeNetwork(HashSet<Node> backingSet, PhysicsObject parentEntity) {
		networkedNodes = backingSet;
		this.parentEntity = parentEntity;
	}

	public NodeNetwork(Node parent, PhysicsObject parentEntity) {
		this(parentEntity);
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
			NodeNetwork network = new NodeNetwork(nodeSet, parentEntity);

			for (Node nodeToUpdate : nodeSet) {
				nodeToUpdate.updateParentNetwork(network);
			}

//			System.out.println("New network of Size " + nodeSet.size());
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

//		System.out.println("New network of Size " + networkedNodes.size());
	}

	public PhysicsObject getParentPhysicsObject() {
		return parentEntity;
	}

	public void setParentPhysicsObject(PhysicsObject physObj) {
		parentEntity = physObj;
	}
}
