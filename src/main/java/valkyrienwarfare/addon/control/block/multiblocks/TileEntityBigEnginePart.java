package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.util.math.BlockPos;

public class TileEntityBigEnginePart extends TileEntityMultiblockPart {

	@Override
	public void assembleMultiblock(IMulitblockSchematic schematic, BlockPos relativePos) {
		this.isAssembled = true;
		this.isMaster = relativePos.equals(BlockPos.ORIGIN);
		this.offsetPos = relativePos;
		this.multiblockSchematic = schematic;
		this.sendUpdatePacketToAllNearby();
		this.markDirty();
	}

}
