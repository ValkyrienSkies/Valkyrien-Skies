package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.util.EnumFacing;
import valkyrienwarfare.mod.coordinates.VectorImmutable;
import valkyrienwarfare.physics.management.PhysicsObject;

public class TileEntityGiantPropellerPart extends TileEntityMultiblockPartForce {

    private double prevPropellerAngle;
    private double propellerAngle;

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

    @Override
    public void update() {
        this.prevPropellerAngle = this.propellerAngle;
        this.propellerAngle += 5;
    }

    public EnumFacing getPropellerFacing() {
        if (!this.isPartOfAssembledMultiblock()) {
            return null;
        }
        return ((GiantPropellerMultiblockSchematic) getMultiblockSchematic()).getPropellerFacing();
    }

    public int getPropellerRadius() {
        if (!this.isPartOfAssembledMultiblock()) {
            return 1;
        }
        return ((GiantPropellerMultiblockSchematic) getMultiblockSchematic()).getPropellerRadius();
    }

    public float getPropellerAngle(float partialTick) {
        return (float) (prevPropellerAngle + (propellerAngle - prevPropellerAngle) * partialTick);
    }

}
