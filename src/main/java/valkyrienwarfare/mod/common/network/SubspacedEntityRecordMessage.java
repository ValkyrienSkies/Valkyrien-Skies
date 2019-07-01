package valkyrienwarfare.mod.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import valkyrienwarfare.mod.common.coordinates.CoordinateSpaceType;
import valkyrienwarfare.mod.common.coordinates.ISubspace;
import valkyrienwarfare.mod.common.coordinates.ISubspacedEntity;
import valkyrienwarfare.mod.common.coordinates.ISubspacedEntityRecord;
import valkyrienwarfare.mod.common.coordinates.ImplSubspacedEntityRecord;
import valkyrienwarfare.mod.common.coordinates.VectorImmutable;

import javax.annotation.Nullable;

/**
 * A message that sends the SubspacedEntityRecord from server to client and from
 * client to server.s
 *
 * @author thebest108
 */
public class SubspacedEntityRecordMessage implements IMessage {

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
    // This object only exists on the side that created this packet.
    @Nullable
    private ISubspacedEntityRecord subspacedEntityRecord;

    public SubspacedEntityRecordMessage(ISubspacedEntityRecord subspacedEntityRecord) {
        this.subspacedEntityRecord = subspacedEntityRecord;
    }

    public SubspacedEntityRecordMessage() {
    }

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

    public ISubspacedEntityRecord createRecordForThisMessage(ISubspacedEntity entity, ISubspace provider) {
        return new ImplSubspacedEntityRecord(entity, provider, position, positionLastTick, lookDirection, velocity);
    }

}
