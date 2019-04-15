/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.addon.control.piloting;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import valkyrienwarfare.VWKeyHandler;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.util.UUID;

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

    public void assignKeyBooleans(PhysicsWrapperEntity shipPiloting, Enum inputType) {
        airshipUp_KeyDown = VWKeyHandler.airshipUp.isKeyDown();
        airshipDown_KeyDown = VWKeyHandler.airshipDown.isKeyDown();
        airshipForward_KeyDown = VWKeyHandler.airshipForward.isKeyDown();
        airshipBackward_KeyDown = VWKeyHandler.airshipBackward.isKeyDown();
        airshipLeft_KeyDown = VWKeyHandler.airshipLeft.isKeyDown();
        airshipRight_KeyDown = VWKeyHandler.airshipRight.isKeyDown();
        airshipSprinting = VWKeyHandler.airshipSpriting.isKeyDown(); // Minecraft.getMinecraft().player.isSprinting();

        airshipUp_KeyPressed = airshipUp_KeyDown && !airshipUp_KeyPressedLast;
        airshipDown_KeyPressed = airshipDown_KeyDown && !airshipDown_KeyPressedLast;
        airshipForward_KeyPressed = airshipForward_KeyDown && !airshipForward_KeyPressedLast;
        airshipBackward_KeyPressed = airshipBackward_KeyDown && !airshipBackward_KeyPressedLast;
        airshipLeft_KeyPressed = airshipLeft_KeyDown && !airshipLeft_KeyPressedLast;
        airshipRight_KeyPressed = airshipRight_KeyDown && !airshipRight_KeyPressedLast;
        airshipStop_KeyPressed = airshipStop_KeyDown && !airshipStop_KeyPressedLast;

        if (shipPiloting != null) {
            shipFor = shipPiloting.getUniqueID();
        }
        this.inputType = inputType;
        if (inputType == ControllerInputType.Zepplin) {
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
