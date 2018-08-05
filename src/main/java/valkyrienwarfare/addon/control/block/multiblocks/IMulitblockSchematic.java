package valkyrienwarfare.addon.control.block.multiblocks;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMulitblockSchematic {

	/**
	 * This should generate the getStructureRelativeToCenter() list.
	 * @param schematicID
	 */
	void registerMultiblockSchematic(int schematicID);
	
	/**
	 * Should return a static immutable list that represents how this multiblock is
	 * created.
	 * 
	 * @return
	 */
	List<BlockPosBlockPair> getStructureRelativeToCenter();
	
	/**
	 * This should get called after canCreateMultiblock() returns true.
	 * @param world
	 * @param pos
	 */
	void createMultiblock(World world, BlockPos pos);
	
	int getSchematicID();

	default boolean canCreateMultiblock(World world, BlockPos pos) {
		if (getStructureRelativeToCenter().size() == 0) {
			throw new IllegalStateException("No structure info found in the multiblock schematic!");
		}
		for (BlockPosBlockPair pair : getStructureRelativeToCenter()) {
			BlockPos relativePos = pos.add(pair.getPos());
			IBlockState state = world.getBlockState(relativePos);
			if (state.getBlock() != pair.getBlock()) {
				return false;
			}
		}
		return true;
	}
}
