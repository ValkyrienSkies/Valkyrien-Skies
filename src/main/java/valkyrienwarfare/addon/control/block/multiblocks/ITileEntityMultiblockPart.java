package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.util.math.BlockPos;

public interface ITileEntityMultiblockPart<E extends IMulitblockSchematic> {
	
	boolean isPartOfAssembledMultiblock();
	
	boolean isMaster();
	
	ITileEntityMultiblockPart getMaster();
	
	BlockPos getMultiblockOrigin();
	
	BlockPos getRelativePos();
	
	void dissembleMultiblock();
	
	void dissembleMultiblockLocal();

	void assembleMultiblock(E schematic, BlockPos relativePos);

	E getMultiBlockSchematic();

}
