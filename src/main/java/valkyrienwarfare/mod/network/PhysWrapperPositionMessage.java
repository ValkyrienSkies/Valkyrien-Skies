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

    private final PhysicsWrapperEntity toSpawn;
    public int entityID;
    public float posX, posY, posZ;
    public float pitch, yaw, roll;
    public Vector centerOfMass;
    public int relativeTick;

    public PhysWrapperPositionMessage() {
        toSpawn = null;
    }

    public PhysWrapperPositionMessage(PhysicsWrapperEntity toSend) {
        toSpawn = toSend;
        relativeTick = toSend.ticksExisted;
    }

    public PhysWrapperPositionMessage(PhysicsObject toRunLocally) {
        this();
        posX = (float) toRunLocally.wrapper.posX;
        posY = (float) toRunLocally.wrapper.posY;
        posZ = (float) toRunLocally.wrapper.posZ;

        pitch = (float) toRunLocally.wrapper.pitch;
        yaw = (float) toRunLocally.wrapper.yaw;
        roll = (float) toRunLocally.wrapper.roll;

        centerOfMass = toRunLocally.centerCoord;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityID = buf.readInt();
        relativeTick = buf.readInt();

        posX = buf.readFloat();
        posY = buf.readFloat();
        posZ = buf.readFloat();

        pitch = buf.readFloat();
        yaw = buf.readFloat();
        roll = buf.readFloat();

        centerOfMass = new Vector(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(toSpawn.getEntityId());
        buf.writeInt(relativeTick);

        buf.writeFloat((float) toSpawn.posX);
        buf.writeFloat((float) toSpawn.posY);
        buf.writeFloat((float) toSpawn.posZ);

        buf.writeFloat((float) toSpawn.pitch);
        buf.writeFloat((float) toSpawn.yaw);
        buf.writeFloat((float) toSpawn.roll);

        buf.writeFloat((float) toSpawn.wrapping.centerCoord.X);
        buf.writeFloat((float) toSpawn.wrapping.centerCoord.Y);
        buf.writeFloat((float) toSpawn.wrapping.centerCoord.Z);
    }

}
