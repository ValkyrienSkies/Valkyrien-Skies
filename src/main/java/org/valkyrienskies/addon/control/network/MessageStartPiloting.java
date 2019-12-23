package org.valkyrienskies.addon.control.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.valkyrienskies.addon.control.piloting.ControllerInputType;

public class MessageStartPiloting implements IMessage {

    public BlockPos posToStartPiloting;
    public boolean setPhysicsWrapperEntityToPilot;
    public ControllerInputType controlType;

    public MessageStartPiloting(BlockPos posToStartPiloting, boolean setPhysicsWrapperEntityToPilot,
        ControllerInputType controlType) {
        this.posToStartPiloting = posToStartPiloting;
        this.setPhysicsWrapperEntityToPilot = setPhysicsWrapperEntityToPilot;
        this.controlType = controlType;
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
        posToStartPiloting = new BlockPos(
            packetBuf.readInt(),
            packetBuf.readInt(),
            packetBuf.readInt()
        );
        setPhysicsWrapperEntityToPilot = packetBuf.readBoolean();
        controlType = packetBuf.readEnumValue(ControllerInputType.class);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);
        packetBuf.writeInt(posToStartPiloting.getX());
        packetBuf.writeInt(posToStartPiloting.getY());
        packetBuf.writeInt(posToStartPiloting.getZ());
        //use absolute coordinates instead of writeBlockPos in case we ever add compatibility with cubic chunks
        packetBuf.writeBoolean(setPhysicsWrapperEntityToPilot);
        packetBuf.writeEnumValue(controlType);
    }

}
