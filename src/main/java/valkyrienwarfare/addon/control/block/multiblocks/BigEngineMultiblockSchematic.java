package valkyrienwarfare.addon.control.block.multiblocks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;

public class BigEngineMultiblockSchematic implements IMulitblockSchematic {

	private final List<BlockPosBlockPair> structureRelativeToCenter;
	private int schematicID;
	
	public BigEngineMultiblockSchematic() {
		this.structureRelativeToCenter = new ArrayList<BlockPosBlockPair>();
		this.schematicID = -1;
	}
	
	@Override
	public void registerMultiblockSchematic(int schematicID) {
		Block enginePart = ValkyrienWarfareControl.INSTANCE.vwControlBlocks.bigEnginePart;
		for (int x = -1; x <= 1; x++) {
			for (int y = 0; y <= 1; y++) {
				for (int z = -1; z <= 0; z++) {
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
	public void createMultiblock(World world, BlockPos pos) {
		for (BlockPosBlockPair pair : getStructureRelativeToCenter()) {
			BlockPos realPos = pair.getPos().add(pos);
			TileEntity tileEntity = world.getTileEntity(realPos);
			if (!(tileEntity instanceof TileEntityBigEnginePart)) {
				throw new IllegalStateException();
			}
			TileEntityBigEnginePart enginePart = (TileEntityBigEnginePart) tileEntity;
			enginePart.assembleMultiblock(this, pair.getPos());
		}
		System.out.println("Success!");
	}

	@Override
	public int getSchematicID() {
		return this.schematicID;
	}

}
