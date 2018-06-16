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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node {

	private final TileEntity parentTile;
	private boolean isRelay;
	private byte channel = 0;
	private NodeNetwork parentNetwork;
	private PhysicsObject parentPhysicsObject;
	// No duplicate connections, use Set<Node> to guarantee this
	private Set<BlockPos> linkedNodesPos;
	private boolean nbtLoaded;

	public Node(TileEntity parent) {
		this.parentTile = parent;
		this.linkedNodesPos = new HashSet<BlockPos>();
		this.parentNetwork = new NodeNetwork(parentPhysicsObject);
		this.parentNetwork.getNetworkedNodes().add(this);
		this.isRelay = false;
		this.nbtLoaded = false;
	}

	public void updateParentEntity(PhysicsObject physObj) {
		parentPhysicsObject = physObj;
		parentNetwork.setParentPhysicsObject(physObj);
		if (physObj != null) {
			physObj.getConcurrentNodesWithinShip().add(this);
		}
	}

	public void linkNode(Node other) {
		linkedNodesPos.add(other.parentTile.getPos());
		other.linkedNodesPos.add(this.parentTile.getPos());
		parentNetwork.mergeWithNetworks(new NodeNetwork[] { other.parentNetwork });

		if (!parentTile.getWorld().isRemote) {
			sendUpdatesToNearby();
			other.sendUpdatesToNearby();
		}
	}

	public void unlinkNode(Node other, boolean updateNodeNetwork, boolean sendToClient) {
		linkedNodesPos.remove(other.parentTile.getPos());
		other.linkedNodesPos.remove(this.parentTile.getPos());

		if (updateNodeNetwork) {
			parentNetwork.recalculateNetworks(this);
		}

		if (sendToClient && !parentTile.getWorld().isRemote) {
			sendUpdatesToNearby();
			other.sendUpdatesToNearby();
		}
	}

	public void sendUpdatesToNearby() {
		Packet toSend = parentTile.getUpdatePacket();

		double xPos = parentTile.getPos().getX();
		double yPos = parentTile.getPos().getY();
		double zPos = parentTile.getPos().getZ();

		WorldServer serverWorld = (WorldServer) parentTile.getWorld();
		PlayerList list = serverWorld.mcServer.getPlayerList();
		// System.out.println("help");
		if (!parentTile.isInvalid()) {
			list.sendToAllNearExcept(null, xPos, yPos, zPos, 128D, serverWorld.provider.getDimension(), toSend);
		}
	}

	/**
	 * Destroys all other connections to this node from other nodes, and also calls
	 * for the node network to rebuild itself
	 */
	public void destroyNode() {
		List<BlockPos> connectedNodesCopy = new ArrayList<BlockPos>(linkedNodesPos);
		for (BlockPos pos : connectedNodesCopy) {
			TileEntity nodeProvider = parentTile.getWorld().getTileEntity(pos);
			if (nodeProvider != null) {
				Node node = INodeProvider.class.cast(nodeProvider).getNode();
				if (node != null) {
					unlinkNode(node, false, false);
				} else {
					linkedNodesPos.remove(pos);
				}
			} else {
				linkedNodesPos.remove(pos);
			}
		}
		parentNetwork.recalculateNetworks(this);

		if (parentPhysicsObject != null) {
			parentPhysicsObject.getConcurrentNodesWithinShip().remove(this);
		}
	}

	public PhysicsObject getPhysicsObject() {
		return parentPhysicsObject;
	}

	/**
	 * Returns true if the network input was the same as the network the node
	 * belonged to
	 *
	 * @param newNetwork
	 * @return
	 */
	public boolean updateParentNetwork(NodeNetwork newNetwork) {
		if (parentNetwork == newNetwork) {
			parentNetwork = newNetwork;
			return true;
		}
		parentNetwork = newNetwork;
		return false;
	}

	public NodeNetwork getNodeNetwork() {
		return parentNetwork;
	}

	public byte getChannel() {
		return channel;
	}

	public void setChannel(byte newChannel) {
		channel = newChannel;
	}

	public boolean canLinkToNode(Node other) {
		if (this.isNodeRelay() || other.isNodeRelay()) {
			return true;
		}
		return false;
	}

	public boolean isNodeRelay() {
		return isRelay;
	}

	public void setIsNodeRelay(boolean newVal) {
		isRelay = newVal;
	}

	public void readFromNBT(NBTTagCompound compound) {
		// TODO: This might not be correct
		linkedNodesPos.clear();

		int[] connectednodesarray = compound.getIntArray("connectednodesarray");

		for (int i = 0; i < connectednodesarray.length; i += 3) {
			BlockPos toAdd = new BlockPos(connectednodesarray[i], connectednodesarray[i + 1],
					connectednodesarray[i + 2]);
			linkedNodesPos.add(toAdd);
		}
		channel = compound.getByte("channel");
		setIsNodeRelay(compound.getBoolean("isRelay"));
		// The nbt has been loaded!
		this.nbtLoaded = true;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		int size = linkedNodesPos.size();
		// 3 ints for each BlockPos
		int[] arrayToWrite = new int[size * 3];
		int index = 0;
		for (BlockPos pos : linkedNodesPos) {
			arrayToWrite[index] = pos.getX();
			arrayToWrite[index + 1] = pos.getY();
			arrayToWrite[index + 2] = pos.getZ();
			index += 3;
		}
		compound.setIntArray("connectednodesarray", arrayToWrite);
		compound.setByte("channel", channel);
		compound.setBoolean("isRelay", isNodeRelay());

		return compound;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof Node) {
			Node otherNode = (Node) o;
			if (otherNode == this || otherNode.parentTile == parentTile
					|| otherNode.parentTile.getPos().equals(this.parentTile.getPos())) {
				return true;
			}
		}
		return false;
	}

	public TileEntity getParentTile() {
		return parentTile;
	}

	public Set<BlockPos> getConnectedNodesBlockPos() {
		return linkedNodesPos;
	}

	// TODO: Remove this.
	public Set<Node> getConnectedNodes() {
		Set<Node> connectedNodes = new HashSet<Node>();
		for (BlockPos pos : getConnectedNodesBlockPos()) {
			TileEntity tile = parentTile.getWorld().getTileEntity(pos);
			if (tile != null && tile instanceof INodeProvider) {
				INodeProvider provider = (INodeProvider) tile;
				Node node = provider.getNode();
				if (node != null) {
					connectedNodes.add(node);
				}
			}
		}
		return connectedNodes;
	}

	public void updateBuildState() {
		// Does nothing
	}
}
