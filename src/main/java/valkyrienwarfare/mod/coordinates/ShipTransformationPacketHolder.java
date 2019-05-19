/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mod.coordinates;

import net.minecraft.util.math.AxisAlignedBB;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Quaternion;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.network.PhysWrapperPositionMessage;
import valkyrienwarfare.physics.management.PhysicsObject;

/**
 * This class holds the information from a received PositionMessage, and has
 * code to apply it onto a PhysicsObject.
 *
 * @author thebest108
 */
public class ShipTransformationPacketHolder {

    private final int relativeTick;
    private final double posX, posY, posZ;
    private final double pitch, yaw, roll;
    private final Vector centerOfRotation;
    private final AxisAlignedBB shipBB;
    // The time stamp for this objects creation.
    private final long creationTimeNano;

    public ShipTransformationPacketHolder(PhysWrapperPositionMessage wrapperMessage) {
        posX = wrapperMessage.getPosX();
        posY = wrapperMessage.getPosY();
        posZ = wrapperMessage.getPosZ();

        pitch = wrapperMessage.getPitch();
        yaw = wrapperMessage.getYaw();
        roll = wrapperMessage.getRoll();

        centerOfRotation = wrapperMessage.getCenterOfMass();

        relativeTick = wrapperMessage.getRelativeTick();
        shipBB = wrapperMessage.getShipBB();
        // System.out.println(wrapperMessage.shipBB);
        creationTimeNano = System.nanoTime();
    }

    public ShipTransformationPacketHolder(ShipTransformationPacketHolder[] transformations, double[] weights) {
        double x = 0;
        double y = 0;
        double z = 0;

        for (int i = 0; i < transformations.length; i++) {
            // Vector centerOfRotationDif =
            // transformations[0].centerOfRotation.getSubtraction(transformations[i].centerOfRotation);
            x += weights[i] * transformations[i].posX;
            y += weights[i] * transformations[i].posY;
            z += weights[i] * transformations[i].posZ;
        }

        this.posX = x; // transformations[0].posX;
        this.posY = y; // transformations[0].posY;
        this.posZ = z; // transformations[0].posZ;

        // System.out.println(Arrays.toString(weights));

        for (int i = 0; i < transformations.length; i++) {

            // quaternions[i] =
        }

        this.pitch = transformations[0].pitch;
        this.yaw = transformations[0].yaw;
        this.roll = transformations[0].roll;

        this.centerOfRotation = transformations[0].centerOfRotation;
        this.relativeTick = -1;
        this.shipBB = transformations[0].shipBB;
        creationTimeNano = -1;
    }

    /**
     * Apply this physics transform similar to how a vanilla boat would. Not the
     * best solution, but its simple and robust, and works well enough for now.
     *
     * @param physObj    The PhysicsObject to apply this transform to.
     * @param lerpFactor A number between 0 and 1, where 0 applies none of the transform
     *                   and 1 applies all of it. A number around .7 or .8 is ideal here.
     */
    public void applySmoothLerp(PhysicsObject physObj, double lerpFactor) {
        Vector CMDif = centerOfRotation.getSubtraction(physObj.getCenterCoord());
        physObj.getShipTransformationManager().getCurrentTickTransform().rotate(CMDif, TransformType.SUBSPACE_TO_GLOBAL);
        // CMDif.multiply(lerpFactor);

        physObj.getWrapperEntity().posX -= CMDif.X;
        physObj.getWrapperEntity().posY -= CMDif.Y;
        physObj.getWrapperEntity().posZ -= CMDif.Z;

        physObj.getWrapperEntity().lastTickPosX = physObj.getWrapperEntity().posX;
        physObj.getWrapperEntity().lastTickPosY = physObj.getWrapperEntity().posY;
        physObj.getWrapperEntity().lastTickPosZ = physObj.getWrapperEntity().posZ;

        physObj.getWrapperEntity().posX += (posX - physObj.getWrapperEntity().posX) * lerpFactor;
        physObj.getWrapperEntity().posY += (posY - physObj.getWrapperEntity().posY) * lerpFactor;
        physObj.getWrapperEntity().posZ += (posZ - physObj.getWrapperEntity().posZ) * lerpFactor;

        Quaternion prevRotation = physObj.getShipTransformationManager().getCurrentTickTransform()
                .createRotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        Quaternion newRotation = Quaternion.fromEuler(pitch, yaw, roll);
        Quaternion lerpedRotation = Quaternion.slerpInterpolate(prevRotation, newRotation, lerpFactor);
        double[] lerpedRotationAngles = lerpedRotation.toRadians();

        physObj.getWrapperEntity().setPhysicsEntityRotation(Math.toDegrees(lerpedRotationAngles[0]), Math.toDegrees(lerpedRotationAngles[1]), Math.toDegrees(lerpedRotationAngles[2]));

        physObj.setCenterCoord(centerOfRotation);

        // Create an AABB that contains both the previous AABB and the new one, bigger
        // is always better!
        AxisAlignedBB expandedBB = physObj.getShipBoundingBox().union(shipBB);
        physObj.setShipBoundingBox(expandedBB);
    }
}
