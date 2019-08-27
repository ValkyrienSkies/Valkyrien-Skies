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

package org.valkyrienskies.mod.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.valkyrienskies.addon.control.ValkyrienWarfareControl;
import org.valkyrienskies.addon.control.network.MessagePlayerStoppedPiloting;
import org.valkyrienskies.addon.control.piloting.IShipPilotClient;

public class VWKeyHandler {

    private static final String VW_KEYBIND_IDENTIFIER = "Valkyrien Warfare";

    // Movement Keys
    public static final KeyBinding airshipUp = new KeyBinding("Airship Up", Keyboard.KEY_SPACE, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipForward = new KeyBinding("Airship Forward", Keyboard.KEY_W, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipBackward = new KeyBinding("Airship Backward", Keyboard.KEY_S, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipLeft = new KeyBinding("Airship Turn Left", Keyboard.KEY_A, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipRight = new KeyBinding("Airship Turn Right", Keyboard.KEY_D, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipDown = new KeyBinding("Airship Down", Keyboard.KEY_X, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipSpriting = new KeyBinding("Airship Sprinting", Keyboard.KEY_LCONTROL, VW_KEYBIND_IDENTIFIER);

    public static final KeyBinding airshipUp_Zepplin = new KeyBinding("Zepplin Airship Up", Keyboard.KEY_ADD, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipForward_Zepplin = new KeyBinding("Zepplin Airship Forward", Keyboard.KEY_NUMPAD8, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipBackward_Zepplin = new KeyBinding("Zepplin Airship Backward", Keyboard.KEY_NUMPAD2, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipLeft_Zepplin = new KeyBinding("Zepplin Airship Turn Left", Keyboard.KEY_NUMPAD4, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipRight_Zepplin = new KeyBinding("Zepplin Airship Turn Right", Keyboard.KEY_NUMPAD6, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipDown_Zepplin = new KeyBinding("Zepplin Airship Down", Keyboard.KEY_SUBTRACT, VW_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipStop_Zepplin = new KeyBinding("Zepplin Airship Stop", Keyboard.KEY_NUMPAD5, VW_KEYBIND_IDENTIFIER);

    // Dismount Key
    public static final KeyBinding dismountKey = new KeyBinding("VW Controller Dismount Key", Keyboard.KEY_LSHIFT, VW_KEYBIND_IDENTIFIER);

    static {
        ClientRegistry.registerKeyBinding(airshipUp);
        ClientRegistry.registerKeyBinding(airshipForward);
        ClientRegistry.registerKeyBinding(airshipBackward);
        ClientRegistry.registerKeyBinding(airshipLeft);
        ClientRegistry.registerKeyBinding(airshipRight);
        ClientRegistry.registerKeyBinding(airshipDown);
        ClientRegistry.registerKeyBinding(airshipSpriting);
        ClientRegistry.registerKeyBinding(dismountKey);

        ClientRegistry.registerKeyBinding(airshipUp_Zepplin);
        ClientRegistry.registerKeyBinding(airshipForward_Zepplin);
        ClientRegistry.registerKeyBinding(airshipBackward_Zepplin);
        ClientRegistry.registerKeyBinding(airshipLeft_Zepplin);
        ClientRegistry.registerKeyBinding(airshipRight_Zepplin);
        ClientRegistry.registerKeyBinding(airshipDown_Zepplin);
        ClientRegistry.registerKeyBinding(airshipStop_Zepplin);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void playerTick(PlayerTickEvent event) {
        if (event.side == Side.SERVER)
            return;
        if (event.phase == Phase.START) {
            IShipPilotClient clientPilot = (IShipPilotClient) event.player;
            clientPilot.onClientTick();

            if (dismountKey.isKeyDown() && clientPilot.isPilotingATile()) {
                BlockPos pilotedPos = clientPilot.getPosBeingControlled();
                MessagePlayerStoppedPiloting stopPilotingMessage = new MessagePlayerStoppedPiloting(pilotedPos);
                ValkyrienWarfareControl.controlNetwork.sendToServer(stopPilotingMessage);
                clientPilot.stopPilotingEverything();
            }
        }
    }
}
