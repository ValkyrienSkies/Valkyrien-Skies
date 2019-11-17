/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mod.common.network;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Quaternion;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.multithreaded.PhysicsShipTransform;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import valkyrienwarfare.api.TransformType;

/**
 * This IMessage sends all the position rotation data of a PhysicsObject from the server to the
 * client. Usually the data sent from one of these packets is coming from the physics tick and isn't
 * exactly the same as the game tick; this is done so that the client can see ship movement smoothly
 * even when the server game tick is lagging.
 *
 * It also has code to apply it onto a PhysicsObject.
 *
 * @author thebest108
 */
@Accessors(fluent = false)
@Getter
@Setter
public class WrapperPositionMessage implements IMessage {

    private int relativeTick;
    private int entityID;
    private double posX, posY, posZ;
    private double pitch, yaw, roll;
    private Vector centerOfMass;
    private AxisAlignedBB shipBB;

    public WrapperPositionMessage() { }

    public WrapperPositionMessage(PhysicsShipTransform transformData, int entityID,
        int relativeTick) {
        this.setEntityID(entityID);
        this.setRelativeTick(relativeTick);
        this.setShipBB(transformData.getShipBoundingBox());
        this.setPosX(transformData.getPosX());
        this.setPosY(transformData.getPosY());
        this.setPosZ(transformData.getPosZ());
        this.setPitch(transformData.getPitch());
        this.setYaw(transformData.getYaw());
        this.setRoll(transformData.getRoll());
        this.setCenterOfMass(transformData.getCenterOfMass());
    }

    public WrapperPositionMessage(PhysicsWrapperEntity toSend, int relativeTick) {
        this.setEntityID(toSend.getEntityId());
        this.setRelativeTick(relativeTick);
        this.setShipBB(toSend.getPhysicsObject().shipBoundingBox());
        this.setPosX(toSend.posX);
        this.setPosY(toSend.posY);
        this.setPosZ(toSend.posZ);
        this.setPitch(toSend.getPitch());
        this.setYaw(toSend.getYaw());
        this.setRoll(toSend.getRoll());
        this.setCenterOfMass(toSend.getPhysicsObject().centerCoord());
    }

    public WrapperPositionMessage(PhysicsObject toRunLocally) {
        setPosX(toRunLocally.wrapperEntity().posX);
        setPosY(toRunLocally.wrapperEntity().posY);
        setPosZ(toRunLocally.wrapperEntity().posZ);

        setPitch(toRunLocally.wrapperEntity().getPitch());
        setYaw(toRunLocally.wrapperEntity().getYaw());
        setRoll(toRunLocally.wrapperEntity().getRoll());

        setCenterOfMass(toRunLocally.centerCoord());
        setShipBB(toRunLocally.shipBoundingBox());
    }

    public WrapperPositionMessage(WrapperPositionMessage wrapperMessage) {
        posX = wrapperMessage.getPosX();
        posY = wrapperMessage.getPosY();
        posZ = wrapperMessage.getPosZ();

        pitch = wrapperMessage.getPitch();
        yaw = wrapperMessage.getYaw();
        roll = wrapperMessage.getRoll();

        centerOfMass = wrapperMessage.getCenterOfMass();

        relativeTick = wrapperMessage.getRelativeTick();
        shipBB = wrapperMessage.getShipBB();
        entityID = wrapperMessage.getEntityID();
    }

    public WrapperPositionMessage(WrapperPositionMessage[] transformations,
        double[] weights) {
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

        this.centerOfMass = transformations[0].centerOfMass;
        this.relativeTick = -1;
        this.shipBB = transformations[0].shipBB;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        setEntityID(buf.readInt());
        setRelativeTick(buf.readInt());

        setPosX(buf.readDouble());
        setPosY(buf.readDouble());
        setPosZ(buf.readDouble());

        setPitch(buf.readDouble());
        setYaw(buf.readDouble());
        setRoll(buf.readDouble());

        setCenterOfMass(new Vector(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        setShipBB(new AxisAlignedBB(buf.readDouble(), buf.readDouble(), buf.readDouble(),
            buf.readDouble(),
            buf.readDouble(), buf.readDouble()));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(getEntityID());
        buf.writeInt(getRelativeTick());

        buf.writeDouble(getPosX());
        buf.writeDouble(getPosY());
        buf.writeDouble(getPosZ());

        buf.writeDouble(getPitch());
        buf.writeDouble(getYaw());
        buf.writeDouble(getRoll());

        buf.writeDouble(getCenterOfMass().X);
        buf.writeDouble(getCenterOfMass().Y);
        buf.writeDouble(getCenterOfMass().Z);

        buf.writeDouble(getShipBB().minX);
        buf.writeDouble(getShipBB().minY);
        buf.writeDouble(getShipBB().minZ);
        buf.writeDouble(getShipBB().maxX);
        buf.writeDouble(getShipBB().maxY);
        buf.writeDouble(getShipBB().maxZ);
    }

    /**
     * Apply this physics transform similar to how a vanilla boat would. Not the best solution, but
     * its simple and robust, and works well enough for now.
     *
     * @param physObj    The PhysicsObject to apply this transform to.
     * @param lerpFactor A number between 0 and 1, where 0 applies none of the transform and 1
     *                   applies all of it. A number around .7 or .8 is ideal here.
     */
    public void applySmoothLerp(PhysicsObject physObj, double lerpFactor) {
        Vector CMDif = centerOfMass.getSubtraction(physObj.centerCoord());
        physObj.shipTransformationManager().getCurrentTickTransform()
            .rotate(CMDif, TransformType.SUBSPACE_TO_GLOBAL);
        // CMDif.multiply(lerpFactor);

        physObj.wrapperEntity().posX -= CMDif.X;
        physObj.wrapperEntity().posY -= CMDif.Y;
        physObj.wrapperEntity().posZ -= CMDif.Z;

        physObj.wrapperEntity().lastTickPosX = physObj.wrapperEntity().posX;
        physObj.wrapperEntity().lastTickPosY = physObj.wrapperEntity().posY;
        physObj.wrapperEntity().lastTickPosZ = physObj.wrapperEntity().posZ;

        physObj.wrapperEntity().posX += (posX - physObj.wrapperEntity().posX) * lerpFactor;
        physObj.wrapperEntity().posY += (posY - physObj.wrapperEntity().posY) * lerpFactor;
        physObj.wrapperEntity().posZ += (posZ - physObj.wrapperEntity().posZ) * lerpFactor;

        // Create the quaternion for the old physics tick
        Quaternion prevRotation = physObj.shipTransformationManager().getCurrentTickTransform()
            .createRotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);

        // Create the quaternion for the next physics tick
        ShipTransform newRotationTransform = ShipTransform
            .createRotationTransform(pitch, yaw, roll);
        Quaternion newRotation = newRotationTransform
            .createRotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);

        // Interpolate between two based on the current time-step.
        Quaternion slerpedRotation = Quaternion
            .slerpInterpolate(prevRotation, newRotation, lerpFactor);
        double[] slerpedRotationAngles = slerpedRotation.toRadians();

        physObj.wrapperEntity()
            .setPhysicsEntityRotation(Math.toDegrees(slerpedRotationAngles[0]),
                Math.toDegrees(slerpedRotationAngles[1]), Math.toDegrees(slerpedRotationAngles[2]));

        physObj.centerCoord(centerOfMass);
        // Update the ship bounding box
        physObj.shipBoundingBox(shipBB);
    }
}
