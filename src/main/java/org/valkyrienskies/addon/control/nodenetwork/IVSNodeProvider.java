package org.valkyrienskies.addon.control.nodenetwork;

import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.block_relocation.IPostRelocationAwareTile;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IVSNodeProvider extends IPostRelocationAwareTile {

    IVSNode getNode();

    /**
     * Shifts all of the internal state data, like connections to other nodes.
     */
    default void shiftInternalData(BlockPos offset) {
        getNode().shiftConnections(offset);
    }

    /**
     * This is the result from all of the graphing code. We specifically want nothing external to
     * interact with anything but this, to so we can easily replacing graph code.
     */
    Iterable<IVSNode> getNetworkedConnections();

    @Override
    default void postRelocation(@Nonnull BlockPos newPos, @Nonnull BlockPos oldPos, @Nullable PhysicsObject copiedBy) {
        shiftInternalData(newPos.subtract(oldPos));
        getNode().setParentPhysicsObject(copiedBy);
    }
}
