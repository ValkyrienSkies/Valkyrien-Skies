package valkyrienwarfare.addon.control.block.multiblocks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.MultiblockRegistry;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;

public class EthereumEngineMultiblockSchematic implements IMulitblockSchematic {

	private final List<BlockPosBlockPair> structureRelativeToCenter;
	private String schematicID;
	private EnumMultiblockRotation multiblockRotation;
	
	public EthereumEngineMultiblockSchematic() {
		this.structureRelativeToCenter = new ArrayList<BlockPosBlockPair>();
		this.schematicID = MultiblockRegistry.EMPTY_SCHEMATIC_ID;
		this.multiblockRotation = EnumMultiblockRotation.None;
	}
	
	@Override
	public void initializeMultiblockSchematic(String schematicID) {
		Block enginePart = ValkyrienWarfareControl.INSTANCE.vwControlBlocks.ethereumEnginePart;
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
	public String getSchematicID() {
		return this.schematicID;
	}

	@Override
	public void applyMultiblockCreation(World world, BlockPos tilePos, BlockPos relativePos, EnumMultiblockRotation rotation) {
		TileEntity tileEntity = world.getTileEntity(tilePos);
		if (!(tileEntity instanceof TileEntityEthereumEnginePart)) {
			throw new IllegalStateException();
		}
		TileEntityEthereumEnginePart enginePart = (TileEntityEthereumEnginePart) tileEntity;
		enginePart.assembleMultiblock(this, rotation, relativePos);
	}

	@Override
	public String getSchematicPrefix() {
		return "multiblock_ether_engine";
	}

	@Override
	public List<IMulitblockSchematic> generateAllVariants() {
		List<IMulitblockSchematic> variants = new ArrayList<IMulitblockSchematic>();
		
		for (EnumMultiblockRotation potentialRotation : EnumMultiblockRotation.values()) {
			EthereumEngineMultiblockSchematic variant = new EthereumEngineMultiblockSchematic();
			
			variant.initializeMultiblockSchematic(getSchematicPrefix() + ":rot:" + potentialRotation.toString());
			
			List<BlockPosBlockPair> rotatedPairs = new ArrayList<BlockPosBlockPair>();
			for (BlockPosBlockPair unrotatedPairs : variant.structureRelativeToCenter) {
				BlockPos rotatedPos = potentialRotation.rotatePos(unrotatedPairs.getPos());
				rotatedPairs.add(new BlockPosBlockPair(rotatedPos, unrotatedPairs.getBlock()));
			}
			variant.structureRelativeToCenter.clear();
			variant.structureRelativeToCenter.addAll(rotatedPairs);
			variant.multiblockRotation = potentialRotation;
			variants.add(variant);
		}
		// TODO Auto-generated method stub
		return variants;
	}
	
	@Override
	public EnumMultiblockRotation getMultiblockRotation() {
		return multiblockRotation;
	}

}
