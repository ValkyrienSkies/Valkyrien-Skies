package valkyrienwarfare.addon.control.block.multiblocks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.MultiblockRegistry;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;

public class RudderAxleMultiblockSchematic implements IMulitblockSchematic {

	public static final int MIN_AXLE_LENGTH = 2;
	public static final int MAX_AXLE_LENGTH = 6;
	private final List<BlockPosBlockPair> structureRelativeToCenter;
	private String schematicID;
	private EnumMultiblockRotation multiblockRotation;
	private int axleLength;
	private EnumFacing axleDirection;
	
	public RudderAxleMultiblockSchematic() {
		this.structureRelativeToCenter = new ArrayList<BlockPosBlockPair>();
		this.schematicID = MultiblockRegistry.EMPTY_SCHEMATIC_ID;
		this.multiblockRotation = EnumMultiblockRotation.None;
		this.axleLength = -1;
		this.axleDirection = EnumFacing.UP;
	}
	
	@Override
	public void initializeMultiblockSchematic(String schematicID) {
		this.schematicID = schematicID;
	}

	@Override
	public List<BlockPosBlockPair> getStructureRelativeToCenter() {
		return structureRelativeToCenter;
	}

	@Override
	public String getSchematicPrefix() {
		return "multiblock_rudder_axle";
	}

	@Override
	public String getSchematicID() {
		return schematicID;
	}

	@Override
	public void applyMultiblockCreation(World world, BlockPos tilePos, BlockPos relativePos,
			EnumMultiblockRotation rotation) {
		TileEntity tileEntity = world.getTileEntity(tilePos);
		if (!(tileEntity instanceof TileEntityRudderAxlePart)) {
			throw new IllegalStateException();
		}
		TileEntityRudderAxlePart enginePart = (TileEntityRudderAxlePart) tileEntity;
		enginePart.assembleMultiblock(this, rotation, relativePos);
	}

	@Override
	public List<IMulitblockSchematic> generateAllVariants() {
		Block rudderAxelBlock = ValkyrienWarfareControl.INSTANCE.vwControlBlocks.rudderAxelPart;
		// Order matters here
		List<IMulitblockSchematic> variants = new ArrayList<IMulitblockSchematic>();
		for (int length = MAX_AXLE_LENGTH; length >= MIN_AXLE_LENGTH; length--) {
			for (EnumFacing possibleAxleDirection : EnumFacing.VALUES) {
				for (EnumMultiblockRotation multiblockRotation : EnumMultiblockRotation.values()) {
					BlockPos originPos = new BlockPos(0, 0, 0);
					RudderAxleMultiblockSchematic schematicVariant = new RudderAxleMultiblockSchematic();
					schematicVariant.initializeMultiblockSchematic(
							getSchematicPrefix() + "axel_direction:" + possibleAxleDirection.toString() + ":axel_len:" + length
									+ ":rot:" + multiblockRotation.toString());
					schematicVariant.multiblockRotation = multiblockRotation;
					schematicVariant.axleLength = length;
					schematicVariant.axleDirection = possibleAxleDirection;
					for (int i = 0; i < length; i++) {
						schematicVariant.structureRelativeToCenter
								.add(new BlockPosBlockPair(BlockPos.ORIGIN.offset(possibleAxleDirection, i), rudderAxelBlock));
					}
					variants.add(schematicVariant);
				}
			}
		}
		return variants;
	}

	@Override
	public EnumMultiblockRotation getMultiblockRotation() {
		return multiblockRotation;
	}
	
	public int getAxleLength() {
		return axleLength;
	}
	
	public EnumFacing getAxleDirection() {
		return axleDirection;
	}

}
