package valkyrienwarfare.network;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

public class EntityRelativePositionMessage implements IMessage {
	
	public Integer wrapperEntityId;
	public int listSize;
	public List<Integer> entitiesToSendIDs = new ArrayList<Integer>();
	public List<Vector> entitiesRelativePosition = new ArrayList<Vector>();
	
	public EntityRelativePositionMessage(PhysicsWrapperEntity wrapperEntity, List<Entity> entitiesToSendRelativePosition) {
		wrapperEntityId = wrapperEntity.getEntityId();
		
		listSize = entitiesToSendRelativePosition.size();
		
		double[] wToLTransformationMatrix = wrapperEntity.wrapping.coordTransform.wToLTransform;
		
		for (int i = 0; i < entitiesToSendRelativePosition.size(); i++) {
			Entity entity = entitiesToSendRelativePosition.get(i);
			Vector entityPosition = new Vector(entity);
			entityPosition.transform(wToLTransformationMatrix);
			entitiesToSendIDs.add(entity.getEntityId());
			entitiesRelativePosition.add(entityPosition);
		}
	}
	
	public EntityRelativePositionMessage() {
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);
		
		wrapperEntityId = packetBuf.readInt();
		listSize = packetBuf.readInt();
		
		for (int i = 0; i < listSize; i++) {
			int entityID = packetBuf.readInt();
			Vector entityLocalPosition = new Vector(packetBuf);
			
			entitiesToSendIDs.add(entityID);
			entitiesRelativePosition.add(entityLocalPosition);
		}
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer packetBuf = new PacketBuffer(buf);
		
		packetBuf.writeInt(wrapperEntityId);
		packetBuf.writeInt(listSize);
		
		for (int i = 0; i < listSize; i++) {
			int entityID = entitiesToSendIDs.get(i);
			Vector toWrite = entitiesRelativePosition.get(i);
			
			packetBuf.writeInt(entityID);
			toWrite.writeToByteBuf(packetBuf);
		}
	}
	
}
