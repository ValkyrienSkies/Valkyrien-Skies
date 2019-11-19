package org.valkyrienskies.mod.common.physmanagement.relocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;

/**
 * Allows a TileEntity to intelligently copy themselves when being moved; for example, during ship
 * assembly and disassembly. If the TileEntity being copied doesn't implement this then it will be
 * copied using nbt. TODO: Move this to VS API.
 */
public interface IRelocationAwareTile {

    /**
     * Called by Valkyrien Skies when constructing or deconstructing a ship to allow a tile entity
     * to intelligently copy itself
     *
     * @param newPos   The position this tile is being copied into
     * @param copiedBy Null if being deconstructed, otherwise the ship that is copying this
     *                 TileEntity
     * @return The (relocated) version of this tile. <strong>MUST IMPLEMENT {@link
     * IRelocationAwareTile}</strong>
     */
    @Nonnull
    TileEntity createRelocatedTile(BlockPos newPos, @Nullable ShipData copiedBy);
}
