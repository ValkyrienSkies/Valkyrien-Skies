package org.valkyrienskies.addon.control.nodenetwork;

import gigaherz.graph.api.Graph;
import gigaherz.graph.api.GraphObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.nodenetwork.EnumWireType;
import org.valkyrienskies.fixes.VSNetwork;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

public class VSNode_TileEntity implements IVSNode {

    private final TileEntity parentTile;
    // No duplicate connections, use Set<Node> to guarantee this
    private final Set<BlockPos> linkedNodesPos;
    private final List<EnumWireType> linkedWireTypes;
    // A wrapper unmodifiable Set that allows external classes to see an immutable
    // version of linkedNodesPos.
    private final Set<BlockPos> immutableLinkedNodesPos;
    private final List<EnumWireType> immutableLinkedWireTypes;
    private final int maximumConnections;
    private boolean isValid;
    private PhysicsObject parentPhysicsObject;
    private Graph nodeGraph;
    private EnumWireType wireType;

    public VSNode_TileEntity(TileEntity parent, int maximumConnections) {
        this.parentTile = parent;
        this.linkedNodesPos = new HashSet<>();
        this.linkedWireTypes = new ArrayList<EnumWireType>();
        this.immutableLinkedNodesPos = Collections.unmodifiableSet(linkedNodesPos);
        this.immutableLinkedWireTypes = Collections.unmodifiableList(linkedWireTypes);
        this.isValid = false;
        this.parentPhysicsObject = null;
        this.maximumConnections = maximumConnections;
        this.wireType = EnumWireType.RELAY;
        Graph.integrate(this, Collections.EMPTY_LIST,
            (graph) -> new BasicNodeTileEntity.GraphData());
    }

    @Nullable
    @Deprecated
    public static IVSNode getVSNode_TileEntity(World world, BlockPos pos) {
        if (world == null || pos == null) {
            throw new IllegalArgumentException("Null arguments");
        }
        boolean isChunkLoaded = world.isBlockLoaded(pos);
        if (!isChunkLoaded) {
            // throw new IllegalStateException("VSNode_TileEntity wasn't loaded in the world!");
            return null;
        }
        TileEntity entity = world.getTileEntity(pos);
        if (entity == null) {
            return null;
            // throw new IllegalStateException("VSNode_TileEntity was null");
        }
        if (entity instanceof IVSNodeProvider) {
            IVSNode vsNode = ((IVSNodeProvider) entity).getNode();
            if (!vsNode.isValid()) {
                return null;
                // throw new IllegalStateException("IVSNode was not valid!");
            } else {
                return vsNode;
            }
        } else {
            return null;
            // throw new IllegalStateException("VSNode_TileEntity of different class");
        }
    }

    @Override
    public Iterable<IVSNode> getDirectlyConnectedNodes() {
        // assertValidity();
        List<IVSNode> nodesList = new ArrayList<IVSNode>();
        for (BlockPos pos : linkedNodesPos) {
            IVSNode node = getVSNode_TileEntity(getNodeWorld(), pos);
            if (node != null) {
                nodesList.add(node);
            }
        }
        return nodesList;
    }

    @Override
    public void makeConnection(IVSNode other, EnumWireType wireType) {
        assertValidity();
        boolean contains = linkedNodesPos.contains(other.getNodePos());
        if (!contains) {
            linkedNodesPos.add(other.getNodePos());
            linkedWireTypes.add(wireType);
            parentTile.markDirty();
            other.makeConnection(this, wireType);
            sendNodeUpdates();
            List stupid = Collections.singletonList(other);
            getGraph().addNeighours(this, stupid);
        }
    }

    @Override
    public void breakConnection(IVSNode other) {
        assertValidity();
        boolean contains = linkedNodesPos.contains(other.getNodePos());
        if (contains) {
            linkedNodesPos.remove(other.getNodePos());
            linkedWireTypes.remove(other.getWireType());
            parentTile.markDirty();
            other.breakConnection(this);
            sendNodeUpdates();
            try {
                // TODO: For some reason null graphs show up. Not sure why, but it seems safe to just ignore them.
                if (this.getGraph() != null) {
                    getGraph().removeNeighbour(this, other);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public BlockPos getNodePos() {
        assertValidity();
        return parentTile.getPos();
    }
    @Override
    public EnumWireType getWireType() {
        return this.wireType;
    }

    @Override
    public void validate() {
        isValid = true;
    }

    @Override
    public void invalidate() {
        isValid = false;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public World getNodeWorld() {
        return parentTile.getWorld();
    }

    @Override
    public Set<BlockPos> getLinkedNodesPos() {
        return immutableLinkedNodesPos;
    }

    @Override
    public List<EnumWireType> getLinkedWireTypes() {
        return immutableLinkedWireTypes;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        int[] data = new int[getLinkedNodesPos().size() * 4];
        int i = 0;
        int types = 0;
        for (BlockPos pos : getLinkedNodesPos()) {
            data[i++] = pos.getX();
            data[i++] = pos.getY();
            data[i++] = pos.getZ();
            data[i++] = linkedWireTypes.get(types++).ordinal();
        }
        compound.setIntArray(NBT_DATA_KEY, data);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        int[] data = compound.getIntArray(NBT_DATA_KEY);
        for (int i = 0; i < data.length; i += 4) {
            this.linkedNodesPos.add(new BlockPos(data[i], data[i + 1], data[i + 2]));
            this.linkedWireTypes.add(EnumWireType.values()[data[i + 3]]);
        }
    }

    @Override
    public PhysicsObject getPhysicsObject() {
        return parentPhysicsObject;
    }

    @Override
    public void sendNodeUpdates() {
        if (!this.getNodeWorld().isRemote) {
            // System.out.println("help");
            if (!parentTile.isInvalid()) {
                VSNetwork.sendTileToAllNearby(this.parentTile);
            }
        }
    }

    private void assertValidity() {
        if (!isValid()) {
            throw new IllegalStateException(
                "This node at " + parentTile.getPos() + " is not valid / initialized!");
        }
    }

    @Override
    public void shiftConnections(BlockPos offset) {
        if (isValid()) {
            throw new IllegalStateException(
                "Cannot shift the connections of a Node while it is valid and in use!");
        }
        List<BlockPos> shiftedNodesPos = new ArrayList<BlockPos>(linkedNodesPos.size());
        for (BlockPos originalPos : linkedNodesPos) {
            shiftedNodesPos.add(originalPos.add(offset));
        }
        linkedNodesPos.clear();
        linkedNodesPos.addAll(shiftedNodesPos);
    }

    @Override
    public void setParentPhysicsObject(PhysicsObject parent) {
        if (isValid()) {
            throw new IllegalStateException(
                "Cannot change the parent physics object of a Node while it is valid and in use!");
        }
        this.parentPhysicsObject = parent;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other instanceof VSNode_TileEntity) {
            VSNode_TileEntity otherNode = (VSNode_TileEntity) other;
            return otherNode.getNodePos().equals(this.getNodePos());
        } else {
            return false;
        }
    }

    @Override
    public Graph getGraph() {
        return nodeGraph;
    }

    @Override
    public void setGraph(Graph graph) {
        this.nodeGraph = graph;
    }

    private List<GraphObject> getNeighbors() {
        List<GraphObject> neighbors = new ArrayList<GraphObject>();
        for (BlockPos pos : getLinkedNodesPos()) {
            IVSNode node = getVSNode_TileEntity(this.getNodeWorld(), pos);
            if (node == null) {
                throw new IllegalStateException();
            }
            neighbors.add(node);
        }
        return neighbors;
    }

    @Override
    public List<GraphObject> getNeighbours() {
        List<GraphObject> nodesList = new ArrayList<GraphObject>();
        for (BlockPos pos : linkedNodesPos) {
            IVSNode node = getVSNode_TileEntity(getNodeWorld(), pos);
            if (node != null) {
                nodesList.add(node);
            }
        }
        return nodesList;
    }

    @Override
    public TileEntity getParentTile() {
        return this.parentTile;
    }

    @Override
    public int getMaximumConnections() {
        return maximumConnections;
    }
}
