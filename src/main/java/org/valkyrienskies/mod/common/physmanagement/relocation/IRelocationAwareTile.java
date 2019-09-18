package org.valkyrienskies.mod.common.physmanagement.relocation;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.coordinates.CoordinateSpaceType;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;

/**
 * Allows a TileEntity to intelligently copy themselves when being moved; for example, during ship
 * assembly and disassembly. If the TileEntity being copied doesn't implement this then it will be
 * copied using nbt. TODO: Move this to VS API.
 */
public interface IRelocationAwareTile {

    TileEntity createRelocatedTile(BlockPos newPos, ShipTransform shipTransform,
        CoordinateSpaceType coordinateSpaceType);
}
