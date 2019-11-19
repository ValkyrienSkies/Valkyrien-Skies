package org.valkyrienskies.addon.control.block.multiblocks;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.MultiblockRegistry;
import org.valkyrienskies.addon.control.fuel.IValkyriumEngine;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

public class TileEntityValkyriumCompressorPart extends
    TileEntityMultiblockPartForce<ValkyriumCompressorMultiblockSchematic, TileEntityValkyriumCompressorPart> implements
    IValkyriumEngine {

    private static final VectorImmutable FORCE_NORMAL = new VectorImmutable(0, 1, 0);
    private double prevKeyframe;
    private double currentKeyframe;

    public TileEntityValkyriumCompressorPart() {
        super();
    }

    public TileEntityValkyriumCompressorPart(double maxThrust) {
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
        if (this.isPartOfAssembledMultiblock() && this
            .getMaster() instanceof TileEntityValkyriumCompressorPart) {
            return this.getMaxThrust() * this.getMaster()
                .getThrustMultiplierGoal() * this.getCurrentValkyriumEfficiency();
        } else {
            return 0;
        }
    }

    @Override
    public double getCurrentValkyriumEfficiency() {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(world, pos);
        if (physicsObject.isPresent()) {
            Vector tilePos = new Vector(getPos().getX() + .5D, getPos().getY() + .5D,
                getPos().getZ() + .5D);
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentPhysicsTransform()
                .transform(tilePos, TransformType.SUBSPACE_TO_GLOBAL);
            double yPos = tilePos.Y;
            return IValkyriumEngine.getValkyriumEfficiencyFromHeight(yPos);
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

    @Override
    public boolean attemptToAssembleMultiblock(World worldIn, BlockPos pos, EnumFacing facing) {
        List<IMultiblockSchematic> valkyriumEngineMultiblockSchematics = MultiblockRegistry.getSchematicsWithPrefix("multiblock_valkyrium_compressor");
        for (IMultiblockSchematic schematic : valkyriumEngineMultiblockSchematics) {
            if (schematic.attemptToCreateMultiblock(worldIn, pos)) {
                return true;
            }
        }
        return false;
    }
}
