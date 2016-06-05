package ValkyrienWarfareBase.PhysicsManagement.Network;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PhysWrapperSpawnMessage implements IMessage{

	public PhysicsWrapperEntity toSpawn;

	public PhysWrapperSpawnMessage(){}

	public PhysWrapperSpawnMessage(PhysicsWrapperEntity toSend){
		toSpawn = toSend;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		toSpawn = new PhysicsWrapperEntity(null,buf.readDouble(),buf.readDouble(),buf.readDouble());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(toSpawn.posX);
		buf.writeDouble(toSpawn.posY);
		buf.writeDouble(toSpawn.posZ);
	}

}
