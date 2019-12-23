package org.valkyrienskies.addon.control.nodenetwork;

import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

public interface INodeController extends Comparable<INodeController> {

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
    default int compareTo(INodeController other) {
        if (getPriority() != other.getPriority()) {
            return getPriority() - other.getPriority();
        } else {
            // break the tie
            return getNodePos().compareTo(other.getNodePos());
        }
    }
}
