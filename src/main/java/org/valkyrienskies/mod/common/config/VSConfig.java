package org.valkyrienskies.mod.common.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.command.config.ShortName;

@SuppressWarnings("WeakerAccess") // NOTE: Any forge config option MUST be "public"
@Config(modid = ValkyrienSkiesMod.MOD_ID)
public class VSConfig extends VSConfigTemplate {

    public static double shipUpperLimit = 1000D;

    public static double shipLowerLimit = -30D;

    public static boolean doGravity = true;

    public static boolean doPhysicsBlocks = true;

    public static boolean doAirshipRotation = true;

    public static boolean doAirshipMovement = true;

    @Name("showAnnoyingDebugOutput")
    @Comment("Spams your console with annoying debug output. Not recommended unless you've encountered a strange bug" +
            " and the developers told you to enable this.\nDefault is false. Set to true enable.")
    public static boolean showAnnoyingDebugOutput = false;

    @Comment("The amount of seconds simulated every physics tick. By default there are 100 physics ticks per second, " +
            "so its default value is 1/100 of a second.")
    public static double timeSimulatedPerPhysicsTick = 0.01;

    @Comment({
        "The number of threads to use for physics",
        "recommended to use your cpu's thread count minus 2.",
        "Cannot be set at runtime."
    })
    @RequiresMcRestart
    @RangeInt(min = 2)
    public static int threadCount = Math.max(2, Runtime.getRuntime().availableProcessors() - 2);

    public static int maxShipSize = 15000;

    public static double gravityVecX = 0;

    public static double gravityVecY = -9.8;

    public static double gravityVecZ = 0;

    @Comment("Blocks to not be included when assembling a ship")
    public static String[] shipSpawnDetectorBlacklist = {
        "minecraft:air", "minecraft:dirt", "minecraft:grass", "minecraft:stone",
        "minecraft:tallgrass", "minecraft:water", "minecraft:flowing_water", "minecraft:sand",
        "minecraft:sandstone", "minecraft:gravel", "minecraft:ice", "minecraft:snow",
        "minecraft:snow_layer", "minecraft:lava", "minecraft:flowing_lava", "minecraft:grass_path",
        "minecraft:bedrock", "minecraft:end_portal_frame", "minecraft:end_portal",
        "minecraft:end_gateway", "minecraft:portal",
    };

    public static String[] blockMass = {"minecraft:grass=1500"};

    public static Vector3dc gravity() {
        return new Vector3d(gravityVecX, gravityVecY, gravityVecZ);
    }

    /**
     * Synchronizes the data in this class and the data in the forge configuration
     */
    public static void sync() {
        ConfigManager.sync(ValkyrienSkiesMod.MOD_ID, Type.INSTANCE);

        VSConfig.onSync();
    }

    @Mod.EventBusSubscriber(modid = ValkyrienSkiesMod.MOD_ID)
    @SuppressWarnings("unused")
    private static class EventHandler {

        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(ValkyrienSkiesMod.MOD_ID)) {
                sync();
            }
        }
    }

}
