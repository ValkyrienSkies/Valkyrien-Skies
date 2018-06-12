package valkyrienwarfare.mod.network;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import valkyrienwarfare.mod.coordinates.CoordinateSpaceType;
import valkyrienwarfare.mod.coordinates.ISubspacedEntityRecord;
import valkyrienwarfare.mod.coordinates.VectorImmutable;

public class SubspacedEntityRecordMessage implements IMessage {

	// This object only exists on the side that created this packet.
	@Nullable
	private ISubspacedEntityRecord subspacedEntityRecord;
	// This data only exists on the side that received this packet.
	public int physicsObjectWrapperID;
	public int entitySubspacedID;
	@Nullable
	public VectorImmutable position;
	@Nullable
	public VectorImmutable positionLastTick;
	@Nullable
	public VectorImmutable lookDirection;
	@Nullable
	public VectorImmutable velocity;
	
	public SubspacedEntityRecordMessage(ISubspacedEntityRecord subspacedEntityRecord) {
		this.subspacedEntityRecord = subspacedEntityRecord;
	}
	
	public SubspacedEntityRecordMessage() {}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		physicsObjectWrapperID = buf.readInt();
		entitySubspacedID = buf.readInt();
		position = VectorImmutable.readFromByteBuf(buf);
		positionLastTick = VectorImmutable.readFromByteBuf(buf);
		lookDirection = VectorImmutable.readFromByteBuf(buf);
		velocity = VectorImmutable.readFromByteBuf(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (subspacedEntityRecord.getParentSubspace().getSubspaceCoordinatesType() == CoordinateSpaceType.GLOBAL_COORDINATES) {
			throw new IllegalStateException(
					"Just tried sending SubspacedEntityRecordMessage for a record that was made by the world subspace. This isn't right so we crash right here.");
		}
		buf.writeInt(subspacedEntityRecord.getParentSubspace().getSubspaceParentEntityID());
		buf.writeInt(subspacedEntityRecord.getParentEntity().getSubspacedEntityID());
		subspacedEntityRecord.getPosition().writeToByteBuf(buf);
		subspacedEntityRecord.getPositionLastTick().writeToByteBuf(buf);
		subspacedEntityRecord.getLookDirection().writeToByteBuf(buf);
		subspacedEntityRecord.getVelocity().writeToByteBuf(buf);
	}

}
