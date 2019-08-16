package valkyrienwarfare.mod.common.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;
import valkyrienwarfare.mod.common.math.Vector;

@Config(
		modid = ValkyrienWarfareMod.MOD_ID,
		name = ValkyrienWarfareMod.MOD_NAME
)
public class VWConfig {

	@Name("Ship Y-Height Maximum")
	public static double shipUpperLimit = 1000D;

	@Name("Ship Y-Height Minimum")
	public static double shipLowerLimit = -30D;

	@Name("Enable airship permissions")
	public static boolean runAirshipPermissions = false;

	@Name("Enable gravity")
	public static boolean doGravity = true;

	public static boolean doPhysicsBlocks = true;

	public static boolean doEthereumLifting = true;

	public static boolean doAirshipRotation = true;

	public static boolean doAirshipMovement = true;

	public static double physSpeed = 0.01D;

	@Comment("The number of threads to use for physics, " +
			"recommended to use your cpu's thread count minus 2. " +
			"Cannot be set at runtime.")
	@RangeInt(min = 2)
	public static int threadCount = Math.max(2, Runtime.getRuntime().availableProcessors() - 2);

	@Name("Max airships per player")
	@Comment("Players can't own more than this many airships at once. Set to -1 to disable")
	public static int maxAirships = -1;

	public static int maxShipSize = 15000;

	public static double gravityVecX = 0D;

	public static double gravityVecY = 9.8D;

	public static double gravityVecZ = 0D;

	public static Vector gravity() {
		return new Vector(gravityVecX, gravityVecY, gravityVecZ);
	}

	/**
	 * Synchronizes the data in this class and the data in the forge configuration
	 */
	public static void sync() {
		ConfigManager.sync(ValkyrienWarfareMod.MOD_ID, Type.INSTANCE);
	}

	@Mod.EventBusSubscriber(modid = ValkyrienWarfareMod.MOD_ID)
	private static class EventHandler {

		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(ValkyrienWarfareMod.MOD_ID)) {
				sync();
			}
		}
	}

}
