package org.valkyrienskies.mod.common.physics;

import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

public interface IPhysicsBlockController extends Comparable<IPhysicsBlockController> {

    int getPriority();

    void setPriority(int newPriority);

    /**
     * Does nothing by default, insert processor logic here
     */
    void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations,
        double secondsToSimulate);

    /**
     * Returns the position of the TileEntity that is behind this interface.
     */
    BlockPos getNodePos();

    // Used maintain order of which processors get called first. If both processors
    // have equal priorities, then we use the BlockPos as a tiebreaker.
    @Override
    default int compareTo(IPhysicsBlockController other) {
        if (getPriority() != other.getPriority()) {
            return getPriority() - other.getPriority();
        } else {
            // break the tie
            return getNodePos().compareTo(other.getNodePos());
        }
    }
}
