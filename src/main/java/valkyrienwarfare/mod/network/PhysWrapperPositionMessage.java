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

package valkyrienwarfare.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class PhysWrapperPositionMessage implements IMessage {

    public PhysicsWrapperEntity toSpawn;

    public int entityID;
    public double posX, posY, posZ;
    public double pitch, yaw, roll;
    public Vector centerOfMass;
    public int relativeTick;

    public PhysWrapperPositionMessage() {
    }

    public PhysWrapperPositionMessage(PhysicsWrapperEntity toSend, int relativeTick) {
        toSpawn = toSend;
        this.relativeTick = relativeTick;
    }

    public PhysWrapperPositionMessage(PhysicsObject toRunLocally) {
        posX = toRunLocally.wrapper.posX;
        posY = toRunLocally.wrapper.posY;
        posZ = toRunLocally.wrapper.posZ;

        pitch = toRunLocally.wrapper.pitch;
        yaw = toRunLocally.wrapper.yaw;
        roll = toRunLocally.wrapper.roll;

        centerOfMass = toRunLocally.centerCoord;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityID = buf.readInt();
        relativeTick = buf.readInt();

        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();

        pitch = buf.readDouble();
        yaw = buf.readDouble();
        roll = buf.readDouble();

        centerOfMass = new Vector(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(toSpawn.getEntityId());
        buf.writeInt(relativeTick);

        buf.writeDouble(toSpawn.posX);
        buf.writeDouble(toSpawn.posY);
        buf.writeDouble(toSpawn.posZ);

        buf.writeDouble(toSpawn.pitch);
        buf.writeDouble(toSpawn.yaw);
        buf.writeDouble(toSpawn.roll);

        buf.writeDouble(toSpawn.wrapping.centerCoord.X);
        buf.writeDouble(toSpawn.wrapping.centerCoord.Y);
        buf.writeDouble(toSpawn.wrapping.centerCoord.Z);
    }

}