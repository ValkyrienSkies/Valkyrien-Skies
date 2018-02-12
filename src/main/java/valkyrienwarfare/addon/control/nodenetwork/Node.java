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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.physics.management.PhysicsObject;

public class Node {

    private boolean isRelay;
    private boolean isFullyBuilt;
    private byte channel = 0;
    private NodeNetwork parentNetwork;
    private PhysicsObject parentPhysicsObject;
    // No duplicate connections, use Set<Node> to guarantee this
    private Set<Node> connectedNodes;
    private Set<BlockPos> connectedNodesBlockPos;
    private final TileEntity parentTile;

    public Node(TileEntity parent) {
        this.parentTile = parent;
        this.connectedNodes = new HashSet<Node>();
        this.connectedNodesBlockPos = new HashSet<BlockPos>();
        this.parentNetwork = new NodeNetwork(parentPhysicsObject);
        this.parentNetwork.getNetworkedNodes().add(this);
        this.isRelay = false;
        this.isFullyBuilt = false;
    }

    public void updateParentEntity(PhysicsObject physObj) {
        parentPhysicsObject = physObj;
        parentNetwork.setParentPhysicsObject(physObj);
        if (physObj != null) {
            physObj.nodesWithinShip.add(this);
        }
    }

    public void linkNode(Node other) {
        updateBuildState();
        connectedNodes.add(other);
        other.connectedNodes.add(this);
        connectedNodesBlockPos.add(other.parentTile.getPos());
        other.connectedNodesBlockPos.add(this.parentTile.getPos());
        parentNetwork.mergeWithNetworks(new NodeNetwork[] { other.parentNetwork });

        if (!parentTile.getWorld().isRemote) {
            sendUpdatesToNearby();
            other.sendUpdatesToNearby();
        }
    }

    public void unlinkNode(Node other, boolean updateNodeNetwork, boolean sendToClient) {
        updateBuildState();
        connectedNodes.remove(other);
        other.connectedNodes.remove(this);
        connectedNodesBlockPos.remove(other.parentTile.getPos());
        other.connectedNodesBlockPos.remove(this.parentTile.getPos());

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
        if (parentTile.getWorld().isRemote) {
            // This is needed because it never gets called anywhere else in the client code
            this.updateBuildState();
        }

        List<Node> connectedNodesCopy = new ArrayList<Node>(connectedNodes);
        for (Node node : connectedNodesCopy) {
            unlinkNode(node, false, false);
        }
        parentNetwork.recalculateNetworks(this);

        if (parentPhysicsObject != null) {
            parentPhysicsObject.nodesWithinShip.remove(this);
        }
        // Assume this gets handled by the tileentity.invalidate() method, otherwise
        // this won't work!
        // if(!parentTile.getWorld().isRemote){
        // sendUpdatesToNearby();
        // for(Object node : backingArray){
        // ((Node)node).sendUpdatesToNearby();
        // }
        // }
    }

    public PhysicsObject getPhysicsObject() {
        return parentPhysicsObject;
    }

    public void updateBuildState() {
        if (!isFullyBuilt) {
            isFullyBuilt = attemptToBuildNodeSet();
            if (!isFullyBuilt) {
                System.err.println("Node network building failed");
            } else {
                // System.out.println("Node network built successfully!");
            }
        }
    }

    /**
     * Return true if set was built fully, false if otherwise
     *
     * @return
     */
    public boolean attemptToBuildNodeSet() {
        List<BlockPos> toRemove = new ArrayList<BlockPos>();
        for (BlockPos pos : connectedNodesBlockPos) {
            if (parentTile.getWorld().isBlockLoaded(pos)) {
                boolean isLoaded = parentTile.getWorld().isBlockLoaded(pos);
                TileEntity tile = parentTile.getWorld().getTileEntity(pos);
                if (tile != null) {
                    if (tile instanceof INodeProvider) {
                        Node node = ((INodeProvider) tile).getNode();
                        if (node != null) {
                            connectedNodes.add(node);
                            node.connectedNodes.add(this);
                            parentNetwork.mergeWithNetworks(new NodeNetwork[] { node.parentNetwork });
                        }
                    }
                } else {
                    if (isLoaded) {
                        // Assume the node somehow died on its own
                        toRemove.add(pos);
                    }
                }
            }
        }
        // TODO: This used to call remove(), which is wrong, watch out for any errors
        // here.
        connectedNodesBlockPos.removeAll(toRemove);
        if (connectedNodes.size() == connectedNodesBlockPos.size()) {
            return true;
        }
        return false;
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
        connectedNodesBlockPos.clear();

        int[] connectednodesarray = compound.getIntArray("connectednodesarray");

        for (int i = 0; i < connectednodesarray.length; i += 3) {
            BlockPos toAdd = new BlockPos(connectednodesarray[i], connectednodesarray[i + 1],
                    connectednodesarray[i + 2]);
            connectedNodesBlockPos.add(toAdd);
        }
        channel = compound.getByte("channel");
        setIsNodeRelay(compound.getBoolean("isRelay"));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        int size = connectedNodesBlockPos.size();
        // 3 ints for each BlockPos
        int[] arrayToWrite = new int[size * 3];
        int index = 0;
        for (BlockPos pos : connectedNodesBlockPos) {
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

    public Set<Node> getConnectedNodes() {
        return connectedNodes;
    }

    public Set<BlockPos> getConnectedNodesBlockPos() {
        return connectedNodesBlockPos;
    }

}
