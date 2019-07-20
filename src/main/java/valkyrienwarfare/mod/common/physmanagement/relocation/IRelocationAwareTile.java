package valkyrienwarfare.mod.common.physmanagement.relocation;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.mod.common.coordinates.CoordinateSpaceType;
import valkyrienwarfare.mod.common.coordinates.ShipTransform;

/**
 * Allows a TileEntity to intelligently copy themselves when being moved; for example, during ship assembly and disassembly. If the TileEntity being copied doesn't implement this then it will be copied using nbt.
 * TODO: Move this to VW API.
 */
public interface IRelocationAwareTile {

    TileEntity createRelocatedTile(BlockPos newPos, ShipTransform shipTransform, CoordinateSpaceType coordinateSpaceType);
}
