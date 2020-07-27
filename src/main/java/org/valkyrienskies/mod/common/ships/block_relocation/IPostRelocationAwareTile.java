package org.valkyrienskies.mod.common.ships.block_relocation;

import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows a TileEntity to have additional behavior called after MoveBlocks creates a relocated copy of the tile.
 */
public interface IPostRelocationAwareTile {

    void postRelocation(@Nonnull BlockPos newPos, @Nonnull BlockPos oldPos, @Nullable PhysicsObject copiedBy);
}
