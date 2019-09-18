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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.multithreaded.PhysicsShipTransform;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;

/**
 * This IMessage sends all the position rotation data of a PhysicsObject from the server to the
 * client. Usually the data sent from one of these packets is coming from the physics tick and isn't
 * exactly the same as the game tick; this is done so that the client can see ship movement smoothly
 * even when the server game tick is lagging.
 *
 * @author thebest108
 */
public class PhysWrapperPositionMessage implements IMessage {

    private int entityID;
    private double posX, posY, posZ;
    private double pitch, yaw, roll;
    private Vector centerOfMass;
    private int relativeTick;
    private AxisAlignedBB shipBB;

    public PhysWrapperPositionMessage() {
    }

    public PhysWrapperPositionMessage(PhysicsShipTransform transformData, int entityID,
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

    public PhysWrapperPositionMessage(PhysicsWrapperEntity toSend, int relativeTick) {
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

    public PhysWrapperPositionMessage(PhysicsObject toRunLocally) {
        setPosX(toRunLocally.wrapperEntity().posX);
        setPosY(toRunLocally.wrapperEntity().posY);
        setPosZ(toRunLocally.wrapperEntity().posZ);

        setPitch(toRunLocally.wrapperEntity().getPitch());
        setYaw(toRunLocally.wrapperEntity().getYaw());
        setRoll(toRunLocally.wrapperEntity().getRoll());

        setCenterOfMass(toRunLocally.centerCoord());
        setShipBB(toRunLocally.shipBoundingBox());
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
     * @return the posX
     */
    public double getPosX() {
        return posX;
    }

    /**
     * @param posX the posX to set
     */
    public void setPosX(double posX) {
        this.posX = posX;
    }

    /**
     * @return the posY
     */
    public double getPosY() {
        return posY;
    }

    /**
     * @param posY the posY to set
     */
    public void setPosY(double posY) {
        this.posY = posY;
    }

    /**
     * @return the posZ
     */
    public double getPosZ() {
        return posZ;
    }

    /**
     * @param posZ the posZ to set
     */
    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    /**
     * @return the pitch
     */
    public double getPitch() {
        return pitch;
    }

    /**
     * @param pitch the pitch to set
     */
    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    /**
     * @return the yaw
     */
    public double getYaw() {
        return yaw;
    }

    /**
     * @param yaw the yaw to set
     */
    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    /**
     * @return the roll
     */
    public double getRoll() {
        return roll;
    }

    /**
     * @param roll the roll to set
     */
    public void setRoll(double roll) {
        this.roll = roll;
    }

    /**
     * @return the centerOfMass
     */
    public Vector getCenterOfMass() {
        return centerOfMass;
    }

    /**
     * @param centerOfMass the centerOfMass to set
     */
    public void setCenterOfMass(Vector centerOfMass) {
        this.centerOfMass = centerOfMass;
    }

    /**
     * @return the relativeTick
     */
    public int getRelativeTick() {
        return relativeTick;
    }

    /**
     * @param relativeTick the relativeTick to set
     */
    public void setRelativeTick(int relativeTick) {
        this.relativeTick = relativeTick;
    }

    /**
     * @return the shipBB
     */
    public AxisAlignedBB getShipBB() {
        return shipBB;
    }

    /**
     * @param shipBB the shipBB to set
     */
    public void setShipBB(AxisAlignedBB shipBB) {
        this.shipBB = shipBB;
    }

    /**
     * @return the entityID
     */
    public int getEntityID() {
        return entityID;
    }

    /**
     * @param entityID the entityID to set
     */
    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

}