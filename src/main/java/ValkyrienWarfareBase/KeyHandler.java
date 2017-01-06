package ValkyrienWarfareBase;

import org.lwjgl.input.Keyboard;

import ValkyrienWarfareControl.Piloting.ClientPilotingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KeyHandler {

	private static final String keybindIdentifyer = "Valkyrien Warfare";

	// Movement Keys
	public static KeyBinding airshipUp = new KeyBinding("Airship Up", Keyboard.KEY_SPACE, keybindIdentifyer);
	public static KeyBinding airshipForward = new KeyBinding("Airship Forward", Keyboard.KEY_W, keybindIdentifyer);
	public static KeyBinding airshipBackward = new KeyBinding("Airship Backward", Keyboard.KEY_S, keybindIdentifyer);
	public static KeyBinding airshipLeft = new KeyBinding("Airship Turn Left", Keyboard.KEY_A, keybindIdentifyer);
	public static KeyBinding airshipRight = new KeyBinding("Airship Turn Right", Keyboard.KEY_D, keybindIdentifyer);
	public static KeyBinding airshipDown = new KeyBinding("Airship Down", Keyboard.KEY_X, keybindIdentifyer);

	// Dismount Key
	public static KeyBinding airshipDismount = new KeyBinding("Airship Dismount", Keyboard.KEY_LSHIFT, keybindIdentifyer);

	static {
		ClientRegistry.registerKeyBinding(airshipUp);
		ClientRegistry.registerKeyBinding(airshipForward);
		ClientRegistry.registerKeyBinding(airshipBackward);
		ClientRegistry.registerKeyBinding(airshipLeft);
		ClientRegistry.registerKeyBinding(airshipRight);
		ClientRegistry.registerKeyBinding(airshipDown);
		ClientRegistry.registerKeyBinding(airshipDismount);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void playerTick(PlayerTickEvent event) {
		if (event.side == Side.SERVER)
			return;
		if (event.phase == Phase.START) {
			if (ClientPilotingManager.isPlayerPilotingShip()) {
				// if(airshipDismount.isKeyDown()){
				// PilotShipManager.dismountPlayer();
				// }else{
				Entity player = Minecraft.getMinecraft().thePlayer;

				player.setPosition(player.posX, player.posY, player.posZ);

				ClientPilotingManager.sendPilotKeysToServer();
				// }
			}
		}
	}

}
