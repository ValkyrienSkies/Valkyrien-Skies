package org.valkyrienskies.addon.control.nodenetwork;

import gigaherz.graph.api.GraphObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

import java.util.List;
import java.util.Set;

/**
 * The nodes that form the graphs of control elements.
 *
 * @author thebest108, DeltaNedas
 */
public interface IVSNode extends GraphObject {

    String NBT_DATA_KEY = "VSNode_Tile_Data";

    /**
     * This does not return the full graph of connected nodes, just the ones that are directly
     * connected to this node.
     */
    Iterable<IVSNode> getDirectlyConnectedNodes();

    void makeConnection(IVSNode other, EnumWireType wireType);

    void breakConnection(IVSNode other);

    /**
     * Mark this IVSNode as safe to use.
     */
    void validate();

    /**
     * Mark this IVSNode as unsafe to use.
     */
    void invalidate();

    /**
     * Returns true if the node is safe, false if it isn't.
     */
    boolean isValid();

    /**
     * Returns null if this node doesn't have a blockpos.
     */
    BlockPos getNodePos();

    EnumWireType getWireType();

    World getNodeWorld();

    Set<BlockPos> getLinkedNodesPos();

    List<EnumWireType> getLinkedWireTypes();

    void writeToNBT(NBTTagCompound compound);

    void readFromNBT(NBTTagCompound compound);

    default void breakAllConnections() {
        for (IVSNode node : getDirectlyConnectedNodes()) {
            breakConnection(node);
        }
    }

    default boolean canLinkToOtherNode(IVSNode other) {
        return getLinkedNodesPos().size() < getMaximumConnections()
            && other.getLinkedNodesPos().size() < other.getMaximumConnections();
    }

    void sendNodeUpdates();

    /**
     * Can only be called while this node is invalid. Otherwise an IllegalStateException is thrown.
     */
    void shiftConnections(BlockPos offset);

    /**
     * Should only be called when after shiftConnections()
     */
    void setParentPhysicsObject(PhysicsObject parent);

    PhysicsObject getPhysicsObject();

    List<GraphObject> getNeighbours();

    TileEntity getParentTile();

    int getMaximumConnections();

    /**
     * @return True if the nodes are linked.
     */
    default boolean isLinkedToNode(IVSNode other) {
        return getLinkedNodesPos().contains(other.getNodePos());
    }
}
