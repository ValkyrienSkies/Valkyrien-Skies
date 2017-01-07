package ValkyrienWarfareBase.PhysicsManagement.Network;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PhysWrapperPositionMessage implements IMessage {

	public PhysicsWrapperEntity toSpawn;

	public int entityID;
	public double posX, posY, posZ;
	public double pitch, yaw, roll;
	public Vector centerOfMass;

	public PhysWrapperPositionMessage() {
	}

	public PhysWrapperPositionMessage(PhysicsWrapperEntity toSend) {
		toSpawn = toSend;
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
