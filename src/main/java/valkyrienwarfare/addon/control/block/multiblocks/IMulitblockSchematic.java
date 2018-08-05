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
	
	int getSchematicID();

	default boolean createMultiblock(World world, BlockPos pos) {
		if (getStructureRelativeToCenter().size() == 0) {
			throw new IllegalStateException("No structure info found in the multiblock schematic!");
		}
		
		for (EnumMultiblockRotation potentialRotation : EnumMultiblockRotation.values()) {
			boolean buildSuccessful = true;
			for (BlockPosBlockPair pair : getStructureRelativeToCenter()) {
				BlockPos realPos = pos.add(potentialRotation.rotatePos(pair.getPos()));
				IBlockState state = world.getBlockState(realPos);
				if (state.getBlock() != pair.getBlock()) {
					// This rotation didn't work
					buildSuccessful = false;
					break;
				}
			}
			
			if (buildSuccessful) {
				for (BlockPosBlockPair pair : getStructureRelativeToCenter()) {
					BlockPos realPos = pos.add(potentialRotation.rotatePos(pair.getPos()));
					applyMultiblockCreation(world, realPos, potentialRotation.rotatePos(pair.getPos()), potentialRotation);
				}
				return true;
			}
		}
		
		return false;
	}
	
	void applyMultiblockCreation(World world, BlockPos tilePos, BlockPos relativePos, EnumMultiblockRotation rotation);
}
