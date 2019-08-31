package org.valkyrienskies.mod.common.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.math.Vector;

@SuppressWarnings("WeakerAccess") // NOTE: Any forge config option MUST be "public"
@Config(
    modid = ValkyrienSkiesMod.MOD_ID,
    name = ValkyrienSkiesMod.MOD_NAME
)
public class VSConfig extends VSConfigTemplate {

    @Name("Ship Y-Height Maximum")
    public static double shipUpperLimit = 1000D;

    @Name("Ship Y-Height Minimum")
    public static double shipLowerLimit = -30D;

    @Name("Enable airship permissions (does nothing atm)")
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

    public static double gravityVecY = -9.8D;

    public static double gravityVecZ = 0D;

    @Name("Engine Power")
    public static final EnginePower ENGINE_POWER = new EnginePower();

    public static class EnginePower {

        @RequiresMcRestart
        public double basicEnginePower = 2000;

        @RequiresMcRestart
        public double advancedEnginePower = 2500;

        @RequiresMcRestart
        public double eliteEnginePower = 5000;

        @RequiresMcRestart
        public double ultimateEnginePower = 10000;

        @RequiresMcRestart
        public double redstoneEnginePower = 500;

    }

    @Comment("Blocks to not be included when assembling a ship")
    public static String[] shipSpawnDetectorBlacklist = {"minecraft:air", "minecraft:dirt",
        "minecraft:grass",
        "minecraft:stone", "minecraft:tallgrass", "minecraft:water", "minecraft:flowing_water",
        "minecraft:sand", "minecraft:sandstone", "minecraft:gravel", "minecraft:ice",
        "minecraft:snow",
        "minecraft:snow_layer", "minecraft:lava", "minecraft:flowing_lava", "minecraft:grass_path",
        "minecraft:bedrock", "minecraft:end_portal_frame", "minecraft:end_portal",
        "minecraft:end_gateway",
        "minecraft:portal",
    };

    public static Vector gravity() {
        return new Vector(gravityVecX, gravityVecY, gravityVecZ);
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
