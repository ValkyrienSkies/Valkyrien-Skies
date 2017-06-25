package ValkyrienWarfareControl.Piloting;

import java.util.UUID;

import ValkyrienWarfareBase.VWKeyHandler;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.ShipType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PilotControlsMessage implements IMessage {

    public boolean airshipUp;
    public boolean airshipDown;
    public boolean airshipForward;
    public boolean airshipBackward;
    public boolean airshipLeft;
    public boolean airshipRight;
    public boolean airshipSprinting;
    public boolean airshipStop;
    public Enum inputType;
    public UUID shipFor = defaultUUID;
    public BlockPos controlBlockPos;

    private static UUID defaultUUID = new UUID(0,0);

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
        airshipStop = packetBuf.readBoolean();
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
        packetBuf.writeBoolean(airshipStop);
        packetBuf.writeEnumValue(inputType);
        packetBuf.writeUniqueId(shipFor);
        if(controlBlockPos == null) {
        	System.out.println(":(");
        	controlBlockPos = BlockPos.ORIGIN;
        }
        packetBuf.writeBlockPos(controlBlockPos);
    }

    public void assignKeyBooleans(PhysicsWrapperEntity shipPiloting, Enum inputType) {
        airshipUp = VWKeyHandler.airshipUp.isKeyDown();
        airshipDown = VWKeyHandler.airshipDown.isKeyDown();
        airshipForward = VWKeyHandler.airshipForward.isKeyDown();
        airshipBackward = VWKeyHandler.airshipBackward.isKeyDown();
        airshipLeft = VWKeyHandler.airshipLeft.isKeyDown();
        airshipRight = VWKeyHandler.airshipRight.isKeyDown();
        airshipSprinting = VWKeyHandler.getIsPlayerSprinting();
        if(shipPiloting != null) {
        	shipFor = shipPiloting.getUniqueID();
        }
        this.inputType = inputType;
        if(inputType == ControllerInputType.Zepplin) {
        	airshipUp = VWKeyHandler.airshipUp_Zepplin.isKeyDown();
            airshipDown = VWKeyHandler.airshipDown_Zepplin.isKeyDown();
            airshipForward = VWKeyHandler.airshipForward_Zepplin.isKeyDown();
            airshipBackward = VWKeyHandler.airshipBackward_Zepplin.isKeyDown();
            airshipLeft = VWKeyHandler.airshipLeft_Zepplin.isKeyDown();
            airshipRight = VWKeyHandler.airshipRight_Zepplin.isKeyDown();
            airshipStop = VWKeyHandler.airshipStop_Zepplin.isKeyDown();
        }
    }

}
