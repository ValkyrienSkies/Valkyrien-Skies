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
        this.setShipBB(toSend.getPhysicsObject().getShipBoundingBox());
        this.setPosX(toSend.posX);
        this.setPosY(toSend.posY);
        this.setPosZ(toSend.posZ);
        this.setPitch(toSend.getPitch());
        this.setYaw(toSend.getYaw());
        this.setRoll(toSend.getRoll());
        this.setCenterOfMass(toSend.getPhysicsObject().getCenterCoord());
    }

    public WrapperPositionMessage(PhysicsObject toRunLocally) {
        setPosX(toRunLocally.getWrapperEntity().posX);
        setPosY(toRunLocally.getWrapperEntity().posY);
        setPosZ(toRunLocally.getWrapperEntity().posZ);

        setPitch(toRunLocally.getWrapperEntity().getPitch());
        setYaw(toRunLocally.getWrapperEntity().getYaw());
        setRoll(toRunLocally.getWrapperEntity().getRoll());

        setCenterOfMass(toRunLocally.getCenterCoord());
        setShipBB(toRunLocally.getShipBoundingBox());
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
        Vector CMDif = centerOfMass.getSubtraction(physObj.getCenterCoord());
        physObj.getShipTransformationManager().getCurrentTickTransform()
            .rotate(CMDif, TransformType.SUBSPACE_TO_GLOBAL);
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

        // Create the quaternion for the old physics tick
        Quaternion prevRotation = physObj.getShipTransformationManager().getCurrentTickTransform()
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

        physObj.getWrapperEntity()
            .setPhysicsEntityRotation(Math.toDegrees(slerpedRotationAngles[0]),
                Math.toDegrees(slerpedRotationAngles[1]), Math.toDegrees(slerpedRotationAngles[2]));

        physObj.setCenterCoord(centerOfMass);
        // Update the ship bounding box
        physObj.setShipBoundingBox(shipBB);
    }
}
