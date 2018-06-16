package valkyrienwarfare.addon.control.nodenetwork;

import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IVWNode {

	Iterable<IVWNode> getConnectedNodes();
	
	void makeConnection(IVWNode other);
	
	void breakConnection(IVWNode other);
	
	/**
	 * Mark this IVWNode as safe to use.
	 */
	void validate();
	
	/**
	 * Mark this IVWNode as unsafe to use.
	 */
	void invalidate();
	
	/**
	 * Returns true if the node is safe, false if it isn't.
	 * @return
	 */
	boolean isValid();
	
	/**
	 * Returns null if this node doesn't have a blockpos.
	 * @return
	 */
	BlockPos getNodePos();
	
	World getNodeWorld();
	
	Set<BlockPos> getImmutableLinkedNodesPos();
	
	void writeToNBT(NBTTagCompound compound);
	
	void readFromNBT(NBTTagCompound compound);
	
	void setIsNodeRelay(boolean isRelay);
	
	boolean isNodeRelay();
	
	/**
	 * Only used for internal purposes.
	 * @return
	 */
	@Deprecated
	Set<BlockPos> getLinkedNodesPosMutable();
	
	default void breakAllConnections() {
		for (IVWNode node : getConnectedNodes()) {
			breakConnection(node);
		}
	}

	default boolean canLinkToNode(IVWNode other) {
		return isNodeRelay() || other.isNodeRelay();
	}
	
	void sendNodeUpdates();
}
