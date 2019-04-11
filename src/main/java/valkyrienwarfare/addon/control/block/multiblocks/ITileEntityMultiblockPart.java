package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.util.math.BlockPos;

public interface ITileEntityMultiblockPart<E extends IMulitblockSchematic, F extends ITileEntityMultiblockPart> {
	
	boolean isPartOfAssembledMultiblock();
	
	boolean isMaster();
	
	F getMaster();
	
	BlockPos getMultiblockOrigin();
	
	BlockPos getRelativePos();
	
	void dissembleMultiblock();
	
	void dissembleMultiblockLocal();

	void assembleMultiblock(E schematic, BlockPos relativePos);

	E getMultiBlockSchematic();

}
