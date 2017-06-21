package ValkyrienWarfareControl.Network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class PlayerUsingControlsMessage implements IMessage {

    public UUID playerControllingID;
    public BlockPos controlBlockPos;
    //True if the player is controlling, false if the player is no longer controlling
    public boolean isControlling;

    public PlayerUsingControlsMessage(EntityPlayer playerControlling, BlockPos controlBlockPos, boolean isControlling) {
        playerControllingID = playerControlling.getPersistentID();
        this.controlBlockPos = controlBlockPos;
        this.isControlling = isControlling;
    }

    public PlayerUsingControlsMessage() {

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);

        playerControllingID = packetBuf.readUniqueId();
        controlBlockPos = packetBuf.readBlockPos();
        isControlling = packetBuf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);

        packetBuf.writeUniqueId(playerControllingID);
        packetBuf.writeBlockPos(controlBlockPos);
        packetBuf.writeBoolean(isControlling);
    }

}
