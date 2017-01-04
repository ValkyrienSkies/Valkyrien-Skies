package ValkyrienWarfareControl.Network;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class EntityFixMessage implements IMessage {

	public int shipId, entityUUID;
	// If true, then entity is mounting; if false entity is dismounting
	public boolean isFixing;
	public Vector localPosition;

	public EntityFixMessage() {
	}

	public EntityFixMessage(PhysicsWrapperEntity toFixOn, Entity toFix, boolean isFixing, Vector localPos) {
		shipId = toFixOn.getEntityId();
		entityUUID = toFix.getPersistentID().hashCode();
		this.isFixing = isFixing;
		localPosition = localPos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		shipId = buf.readInt();
		entityUUID = buf.readInt();
		isFixing = buf.readBoolean();
		if (isFixing) {
			localPosition = new Vector(buf);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(shipId);
		buf.writeInt(entityUUID);
		buf.writeBoolean(isFixing);
		if (isFixing) {
			localPosition.writeToByteBuf(buf);
		}
	}

}
