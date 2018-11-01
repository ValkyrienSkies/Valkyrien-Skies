package valkyrienwarfare.addon.control.block.multiblocks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMulitblockSchematic {

	/**
	 * This should generate the getStructureRelativeToCenter() list.
	 * @param schematicID
	 */
	void initializeMultiblockSchematic(String schematicID);
	
	/**
	 * Should return a static immutable list that represents how this multiblock is
	 * created.
	 * 
	 * @return
	 */
	List<BlockPosBlockPair> getStructureRelativeToCenter();
	
	/**
	 * Returns a common schematic prefix for all multiblocks of this type.
	 * @return
	 */
	String getSchematicPrefix();
	
	String getSchematicID();

	/**
	 * Returns true if the multiblock was successfully created.
	 * @param world
	 * @param pos
	 * @return
	 */
	default boolean attemptToCreateMultiblock(World world, BlockPos pos) {
		if (getStructureRelativeToCenter().size() == 0) {
			throw new IllegalStateException("No structure info found in the multiblock schematic!");
		}
		
		boolean buildSuccessful = true;
		for (BlockPosBlockPair pair : getStructureRelativeToCenter()) {
			BlockPos realPos = pos.add(pair.getPos());
			IBlockState state = world.getBlockState(realPos);
			if (state.getBlock() != pair.getBlock()) {
				// This rotation didn't work
				buildSuccessful = false;
				break;
			} else {
				TileEntity tile = world.getTileEntity(realPos);
				if (tile instanceof ITileEntityMultiblockPart) {
					ITileEntityMultiblockPart multiblockPart = (ITileEntityMultiblockPart) tile;
					if (multiblockPart.isPartOfAssembledMultiblock()) {
						// If its already a part of a multiblock then do not allow this to assemble.
						buildSuccessful = false;
						break;
					}
				} else {
					buildSuccessful = false;
					break;
				}
			}
		}
		
		if (buildSuccessful) {
			for (BlockPosBlockPair pair : getStructureRelativeToCenter()) {
				BlockPos realPos = pos.add(pair.getPos());
				applyMultiblockCreation(world, realPos, pair.getPos(), getMultiblockRotation());
			}
			return true;
		}
		
		return false;
	}
	
	void applyMultiblockCreation(World world, BlockPos tilePos, BlockPos relativePos, EnumMultiblockRotation rotation);

	/**
	 * Should only be called once by initialization code. Doesn't have any non
	 * static properties but java doesn't allow static interface methods.
	 * 
	 * The order in which the schematics are in this list will be used as priority
	 * order for which schematic variants are tested for first.
	 * 
	 * @return
	 */
	List<IMulitblockSchematic> generateAllVariants();

	default EnumMultiblockRotation getMultiblockRotation() {
		return EnumMultiblockRotation.None;
	}
}
