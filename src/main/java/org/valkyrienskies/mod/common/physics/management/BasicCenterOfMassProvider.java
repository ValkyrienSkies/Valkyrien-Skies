package org.valkyrienskies.mod.common.physics.management;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.valkyrienskies.mod.common.physics.BlockPhysicsDetails;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.ShipInertiaData;

public class BasicCenterOfMassProvider implements IPhysicsObjectCenterOfMassProvider {

    private static final double INERTIA_OFFSET = .4D;

    private final ShipInertiaData inertiaData;

    public BasicCenterOfMassProvider(ShipInertiaData inertiaData) {
        this.inertiaData = inertiaData;
        if (inertiaData.getGameMoITensor() == null) {
            inertiaData.setGameMoITensor(new Matrix3d());
        }
    }

    @Override
    public void onSetBlockState(PhysicsObject physicsObject, BlockPos pos, IBlockState oldState, IBlockState newState) {
        World worldObj = physicsObject.getWorld();
        if (!newState.equals(oldState)) {
            double oldMass = BlockPhysicsDetails.getMassFromState(oldState, pos, worldObj);
            double newMass = BlockPhysicsDetails.getMassFromState(newState, pos, worldObj);
            double deltaMass = newMass - oldMass;
            // Don't change anything if the mass is the same
            if (Math.abs(deltaMass) > .00001) {
                double x = pos.getX() + .5;
                double y = pos.getY() + .5;
                double z = pos.getZ() + .5;

                deltaMass /= 9;
                addMassAt(physicsObject, x, y, z, deltaMass);
                addMassAt(physicsObject, x + INERTIA_OFFSET, y + INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(physicsObject, x + INERTIA_OFFSET, y + INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(physicsObject, x + INERTIA_OFFSET, y - INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(physicsObject, x + INERTIA_OFFSET, y - INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(physicsObject, x - INERTIA_OFFSET, y + INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(physicsObject, x - INERTIA_OFFSET, y + INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(physicsObject, x - INERTIA_OFFSET, y - INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(physicsObject, x - INERTIA_OFFSET, y - INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
            }
        }
    }

    private void addMassAt(PhysicsObject physicsObject, double x, double y, double z, double addedMass) {
        final org.valkyrienskies.mod.common.math.Vector gameTickCenterOfMass = inertiaData.getGameTickCenterOfMass();
        final Matrix3dc gameMoITensor = inertiaData.getGameMoITensor();
        org.valkyrienskies.mod.common.math.Vector prevCenterOfMass = new org.valkyrienskies.mod.common.math.Vector(
                gameTickCenterOfMass);
        if (inertiaData.getGameTickMass() > .0001) {
            gameTickCenterOfMass.multiply(inertiaData.getGameTickMass());
            gameTickCenterOfMass
                    .add(new org.valkyrienskies.mod.common.math.Vector(x, y, z).getProduct(addedMass));
            gameTickCenterOfMass.multiply(1.0 / (inertiaData.getGameTickMass() + addedMass));
        } else {
            inertiaData.setGameTickCenterOfMass(new org.valkyrienskies.mod.common.math.Vector(x, y, z));
            inertiaData.setGameMoITensor(new Matrix3d());
        }
        double cmShiftX = prevCenterOfMass.x - gameTickCenterOfMass.x;
        double cmShiftY = prevCenterOfMass.y - gameTickCenterOfMass.y;
        double cmShiftZ = prevCenterOfMass.z - gameTickCenterOfMass.z;
        double rx = x - gameTickCenterOfMass.x;
        double ry = y - gameTickCenterOfMass.y;
        double rz = z - gameTickCenterOfMass.z;


        Matrix3d copy = new Matrix3d(gameMoITensor);


        copy.m00 =
                gameMoITensor.m00() + (cmShiftY * cmShiftY + cmShiftZ * cmShiftZ) * inertiaData.getGameTickMass()
                        + (ry * ry + rz * rz) * addedMass;
        copy.m10 =
                gameMoITensor.m10() - cmShiftX * cmShiftY * inertiaData.getGameTickMass() - rx * ry * addedMass;
        copy.m20 =
                gameMoITensor.m20() - cmShiftX * cmShiftZ * inertiaData.getGameTickMass() - rx * rz * addedMass;
        copy.m01 = gameMoITensor.m10();
        copy.m11 =
                gameMoITensor.m11() + (cmShiftX * cmShiftX + cmShiftZ * cmShiftZ) * inertiaData.getGameTickMass()
                        + (rx * rx + rz * rz) * addedMass;
        copy.m21 =
                gameMoITensor.m21() - cmShiftY * cmShiftZ * inertiaData.getGameTickMass() - ry * rz * addedMass;
        copy.m02 = gameMoITensor.m20();
        copy.m12 = gameMoITensor.m21();
        copy.m22 =
                gameMoITensor.m22() + (cmShiftX * cmShiftX + cmShiftY * cmShiftY) * inertiaData.getGameTickMass()
                        + (rx * rx + ry * ry) * addedMass;


        inertiaData.setGameMoITensor(copy);

        // Do this to avoid a mass of zero, which runs the risk of dividing by zero and
        // crashing the program.
        if (inertiaData.getGameTickMass() + addedMass < .0001) {
            inertiaData.setGameTickMass(.0001);
            physicsObject.getData().setPhysicsEnabled(false);
        } else {
            inertiaData.setGameTickMass(inertiaData.getGameTickMass() + addedMass);
        }
    }

    @Override
    public boolean providesInertiaMatrix() {
        return true;
    }
}
