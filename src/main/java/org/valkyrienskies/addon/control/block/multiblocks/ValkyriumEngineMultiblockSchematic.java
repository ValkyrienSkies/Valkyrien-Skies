package org.valkyrienskies.addon.control.block.multiblocks;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.MultiblockRegistry;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;

public class ValkyriumEngineMultiblockSchematic implements IMultiblockSchematic {

    private final List<BlockPosBlockPair> structureRelativeToCenter;
    private String schematicID;
    private EnumMultiblockRotation multiblockRotation;
    private BlockPos torqueOutputPos;
    private Vec3i torqueOutputDirection;

    public ValkyriumEngineMultiblockSchematic() {
        this.structureRelativeToCenter = new ArrayList<BlockPosBlockPair>();
        this.schematicID = MultiblockRegistry.EMPTY_SCHEMATIC_ID;
        this.multiblockRotation = EnumMultiblockRotation.NONE;
    }

    @Override
    public void initializeMultiblockSchematic(String schematicID) {
        Block enginePart = ValkyrienSkiesControl.INSTANCE.vsControlBlocks.valkyriumEnginePart;
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -1; z <= 0; z++) {
                    structureRelativeToCenter
                        .add(new BlockPosBlockPair(new BlockPos(x, y, z), enginePart));
                }
            }
        }
        this.torqueOutputPos = new BlockPos(1, 0, -1);
        this.torqueOutputDirection = new BlockPos(1, 0, 0);
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
    public void applyMultiblockCreation(World world, BlockPos tilePos, BlockPos relativePos) {
        TileEntity tileEntity = world.getTileEntity(tilePos);
        if (!(tileEntity instanceof TileEntityValkyriumEnginePart)) {
            throw new IllegalStateException();
        }
        TileEntityValkyriumEnginePart enginePart = (TileEntityValkyriumEnginePart) tileEntity;
        enginePart.assembleMultiblock(this, relativePos);
    }

    @Override
    public String getSchematicPrefix() {
        return "multiblock_valkyrium_engine";
    }

    @Override
    public List<IMultiblockSchematic> generateAllVariants() {
        List<IMultiblockSchematic> variants = new ArrayList<IMultiblockSchematic>();

        for (EnumMultiblockRotation potentialRotation : EnumMultiblockRotation.values()) {
            ValkyriumEngineMultiblockSchematic variant = new ValkyriumEngineMultiblockSchematic();

            variant.initializeMultiblockSchematic(
                getSchematicPrefix() + ":rot:" + potentialRotation.toString());

            List<BlockPosBlockPair> rotatedPairs = new ArrayList<BlockPosBlockPair>();
            for (BlockPosBlockPair unrotatedPairs : variant.structureRelativeToCenter) {
                BlockPos rotatedPos = potentialRotation.rotatePos(unrotatedPairs.getPos());
                rotatedPairs.add(new BlockPosBlockPair(rotatedPos, unrotatedPairs.getBlock()));
            }
            variant.structureRelativeToCenter.clear();
            variant.structureRelativeToCenter.addAll(rotatedPairs);
            variant.multiblockRotation = potentialRotation;
            variant.torqueOutputPos = potentialRotation.rotatePos(variant.torqueOutputPos);
            variant.torqueOutputDirection = potentialRotation
                .rotatePos((BlockPos) variant.torqueOutputDirection);
            variants.add(variant);
        }
        return variants;
    }

    @Override
    public EnumMultiblockRotation getMultiblockRotation() {
        return multiblockRotation;
    }


    public BlockPos getTorqueOutputPos() {
        return torqueOutputPos;
    }

    public Vec3i getTorqueOutputDirection() {
        return torqueOutputDirection;
    }

}
