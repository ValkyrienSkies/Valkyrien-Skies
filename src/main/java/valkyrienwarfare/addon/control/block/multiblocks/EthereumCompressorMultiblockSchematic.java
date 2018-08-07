package valkyrienwarfare.addon.control.block.multiblocks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;

public class EthereumCompressorMultiblockSchematic implements IMulitblockSchematic {

	private final List<BlockPosBlockPair> structureRelativeToCenter;
	private int schematicID;
	
	public EthereumCompressorMultiblockSchematic() {
		this.structureRelativeToCenter = new ArrayList<BlockPosBlockPair>();
		this.schematicID = -1;
	}
	
	@Override
	public void registerMultiblockSchematic(int schematicID) {
		Block enginePart = ValkyrienWarfareControl.INSTANCE.vwControlBlocks.etherCompressorPanel;
		for (int x = 0; x <= 1; x++) {
			for (int y = 0; y <= 1; y++) {
				for (int z = 0; z <= 1; z++) {
					structureRelativeToCenter.add(new BlockPosBlockPair(new BlockPos(x, y, z), enginePart));
				}
			}
		}
		this.schematicID = schematicID;
	}

	@Override
	public List<BlockPosBlockPair> getStructureRelativeToCenter() {
		return structureRelativeToCenter;
	}

	@Override
	public int getSchematicID() {
		return this.schematicID;
	}

	@Override
	public void applyMultiblockCreation(World world, BlockPos tilePos, BlockPos relativePos, EnumMultiblockRotation rotation) {
		TileEntity tileEntity = world.getTileEntity(tilePos);
		if (!(tileEntity instanceof TileEntityEthereumCompressorPart)) {
			throw new IllegalStateException();
		}
		TileEntityEthereumCompressorPart enginePart = (TileEntityEthereumCompressorPart) tileEntity;
		enginePart.assembleMultiblock(this, rotation, relativePos);
	}

}
