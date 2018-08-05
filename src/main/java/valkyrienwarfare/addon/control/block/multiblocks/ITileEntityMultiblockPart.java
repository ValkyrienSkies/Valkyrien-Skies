package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.util.math.BlockPos;

public interface ITileEntityMultiblockPart {
	
	boolean isPartOfAssembledMultiblock();
	
	boolean isMaster();
	
	ITileEntityMultiblockPart getMaster();
	
	BlockPos getMultiblockOrigin();
	
	BlockPos getRelativePos();
	
	void dissembleMultiblock();
	
	void dissembleMultiblockLocal();

	void assembleMultiblock(IMulitblockSchematic schematic, EnumMultiblockRotation rotation, BlockPos relativePos);
	
	EnumMultiblockRotation getMultiblockRotation();
}
