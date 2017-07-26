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

    public boolean airshipUp_KeyDown;
    public boolean airshipDown_KeyDown;
    public boolean airshipForward_KeyDown;
    public boolean airshipBackward_KeyDown;
    public boolean airshipLeft_KeyDown;
    public boolean airshipRight_KeyDown;
    public boolean airshipSprinting;
    public boolean airshipStop_KeyDown;

    public static boolean airshipUp_KeyPressedLast;
    public static boolean airshipDown_KeyPressedLast;
    public static boolean airshipForward_KeyPressedLast;
    public static boolean airshipBackward_KeyPressedLast;
    public static boolean airshipLeft_KeyPressedLast;
    public static boolean airshipRight_KeyPressedLast;
    public static boolean airshipStop_KeyPressedLast;

    public boolean airshipUp_KeyPressed;
    public boolean airshipDown_KeyPressed;
    public boolean airshipForward_KeyPressed;
    public boolean airshipBackward_KeyPressed;
    public boolean airshipLeft_KeyPressed;
    public boolean airshipRight_KeyPressed;
    public boolean airshipStop_KeyPressed;

    public Enum inputType;
    public UUID shipFor = defaultUUID;
    public BlockPos controlBlockPos;

    private static UUID defaultUUID = new UUID(0,0);

    public PilotControlsMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);

        airshipUp_KeyDown = packetBuf.readBoolean();
        airshipDown_KeyDown = packetBuf.readBoolean();
        airshipForward_KeyDown = packetBuf.readBoolean();
        airshipBackward_KeyDown = packetBuf.readBoolean();
        airshipLeft_KeyDown = packetBuf.readBoolean();
        airshipRight_KeyDown = packetBuf.readBoolean();
        airshipSprinting = packetBuf.readBoolean();
        airshipStop_KeyDown = packetBuf.readBoolean();

        airshipUp_KeyPressed = packetBuf.readBoolean();
        airshipDown_KeyPressed = packetBuf.readBoolean();
        airshipForward_KeyPressed = packetBuf.readBoolean();
        airshipBackward_KeyPressed = packetBuf.readBoolean();
        airshipLeft_KeyPressed = packetBuf.readBoolean();
        airshipRight_KeyPressed = packetBuf.readBoolean();
        airshipStop_KeyPressed = packetBuf.readBoolean();

        inputType = packetBuf.readEnumValue(ControllerInputType.class);
        shipFor = packetBuf.readUniqueId();
        controlBlockPos = packetBuf.readBlockPos();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);

        packetBuf.writeBoolean(airshipUp_KeyDown);
        packetBuf.writeBoolean(airshipDown_KeyDown);
        packetBuf.writeBoolean(airshipForward_KeyDown);
        packetBuf.writeBoolean(airshipBackward_KeyDown);
        packetBuf.writeBoolean(airshipLeft_KeyDown);
        packetBuf.writeBoolean(airshipRight_KeyDown);
        packetBuf.writeBoolean(airshipSprinting);
        packetBuf.writeBoolean(airshipStop_KeyDown);

        packetBuf.writeBoolean(airshipUp_KeyPressed);
        packetBuf.writeBoolean(airshipDown_KeyPressed);
        packetBuf.writeBoolean(airshipForward_KeyPressed);
        packetBuf.writeBoolean(airshipBackward_KeyPressed);
        packetBuf.writeBoolean(airshipLeft_KeyPressed);
        packetBuf.writeBoolean(airshipRight_KeyPressed);
        packetBuf.writeBoolean(airshipStop_KeyPressed);

        packetBuf.writeEnumValue(inputType);
        packetBuf.writeUniqueId(shipFor);
        if(controlBlockPos == null) {
        	System.out.println(":(");
        	controlBlockPos = BlockPos.ORIGIN;
        }
        packetBuf.writeBlockPos(controlBlockPos);
    }

    public void assignKeyBooleans(PhysicsWrapperEntity shipPiloting, Enum inputType) {
        airshipUp_KeyDown = VWKeyHandler.airshipUp.isKeyDown();
        airshipDown_KeyDown = VWKeyHandler.airshipDown.isKeyDown();
        airshipForward_KeyDown = VWKeyHandler.airshipForward.isKeyDown();
        airshipBackward_KeyDown = VWKeyHandler.airshipBackward.isKeyDown();
        airshipLeft_KeyDown = VWKeyHandler.airshipLeft.isKeyDown();
        airshipRight_KeyDown = VWKeyHandler.airshipRight.isKeyDown();
        airshipSprinting = VWKeyHandler.getIsPlayerSprinting();

        airshipUp_KeyPressed = airshipUp_KeyDown && !airshipUp_KeyPressedLast;
        airshipDown_KeyPressed = airshipDown_KeyDown && !airshipDown_KeyPressedLast;
        airshipForward_KeyPressed = airshipForward_KeyDown && !airshipForward_KeyPressedLast;
        airshipBackward_KeyPressed = airshipBackward_KeyDown && !airshipBackward_KeyPressedLast;
        airshipLeft_KeyPressed = airshipLeft_KeyDown && !airshipLeft_KeyPressedLast;
        airshipRight_KeyPressed = airshipRight_KeyDown && !airshipRight_KeyPressedLast;
        airshipStop_KeyPressed = airshipStop_KeyDown && !airshipStop_KeyPressedLast;

        if(shipPiloting != null) {
        	shipFor = shipPiloting.getUniqueID();
        }
        this.inputType = inputType;
        if(inputType == ControllerInputType.Zepplin) {
        	airshipUp_KeyDown = VWKeyHandler.airshipUp_Zepplin.isKeyDown();
            airshipDown_KeyDown = VWKeyHandler.airshipDown_Zepplin.isKeyDown();
            airshipForward_KeyDown = VWKeyHandler.airshipForward_Zepplin.isKeyDown();
            airshipBackward_KeyDown = VWKeyHandler.airshipBackward_Zepplin.isKeyDown();
            airshipLeft_KeyDown = VWKeyHandler.airshipLeft_Zepplin.isKeyDown();
            airshipRight_KeyDown = VWKeyHandler.airshipRight_Zepplin.isKeyDown();
            airshipStop_KeyDown = VWKeyHandler.airshipStop_Zepplin.isKeyDown();

            airshipUp_KeyPressed = airshipUp_KeyDown && !airshipUp_KeyPressedLast;
            airshipDown_KeyPressed = airshipDown_KeyDown && !airshipDown_KeyPressedLast;
            airshipForward_KeyPressed = airshipForward_KeyDown && !airshipForward_KeyPressedLast;
            airshipBackward_KeyPressed = airshipBackward_KeyDown && !airshipBackward_KeyPressedLast;
            airshipLeft_KeyPressed = airshipLeft_KeyDown && !airshipLeft_KeyPressedLast;
            airshipRight_KeyPressed = airshipRight_KeyDown && !airshipRight_KeyPressedLast;
            airshipStop_KeyPressed = airshipStop_KeyDown && !airshipStop_KeyPressedLast;
        }

        airshipUp_KeyPressedLast = airshipUp_KeyDown;
        airshipDown_KeyPressedLast = airshipDown_KeyDown;
        airshipForward_KeyPressedLast = airshipForward_KeyDown;
        airshipBackward_KeyPressedLast = airshipBackward_KeyDown;
        airshipLeft_KeyPressedLast = airshipLeft_KeyDown;
        airshipRight_KeyPressedLast = airshipRight_KeyDown;
        airshipStop_KeyPressedLast = airshipStop_KeyDown;
    }

}
