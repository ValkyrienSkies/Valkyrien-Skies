package org.valkyrienskies.mod.common.physics.management;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix3d;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.BlockPhysicsDetails;
import org.valkyrienskies.mod.common.ship_handling.ShipInertiaData;

public class BasicCenterOfMassProvider implements IPhysicsObjectCenterOfMassProvider {

    private static final double INERTIA_OFFSET = .4D;

    @Override
    public void onSetBlockState(ShipInertiaData inertiaData, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if (!newState.equals(oldState)) {
            double oldMass = BlockPhysicsDetails.getMassFromState(oldState);
            double newMass = BlockPhysicsDetails.getMassFromState(newState);
            double deltaMass = newMass - oldMass;
            // Don't change anything if the mass is the same
            if (Math.abs(deltaMass) > .00001) {
                double x = pos.getX() + .5;
                double y = pos.getY() + .5;
                double z = pos.getZ() + .5;

                deltaMass /= 9;
                addMassAt(inertiaData, x, y, z, deltaMass);
                addMassAt(inertiaData, x + INERTIA_OFFSET, y + INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(inertiaData, x + INERTIA_OFFSET, y + INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(inertiaData, x + INERTIA_OFFSET, y - INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(inertiaData, x + INERTIA_OFFSET, y - INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(inertiaData, x - INERTIA_OFFSET, y + INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(inertiaData, x - INERTIA_OFFSET, y + INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(inertiaData, x - INERTIA_OFFSET, y - INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(inertiaData, x - INERTIA_OFFSET, y - INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
            }
        }
    }

    /**
     * Updates the center of mass and rotation inertia tensor matrix of the ShipInertiaData, using the rigid body
     * inertia tensor equations. Reference http://www.kwon3d.com/theory/moi/triten.html eqs. 13 & 14.
     */
    private void addMassAt(ShipInertiaData inertiaData, double x, double y, double z, double addedMass) {
        double[] gameMoITensor = new double[9];
        Matrix3d transposed = inertiaData.getGameMoITensor().transpose(new Matrix3d());
        transposed.get(gameMoITensor);

        double gameTickMass = inertiaData.getGameTickMass();
        Vector prevCenterOfMass = new Vector(inertiaData.getGameTickCenterOfMass());
        if (gameTickMass > .0001D) {
            inertiaData.getGameTickCenterOfMass().multiply(gameTickMass);
            inertiaData.getGameTickCenterOfMass().add(new Vector(x, y, z).getProduct(addedMass));
            inertiaData.getGameTickCenterOfMass().multiply(1.0D / (gameTickMass + addedMass));
        } else {
            inertiaData.setGameTickCenterOfMass(new Vector(x, y, z));
            inertiaData.setGameMoITensor(new Matrix3d().zero());
        }

        // This code is pretty awful in hindsight, but it gets the job done.
        double cmShiftX = prevCenterOfMass.x - inertiaData.getGameTickCenterOfMass().x;
        double cmShiftY = prevCenterOfMass.y - inertiaData.getGameTickCenterOfMass().y;
        double cmShiftZ = prevCenterOfMass.z - inertiaData.getGameTickCenterOfMass().z;
        double rx = x - inertiaData.getGameTickCenterOfMass().x;
        double ry = y - inertiaData.getGameTickCenterOfMass().y;
        double rz = z - inertiaData.getGameTickCenterOfMass().z;

        gameMoITensor[0] = gameMoITensor[0] + (cmShiftY * cmShiftY + cmShiftZ * cmShiftZ) * gameTickMass
                + (ry * ry + rz * rz) * addedMass;
        gameMoITensor[1] = gameMoITensor[1] - cmShiftX * cmShiftY * gameTickMass - rx * ry * addedMass;
        gameMoITensor[2] = gameMoITensor[2] - cmShiftX * cmShiftZ * gameTickMass - rx * rz * addedMass;
        gameMoITensor[3] = gameMoITensor[1];
        gameMoITensor[4] = gameMoITensor[4] + (cmShiftX * cmShiftX + cmShiftZ * cmShiftZ) * gameTickMass
                + (rx * rx + rz * rz) * addedMass;
        gameMoITensor[5] = gameMoITensor[5] - cmShiftY * cmShiftZ * gameTickMass - ry * rz * addedMass;
        gameMoITensor[6] = gameMoITensor[2];
        gameMoITensor[7] = gameMoITensor[5];
        gameMoITensor[8] = gameMoITensor[8] + (cmShiftX * cmShiftX + cmShiftY * cmShiftY) * gameTickMass
                + (rx * rx + ry * ry) * addedMass;


        inertiaData.setGameMoITensor(new Matrix3d().set(gameMoITensor).transpose());

        // Do this to avoid a mass of zero, which runs the risk of dividing by zero and
        // crashing the program.
        if (inertiaData.getGameTickMass() + addedMass < .0001) {
            inertiaData.setGameTickMass(0);
        } else {
            inertiaData.setGameTickMass(inertiaData.getGameTickMass() + addedMass);
        }
    }

}
