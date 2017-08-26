package ValkyrienWarfareBase;

import ValkyrienWarfareControl.Network.MessagePlayerStoppedPiloting;
import ValkyrienWarfareControl.Piloting.IShipPilotClient;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class VWKeyHandler {
	
	private static final String keybindIdentifyer = "Valkyrien Warfare";
	
	// Movement Keys
	public static KeyBinding airshipUp = new KeyBinding("Airship Up", Keyboard.KEY_SPACE, keybindIdentifyer);
	public static KeyBinding airshipForward = new KeyBinding("Airship Forward", Keyboard.KEY_W, keybindIdentifyer);
	public static KeyBinding airshipBackward = new KeyBinding("Airship Backward", Keyboard.KEY_S, keybindIdentifyer);
	public static KeyBinding airshipLeft = new KeyBinding("Airship Turn Left", Keyboard.KEY_A, keybindIdentifyer);
	public static KeyBinding airshipRight = new KeyBinding("Airship Turn Right", Keyboard.KEY_D, keybindIdentifyer);
	public static KeyBinding airshipDown = new KeyBinding("Airship Down", Keyboard.KEY_X, keybindIdentifyer);
	
	public static KeyBinding airshipUp_Zepplin = new KeyBinding("Zepplin Airship Up", Keyboard.KEY_ADD, keybindIdentifyer);
	public static KeyBinding airshipForward_Zepplin = new KeyBinding("Zepplin Airship Forward", Keyboard.KEY_NUMPAD8, keybindIdentifyer);
	public static KeyBinding airshipBackward_Zepplin = new KeyBinding("Zepplin Airship Backward", Keyboard.KEY_NUMPAD2, keybindIdentifyer);
	public static KeyBinding airshipLeft_Zepplin = new KeyBinding("Zepplin Airship Turn Left", Keyboard.KEY_NUMPAD4, keybindIdentifyer);
	public static KeyBinding airshipRight_Zepplin = new KeyBinding("Zepplin Airship Turn Right", Keyboard.KEY_NUMPAD6, keybindIdentifyer);
	public static KeyBinding airshipDown_Zepplin = new KeyBinding("Zepplin Airship Down", Keyboard.KEY_SUBTRACT, keybindIdentifyer);
	public static KeyBinding airshipStop_Zepplin = new KeyBinding("Zepplin Airship Stop", Keyboard.KEY_NUMPAD5, keybindIdentifyer);
	
	// Dismount Key
	public static KeyBinding dismountKey = new KeyBinding("VW Controller Dismount Key", Keyboard.KEY_LSHIFT, keybindIdentifyer);
	
	static {
		ClientRegistry.registerKeyBinding(airshipUp);
		ClientRegistry.registerKeyBinding(airshipForward);
		ClientRegistry.registerKeyBinding(airshipBackward);
		ClientRegistry.registerKeyBinding(airshipLeft);
		ClientRegistry.registerKeyBinding(airshipRight);
		ClientRegistry.registerKeyBinding(airshipDown);
		ClientRegistry.registerKeyBinding(dismountKey);
		
		ClientRegistry.registerKeyBinding(airshipUp_Zepplin);
		ClientRegistry.registerKeyBinding(airshipForward_Zepplin);
		ClientRegistry.registerKeyBinding(airshipBackward_Zepplin);
		ClientRegistry.registerKeyBinding(airshipLeft_Zepplin);
		ClientRegistry.registerKeyBinding(airshipRight_Zepplin);
		ClientRegistry.registerKeyBinding(airshipDown_Zepplin);
		ClientRegistry.registerKeyBinding(airshipStop_Zepplin);
	}
	
	public static boolean getIsPlayerSprinting() {
		return Minecraft.getMinecraft().player.isSprinting();
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
				ValkyrienWarfareControlMod.controlNetwork.sendToServer(stopPilotingMessage);
				clientPilot.stopPilotingEverything();
			}
		}
	}
	
}
