package ValkyrienWarfareBase.PhysicsManagement.Network;

import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PhysWrapperPositionMessage implements IMessage{

	public PhysicsWrapperEntity toSpawn;
	
	int entityID;
	double posX,posY,posZ;
	double pitch,yaw,roll;
	Vector centerOfMass;

	public PhysWrapperPositionMessage(){}

	public PhysWrapperPositionMessage(PhysicsWrapperEntity toSend){
		toSpawn = toSend;
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
		
		centerOfMass = new Vector(buf.readDouble(),buf.readDouble(),buf.readDouble());
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
