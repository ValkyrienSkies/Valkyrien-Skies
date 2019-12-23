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
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.network.MessagePlayerStoppedPiloting;
import org.valkyrienskies.addon.control.piloting.IShipPilotClient;

public class VSKeyHandler {

    private static final String VS_KEYBIND_IDENTIFIER = "Valkyrien Skies";

    // Movement Keys
    public static final KeyBinding airshipUp = new KeyBinding("Airship Up", Keyboard.KEY_SPACE,
        VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipForward = new KeyBinding("Airship Forward",
        Keyboard.KEY_W, VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipBackward = new KeyBinding("Airship Backward",
        Keyboard.KEY_S, VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipLeft = new KeyBinding("Airship Turn Left", Keyboard.KEY_A,
        VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipRight = new KeyBinding("Airship Turn Right",
        Keyboard.KEY_D, VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipDown = new KeyBinding("Airship Down", Keyboard.KEY_X,
        VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipSpriting = new KeyBinding("Airship Sprinting",
        Keyboard.KEY_LCONTROL, VS_KEYBIND_IDENTIFIER);

    public static final KeyBinding airshipUp_Zepplin = new KeyBinding("Zepplin Airship Up",
        Keyboard.KEY_ADD, VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipForward_Zepplin = new KeyBinding(
        "Zepplin Airship Forward", Keyboard.KEY_NUMPAD8, VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipBackward_Zepplin = new KeyBinding(
        "Zepplin Airship Backward", Keyboard.KEY_NUMPAD2, VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipLeft_Zepplin = new KeyBinding("Zepplin Airship Turn Left",
        Keyboard.KEY_NUMPAD4, VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipRight_Zepplin = new KeyBinding(
        "Zepplin Airship Turn Right", Keyboard.KEY_NUMPAD6, VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipDown_Zepplin = new KeyBinding("Zepplin Airship Down",
        Keyboard.KEY_SUBTRACT, VS_KEYBIND_IDENTIFIER);
    public static final KeyBinding airshipStop_Zepplin = new KeyBinding("Zepplin Airship Stop",
        Keyboard.KEY_NUMPAD5, VS_KEYBIND_IDENTIFIER);

    // Dismount Key
    public static final KeyBinding dismountKey = new KeyBinding("VS Controller Dismount Key",
        Keyboard.KEY_LSHIFT, VS_KEYBIND_IDENTIFIER);

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
        if (event.side == Side.SERVER) {
            return;
        }
        if (event.phase == Phase.START) {
            IShipPilotClient clientPilot = (IShipPilotClient) event.player;
            clientPilot.onClientTick();

            if (dismountKey.isKeyDown() && clientPilot.isPilotingATile()) {
                BlockPos pilotedPos = clientPilot.getPosBeingControlled();
                MessagePlayerStoppedPiloting stopPilotingMessage = new MessagePlayerStoppedPiloting(
                    pilotedPos);
                ValkyrienSkiesControl.controlNetwork.sendToServer(stopPilotingMessage);
                clientPilot.stopPilotingEverything();
            }
        }
    }
}
