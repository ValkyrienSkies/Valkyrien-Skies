package org.valkyrienskies.addon.control.block.multiblocks;

import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMultiblockSchematic {

    /**
     * This should generate the getStructureRelativeToCenter() list.
     */
    void initializeMultiblockSchematic(String schematicID);

    /**
     * Should return a static immutable list that represents how this multiblock is created.
     */
    List<BlockPosBlockPair> getStructureRelativeToCenter();

    /**
     * Returns the render bounding box tile entities should use while rendering this schematic.
     */
    default AxisAlignedBB getSchematicRenderBB(BlockPos masterPos) {
        double minX, minY, minZ, maxX, maxY, maxZ;
        minX = minY = minZ = Double.POSITIVE_INFINITY;
        maxX = maxY = maxZ = Double.NEGATIVE_INFINITY;
        for (BlockPosBlockPair pair : getStructureRelativeToCenter()) {
            double curX = pair.getPos()
                .getX() + masterPos.getX();
            double curY = pair.getPos()
                .getY() + masterPos.getY();
            double curZ = pair.getPos()
                .getZ() + masterPos.getZ();
            minX = Math.min(curX, minX);
            minY = Math.min(curY, minY);
            minZ = Math.min(curZ, minZ);
            maxX = Math.max(curX, maxX);
            maxY = Math.max(curY, maxY);
            maxZ = Math.max(curZ, maxZ);
        }
        return new AxisAlignedBB(minX - .5, minY - .5, minZ - .5, maxX + .5, maxY + .5, maxZ + .5);
    }

    /**
     * Returns a common schematic prefix for all multiblocks of this type.
     */
    String getSchematicPrefix();

    String getSchematicID();

    /**
     * Returns true if the multiblock was successfully created.
     */
    default boolean attemptToCreateMultiblock(World world, BlockPos pos) {
        if (getStructureRelativeToCenter().size() == 0) {
            throw new IllegalStateException("No structure info found in the multiblock schematic!");
        }

        boolean buildSuccessful = true;
        for (BlockPosBlockPair pair : getStructureRelativeToCenter()) {
            BlockPos realPos = pos.add(pair.getPos());
            IBlockState state = world.getBlockState(realPos);
            if (state.getBlock() != pair.getBlock()) {
                // This rotation didn't work
                buildSuccessful = false;
                break;
            } else {
                TileEntity tile = world.getTileEntity(realPos);
                if (tile instanceof ITileEntityMultiblockPart) {
                    ITileEntityMultiblockPart multiblockPart = (ITileEntityMultiblockPart) tile;
                    if (multiblockPart.isPartOfAssembledMultiblock()) {
                        // If its already a part of a multiblock then do not allow this to assemble.
                        buildSuccessful = false;
                        break;
                    }
                } else {
                    buildSuccessful = false;
                    break;
                }
            }
        }

        if (buildSuccessful) {
            for (BlockPosBlockPair pair : getStructureRelativeToCenter()) {
                BlockPos realPos = pos.add(pair.getPos());
                applyMultiblockCreation(world, realPos, pair.getPos());
            }
            return true;
        }

        return false;
    }

    void applyMultiblockCreation(World world, BlockPos tilePos, BlockPos relativePos);

    /**
     * Should only be called once by initialization code. Doesn't have any non static properties but
     * java doesn't allow static interface methods.
     * <p>
     * The order in which the schematics are in this list will be used as priority order for which
     * schematic variants are tested for first.
     */
    List<IMultiblockSchematic> generateAllVariants();

    default EnumMultiblockRotation getMultiblockRotation() {
        return EnumMultiblockRotation.NONE;
    }
}
