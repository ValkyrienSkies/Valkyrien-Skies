package ValkyrienWarfareControl.Piloting;

import ValkyrienWarfareBase.KeyHandler;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class PilotControlsMessage implements IMessage {

    public boolean airshipUp;
    public boolean airshipDown;
    public boolean airshipForward;
    public boolean airshipBackward;
    public boolean airshipLeft;
    public boolean airshipRight;
    public boolean airshipSprinting;
    public Enum inputType;
    public UUID shipFor;
    public BlockPos controlBlockPos;

    public PilotControlsMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);
        airshipUp = packetBuf.readBoolean();
        airshipDown = packetBuf.readBoolean();
        airshipForward = packetBuf.readBoolean();
        airshipBackward = packetBuf.readBoolean();
        airshipLeft = packetBuf.readBoolean();
        airshipRight = packetBuf.readBoolean();
        airshipSprinting = packetBuf.readBoolean();
        inputType = packetBuf.readEnumValue(ControllerInputType.class);
        shipFor = packetBuf.readUniqueId();
        controlBlockPos = packetBuf.readBlockPos();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);
        packetBuf.writeBoolean(airshipUp);
        packetBuf.writeBoolean(airshipDown);
        packetBuf.writeBoolean(airshipForward);
        packetBuf.writeBoolean(airshipBackward);
        packetBuf.writeBoolean(airshipLeft);
        packetBuf.writeBoolean(airshipRight);
        packetBuf.writeBoolean(airshipSprinting);
        packetBuf.writeEnumValue(inputType);
        packetBuf.writeUniqueId(shipFor);
        packetBuf.writeBlockPos(controlBlockPos);
    }

    public void assignKeyBooleans(PhysicsWrapperEntity shipPiloting, Enum inputType) {
        airshipUp = KeyHandler.airshipUp.isKeyDown();
        airshipDown = KeyHandler.airshipDown.isKeyDown();
        airshipForward = KeyHandler.airshipForward.isKeyDown();
        airshipBackward = KeyHandler.airshipBackward.isKeyDown();
        airshipLeft = KeyHandler.airshipLeft.isKeyDown();
        airshipRight = KeyHandler.airshipRight.isKeyDown();
        airshipSprinting = KeyHandler.getIsPlayerSprinting();
        shipFor = shipPiloting.getUniqueID();
        this.inputType = inputType;
    }

}
