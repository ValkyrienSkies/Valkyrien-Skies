package valkyrienwarfare.addon.control.block.multiblocks;

import valkyrienwarfare.mod.coordinates.VectorImmutable;
import valkyrienwarfare.physics.management.PhysicsObject;

public class TileEntityGiantPropellerPart extends TileEntityMultiblockPartForce {

    @Override
    public double getMaxThrust() {
        return super.getMaxThrust();
    }

    @Override
    public VectorImmutable getForceOutputNormal(double secondsToApply, PhysicsObject physicsObject) {
        return null;
    }

    @Override
    public double getThrustMagnitude() {
        return 0;
    }

}
