package valkyrienwarfare.addon.control.block.multiblocks;

import valkyrienwarfare.addon.control.fuel.IEtherEngine;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.mod.common.coordinates.VectorImmutable;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;
import valkyrienwarfare.mod.common.util.ValkyrienUtils;

import java.util.Optional;

public class TileEntityEthereumCompressorPart extends TileEntityMultiblockPartForce<EthereumCompressorMultiblockSchematic, TileEntityEthereumCompressorPart> implements IEtherEngine {

    private static final VectorImmutable FORCE_NORMAL = new VectorImmutable(0, 1, 0);
    private double prevKeyframe;
    private double currentKeyframe;

    public TileEntityEthereumCompressorPart() {
        super();
    }

    public TileEntityEthereumCompressorPart(double maxThrust) {
        this();
        this.setMaxThrust(maxThrust);
        this.prevKeyframe = 0;
        this.currentKeyframe = 0;
    }

    @Override
    public void update() {
        super.update();
        prevKeyframe = currentKeyframe;
        currentKeyframe += 1.2;
        currentKeyframe = currentKeyframe % 99;
    }

    @Override
    public VectorImmutable getForceOutputNormal(double secondsToApply, PhysicsObject object) {
        return FORCE_NORMAL;
    }

    @Override
    public void setThrustMultiplierGoal(double thrustMultiplierGoal) {
        // TODO: Something is fundamentally wrong here.
        if (this.isMaster() || this.getMaster() == this) {
            super.setThrustMultiplierGoal(thrustMultiplierGoal);
        } else {
            this.getMaster()
                    .setThrustMultiplierGoal(thrustMultiplierGoal);
        }
    }

    @Override
    public double getThrustMagnitude() {
        if (this.isPartOfAssembledMultiblock() && this.getMaster() instanceof TileEntityEthereumCompressorPart) {
            return this.getMaxThrust() * this.getMaster()
                    .getThrustMultiplierGoal() * this.getCurrentEtherEfficiency();
        } else {
            return 0;
        }
    }

    @Override
    public double getCurrentEtherEfficiency() {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(world, pos);
        if (physicsObject.isPresent()) {
            Vector tilePos = new Vector(getPos().getX() + .5D, getPos().getY() + .5D, getPos().getZ() + .5D);
            physicsObject.get()
                    .getShipTransformationManager()
                    .getCurrentPhysicsTransform()
                    .transform(tilePos, TransformType.SUBSPACE_TO_GLOBAL);
            double yPos = tilePos.Y;
            return IEtherEngine.getEtherEfficiencyFromHeight(yPos);
        } else {
            return 1;
        }
    }

    public double getCurrentKeyframe(double partialTick) {
        double increment = currentKeyframe - prevKeyframe;
        if (increment < 0) {
            increment = (increment % 99) + 99;
        }
        return prevKeyframe + (increment * partialTick) + 1;
    }

}
