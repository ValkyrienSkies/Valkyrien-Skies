package org.valkyrienskies.addon.control.piloting;

import java.util.UUID;

import org.valkyrienskies.mod.client.VSKeyHandler;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PilotControlsMessage implements IMessage {

    public static boolean airshipUp_KeyPressedLast;
    public static boolean airshipDown_KeyPressedLast;
    public static boolean airshipForward_KeyPressedLast;
    public static boolean airshipBackward_KeyPressedLast;
    public static boolean airshipLeft_KeyPressedLast;
    public static boolean airshipRight_KeyPressedLast;
    public static boolean airshipStop_KeyPressedLast;
    private static UUID defaultUUID = new UUID(0, 0);
    public boolean airshipUp_KeyDown;
    public boolean airshipDown_KeyDown;
    public boolean airshipForward_KeyDown;
    public boolean airshipBackward_KeyDown;
    public boolean airshipLeft_KeyDown;
    public boolean airshipRight_KeyDown;
    public boolean airshipSprinting;
    public boolean airshipStop_KeyDown;
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

    public PilotControlsMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);

        {
            byte b = packetBuf.readByte();
            airshipUp_KeyDown = (b & 1) == 0;
            airshipDown_KeyDown = ((b >> 1) & 1) == 0;
            airshipForward_KeyDown = ((b >> 2) & 1) == 0;
            airshipBackward_KeyDown = ((b >> 3) & 1) == 0;
            airshipLeft_KeyDown = ((b >> 4) & 1) == 0;
            airshipRight_KeyDown = ((b >> 5) & 1) == 0;
            airshipSprinting = ((b >> 6) & 1) == 0;
            airshipStop_KeyDown = ((b >> 7) & 1) == 0;
        }

        {
            byte b = packetBuf.readByte();
            airshipUp_KeyPressed = (b & 1) == 0;
            airshipDown_KeyPressed = ((b >> 1) & 1) == 0;
            airshipForward_KeyPressed = ((b >> 2) & 1) == 0;
            airshipBackward_KeyPressed = ((b >> 3) & 1) == 0;
            airshipLeft_KeyPressed = ((b >> 4) & 1) == 0;
            airshipRight_KeyPressed = ((b >> 5) & 1) == 0;
            airshipStop_KeyPressed = ((b >> 6) & 1) == 0;
            //ignore most significant byte
        }

        inputType = packetBuf.readEnumValue(ControllerInputType.class);
        shipFor = packetBuf.readUniqueId();
        controlBlockPos = packetBuf.readBlockPos();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);

        {
            int i = 0;
            i |= airshipUp_KeyDown ? 0 : 1;
            i |= (airshipDown_KeyDown ? 0 : 1) << 1;
            i |= (airshipForward_KeyDown ? 0 : 1) << 2;
            i |= (airshipBackward_KeyDown ? 0 : 1) << 3;
            i |= (airshipLeft_KeyDown ? 0 : 1) << 4;
            i |= (airshipRight_KeyDown ? 0 : 1) << 5;
            i |= (airshipSprinting ? 0 : 1) << 6;
            i |= (airshipStop_KeyDown ? 0 : 1) << 7;
            packetBuf.writeByte(i);
        }

        {
            int i = 0;
            i |= airshipUp_KeyPressed ? 0 : 1;
            i |= (airshipDown_KeyPressed ? 0 : 1) << 1;
            i |= (airshipForward_KeyPressed ? 0 : 1) << 2;
            i |= (airshipBackward_KeyPressed ? 0 : 1) << 3;
            i |= (airshipLeft_KeyPressed ? 0 : 1) << 4;
            i |= (airshipRight_KeyPressed ? 0 : 1) << 5;
            i |= (airshipStop_KeyPressed ? 0 : 1) << 6;
            packetBuf.writeByte(i);
        }

        packetBuf.writeEnumValue(inputType);
        packetBuf.writeUniqueId(shipFor);
        if (controlBlockPos == null) {
            System.out.println(":(");
            controlBlockPos = BlockPos.ORIGIN;
        }
        packetBuf.writeBlockPos(controlBlockPos);
    }

    public void assignKeyBooleans(PhysicsObject shipPiloting, Enum inputType) {
        airshipUp_KeyDown = VSKeyHandler.airshipUp.isKeyDown();
        airshipDown_KeyDown = VSKeyHandler.airshipDown.isKeyDown();
        airshipForward_KeyDown = VSKeyHandler.airshipForward.isKeyDown();
        airshipBackward_KeyDown = VSKeyHandler.airshipBackward.isKeyDown();
        airshipLeft_KeyDown = VSKeyHandler.airshipLeft.isKeyDown();
        airshipRight_KeyDown = VSKeyHandler.airshipRight.isKeyDown();
        airshipSprinting = VSKeyHandler.airshipSpriting
            .isKeyDown(); // Minecraft.getMinecraft().player.isSprinting();

        airshipUp_KeyPressed = airshipUp_KeyDown && !airshipUp_KeyPressedLast;
        airshipDown_KeyPressed = airshipDown_KeyDown && !airshipDown_KeyPressedLast;
        airshipForward_KeyPressed = airshipForward_KeyDown && !airshipForward_KeyPressedLast;
        airshipBackward_KeyPressed = airshipBackward_KeyDown && !airshipBackward_KeyPressedLast;
        airshipLeft_KeyPressed = airshipLeft_KeyDown && !airshipLeft_KeyPressedLast;
        airshipRight_KeyPressed = airshipRight_KeyDown && !airshipRight_KeyPressedLast;
        airshipStop_KeyPressed = airshipStop_KeyDown && !airshipStop_KeyPressedLast;

        if (shipPiloting != null) {
            // USED TO BE #getUniqueID
            shipFor = shipPiloting.getData().getUuid();
        }
        this.inputType = inputType;
        if (inputType == ControllerInputType.Zepplin) {
            airshipUp_KeyDown = VSKeyHandler.airshipUp_Zepplin.isKeyDown();
            airshipDown_KeyDown = VSKeyHandler.airshipDown_Zepplin.isKeyDown();
            airshipForward_KeyDown = VSKeyHandler.airshipForward_Zepplin.isKeyDown();
            airshipBackward_KeyDown = VSKeyHandler.airshipBackward_Zepplin.isKeyDown();
            airshipLeft_KeyDown = VSKeyHandler.airshipLeft_Zepplin.isKeyDown();
            airshipRight_KeyDown = VSKeyHandler.airshipRight_Zepplin.isKeyDown();
            airshipStop_KeyDown = VSKeyHandler.airshipStop_Zepplin.isKeyDown();

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
