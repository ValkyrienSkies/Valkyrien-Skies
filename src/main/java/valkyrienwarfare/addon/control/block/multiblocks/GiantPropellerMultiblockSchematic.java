package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.MultiblockRegistry;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;

import java.util.ArrayList;
import java.util.List;

public class GiantPropellerMultiblockSchematic implements IMulitblockSchematic {

    private final List<BlockPosBlockPair> structureRelativeToCenter;
    private String schematicID;
    private EnumMultiblockRotation multiblockRotation;
    private int propellerRadius;

    public GiantPropellerMultiblockSchematic() {
        this.structureRelativeToCenter = new ArrayList<BlockPosBlockPair>();
        this.schematicID = MultiblockRegistry.EMPTY_SCHEMATIC_ID;
        this.multiblockRotation = EnumMultiblockRotation.None;
    }

    @Override
    public void initializeMultiblockSchematic(String schematicID) {
        Block enginePart = ValkyrienWarfareControl.INSTANCE.vwControlBlocks.giantPropellerPart;
        for (int x = -propellerRadius; x <= propellerRadius; x++) {
            for (int y = -propellerRadius; y <= propellerRadius; y++) {
                structureRelativeToCenter.add(new BlockPosBlockPair(new BlockPos(x, y, 0), enginePart));
            }
        }
        this.schematicID = schematicID;
    }

    @Override
    public List<BlockPosBlockPair> getStructureRelativeToCenter() {
        return structureRelativeToCenter;
    }

    @Override
    public String getSchematicPrefix() {
        return "multiblock_giant_propeller";
    }

    @Override
    public String getSchematicID() {
        return this.schematicID;
    }

    @Override
    public void applyMultiblockCreation(World world, BlockPos tilePos, BlockPos relativePos, EnumMultiblockRotation rotation) {
        TileEntity tileEntity = world.getTileEntity(tilePos);
        if (!(tileEntity instanceof TileEntityGiantPropellerPart)) {
            throw new IllegalStateException();
        }
        System.out.println(tilePos);
        TileEntityGiantPropellerPart enginePart = (TileEntityGiantPropellerPart) tileEntity;
        enginePart.assembleMultiblock(this, rotation, relativePos);
    }

    @Override
    public List<IMulitblockSchematic> generateAllVariants() {
        List<IMulitblockSchematic> variants = new ArrayList<IMulitblockSchematic>();

        for (EnumMultiblockRotation potentialRotation : EnumMultiblockRotation.values()) {
            for (int radius = 3; radius >= 1; radius--) {
                GiantPropellerMultiblockSchematic variant = new GiantPropellerMultiblockSchematic();

                variant.propellerRadius = radius;
                variant.initializeMultiblockSchematic(getSchematicPrefix() + ":rot:" + potentialRotation.toString() + ":radius:" + radius);

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
        }
        return variants;
    }
}
