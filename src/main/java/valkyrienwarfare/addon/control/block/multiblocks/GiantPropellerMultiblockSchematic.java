package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.MultiblockRegistry;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;

import java.util.ArrayList;
import java.util.List;

public class GiantPropellerMultiblockSchematic implements IMulitblockSchematic {

    private final List<BlockPosBlockPair> structureRelativeToCenter;
    private String schematicID;
    private int propellerRadius;
    private EnumFacing propellerFacing;

    public GiantPropellerMultiblockSchematic() {
        this.structureRelativeToCenter = new ArrayList<BlockPosBlockPair>();
        this.schematicID = MultiblockRegistry.EMPTY_SCHEMATIC_ID;
        this.propellerFacing = EnumFacing.NORTH;
    }

    @Override
    public void initializeMultiblockSchematic(String schematicID) {
        Block enginePart = ValkyrienWarfareControl.INSTANCE.vwControlBlocks.giantPropellerPart;

        Vec3i perpAxisOne = null;
        Vec3i perpAxisTwo = null;
        switch (propellerFacing.getAxis()) {
            case X:
                perpAxisOne = new Vec3i(0, 1, 0);
                perpAxisTwo = new Vec3i(0, 0, 1);
                break;
            case Y:
                perpAxisOne = new Vec3i(1, 0, 0);
                perpAxisTwo = new Vec3i(0, 0, 1);
                break;
            case Z:
                perpAxisOne = new Vec3i(1, 0, 0);
                perpAxisTwo = new Vec3i(0, 1, 0);
                break;
        }

        for (int x = -propellerRadius; x <= propellerRadius; x++) {
            for (int y = -propellerRadius; y <= propellerRadius; y++) {
                int relativeX = (perpAxisOne.getX() * x) + (perpAxisTwo.getX() * y);
                int relativeY = (perpAxisOne.getY() * x) + (perpAxisTwo.getY() * y);
                int relativeZ = (perpAxisOne.getZ() * x) + (perpAxisTwo.getZ() * y);
                structureRelativeToCenter.add(new BlockPosBlockPair(new BlockPos(relativeX, relativeY, relativeZ), enginePart));
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
        TileEntityGiantPropellerPart enginePart = (TileEntityGiantPropellerPart) tileEntity;
        enginePart.assembleMultiblock(this, rotation, relativePos);
    }

    @Override
    public List<IMulitblockSchematic> generateAllVariants() {
        List<IMulitblockSchematic> variants = new ArrayList<IMulitblockSchematic>();

        for (EnumFacing variantPropellerFacing : EnumFacing.values()) {
            for (int radius = 3; radius >= 1; radius--) {
                GiantPropellerMultiblockSchematic variant = new GiantPropellerMultiblockSchematic();

                variant.propellerRadius = radius;
                variant.propellerFacing = variantPropellerFacing;
                variant.initializeMultiblockSchematic(getSchematicPrefix() + ":facing:" + variantPropellerFacing.toString() + ":radius:" + radius);

                variants.add(variant);
            }
        }
        return variants;
    }

    public EnumFacing getPropellerFacing() {
        return propellerFacing;
    }

    public int getPropellerRadius() {
        return propellerRadius;
    }
}
