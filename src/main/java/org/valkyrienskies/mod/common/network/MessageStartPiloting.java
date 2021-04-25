package org.valkyrienskies.mod.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.valkyrienskies.mod.common.piloting.ControllerInputType;

import java.util.UUID;

public class MessageStartPiloting implements IMessage {

    public UUID shipPilotingId;
    public BlockPos posToStartPiloting;
    public boolean setPhysicsWrapperEntityToPilot;
    public ControllerInputType controlType;

    public MessageStartPiloting(BlockPos posToStartPiloting, boolean setPhysicsWrapperEntityToPilot,
        ControllerInputType controlType) {
        this.posToStartPiloting = posToStartPiloting;
        this.setPhysicsWrapperEntityToPilot = setPhysicsWrapperEntityToPilot;
        this.controlType = controlType;
        this.shipPilotingId = null;
    }

    public MessageStartPiloting(UUID shipPilotingId,
                                ControllerInputType controlType) {
        this.posToStartPiloting = null;
        this.setPhysicsWrapperEntityToPilot = true;
        this.controlType = controlType;
        this.shipPilotingId = shipPilotingId;
    }

    /**
     * All IMessage instances must have a no argument constructor.
     */
    @SuppressWarnings("unused")
    public MessageStartPiloting() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);
        final boolean hasPosToPilot = packetBuf.readBoolean();
        final boolean hasShipToPilot = packetBuf.readBoolean();

        if (hasPosToPilot) {
            posToStartPiloting = new BlockPos(
                    packetBuf.readInt(),
                    packetBuf.readInt(),
                    packetBuf.readInt()
            );
        }
        if (hasShipToPilot) {
            shipPilotingId = packetBuf.readUniqueId();
        }
        setPhysicsWrapperEntityToPilot = packetBuf.readBoolean();
        controlType = packetBuf.readEnumValue(ControllerInputType.class);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);

        packetBuf.writeBoolean(posToStartPiloting != null);
        packetBuf.writeBoolean(shipPilotingId != null);

        if (posToStartPiloting != null) {
            packetBuf.writeInt(posToStartPiloting.getX());
            packetBuf.writeInt(posToStartPiloting.getY());
            packetBuf.writeInt(posToStartPiloting.getZ());
        }

        if (shipPilotingId != null) {
            packetBuf.writeUniqueId(shipPilotingId);
        }

        //use absolute coordinates instead of writeBlockPos in case we ever add compatibility with cubic chunks
        packetBuf.writeBoolean(setPhysicsWrapperEntityToPilot);
        packetBuf.writeEnumValue(controlType);
    }

}
