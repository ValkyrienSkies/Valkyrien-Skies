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

// NOTE: When updating names/comments remember to update them in the lang files.
@SuppressWarnings("WeakerAccess") // NOTE: Any forge config option MUST be "public"
@Config(modid = ValkyrienSkiesMod.MOD_ID)
public class VSConfig extends VSConfigTemplate {

    @Name("Ship Y Position Minimum")
    public static double shipLowerLimit = -30;

    @Name("Ship Y Position Maximum")
    public static double shipUpperLimit = 1000;

    @Name("Enable Gravity")
    public static boolean doGravity = true;

    @Name("Enable Physics Blocks")
    public static boolean doPhysicsBlocks = true;

    @Name("Render Ship Chunk Debug Outline")
    @Comment("When true all ship chunks will be rendered with a green outline (in debug rendering mode (f3 + b)).")
    public static boolean renderShipChunkClaimsInDebug = false;

    @Name("Debug Console Output")
    @Comment({
            "Not recommended unless you've encountered a strange bug, or the developers told you to enable this.",
            "Default is false."
    })
    public static boolean showAnnoyingDebugOutput = false;

    @Name("Physics Speed Multiplier")
    @Comment({
            "Default is 1 for 100% speed. Lower values cause slow motion physics, higher values cause high speed physics."
    })
    public static double physSpeedMultiplier = 1;

    @Name("Target TPS")
    @Comment({
        "Target TPS to run the physics world at"
    })
    public static double targetTps = 60;

    // @Name("Use dynamic steps")
    // @Comment("Step physics by time since last tick instead of a fixed number")
    // public static boolean useDynamicSteps = false;

    public static double getTimeSimulatedPerTick() {
        return physSpeedMultiplier / targetTps;
    }

    @Name("Number of Physics Threads")
    @Comment({
        "The number of threads to use for physics",
        "recommended to use your cpu's thread count minus 2.",
        "Cannot be set at runtime."
    })
    @RequiresMcRestart
    @RangeInt(min = 2)
    public static int threadCount = Math.max(2, Runtime.getRuntime().availableProcessors() - 2);

    @Name("Max Detected Ship Size")
    @Comment({
            "The largest size ship a physics infuser will attempt to make.",
            "If a ship is larger than this the infuser will assume it was placed on the ground and give up.",
            "Default is 15000 blocks."
    })
    public static int maxDetectedShipSize = 15000;

    @Name("Gravity Vector X")
    public static double gravityVecX = 0;

    @Name("Gravity Vector Y")
    public static double gravityVecY = -9.8;

    @Name("Gravity Vector Z")
    public static double gravityVecZ = 0;

    @Name("Number of Ticks Players Stick to Ships")
    @Comment({
            "If a player touches a ship, then unless they touch another ship (or the ground) they will move along with the ship for this many ticks.",
            "After this number of ticks passes, the player will no longer move with the ship."
    })
    @RangeInt(min = 1)
    public static int ticksToStickToShip = 20;

    @Name("Minecarts on ships")
    @Comment({
        "Enabled minecarts on ships. WARNING: This will derail normal minecarts within a ship's AABB",
    })
    public static boolean minecartsOnShips = false;

    @Name("Chair Recipes")
    @Comment({
        "Enable recipes for captain's chair/passenger's chair"
    })
    public static boolean chairRecipes = true;

    @Name("Warn If No Modules")
    @Comment({
        "Send a warning if no known modules are installed"
    })
    public static boolean warnNoModules = true;

    @Name("Use Vanilla Explosions")
    @Comment({
        "If CUSTOM, vanilla explosions are replaced with a custom implementation which supports ships blocking",
        "explosion damage and is also faster. May have slightly different semantics for things like TNT cannons"
    })
    public static ExplosionMode explosionMode = ExplosionMode.CUSTOM;

    public enum ExplosionMode {
        VANILLA, SLOW_VANILLA, CUSTOM
    }

    @Name("Ship Loading Settings")
    @ShortName("shipLoadingSettings")
    @Comment({
            "Sets the distance at which ships get loaded/unloaded and watched/unwatched by nearby players.",
            "These settings must obey the following constraint:",
            "Player Watch Distance < Ship Load Distance < Ship Load Background Distance <= Player Unwatch Distance < Ship Unload Distance"
    })
    @LangKey("valkyrienskies.general.ship_loading_settings")
    public static final VSConfig.ShipLoadingSettings SHIP_LOADING_SETTINGS = new VSConfig.ShipLoadingSettings();

    @Name("Accurate Rain")
    @Comment("Prevents rain from going inside ships. Warning: mildly laggy")
    public static boolean accurateRain = true;

    @Name("Advanced Settings")
    @ShortName("advancedSettings")
    @Comment("For advanced users only")
    public static final AdvancedSettings ADVANCED_SETTINGS = new AdvancedSettings();

    public static class AdvancedSettings {

        public boolean multithreadCollisionCacheUpdate = true;

        @Name("Enforce Correct Thread")
        @Comment("WARNING: May cause bugs, crashes, race conditions, and/or world corruption. " +
            "If false, the CalledFromWrongThreadException is suppressed. This may make other mods compatible " +
            "at the cost of stability")
        public boolean enforceCorrectThread = true;

    }

    public static class ShipLoadingSettings {

        @Name("Player Watch Ship Distance")
        @Comment("If a player's XZ distance to a ship is less than this, then if aren't already watching it, they will start watching that ship.")
        public double watchDistance = 128;

        @Name("Ship Load Distance")
        @Comment("If a player's XZ distance to a ship is less than this, then if that ship isn't already loaded in the world, it will be loaded in the world immediately.")
        public double loadDistance = 144;

        @Name("Ship Load Background Distance")
        @Comment("If a player's XZ distance to a ship is less than this, then if that ship isn't already loaded in the world, it will preload itself using background threads.")
        public double loadBackgroundDistance = 160;

        @Name("Player Unwatch Ship Distance")
        @Comment("If a player's XZ distance to a ship is greater than this, then if they're current watching it, they will stop watching that ship.")
        public double unwatchDistance = 160;

        @Name("Ship Unload Distance")
        @Comment("If there are no players within this XZ distance of a ship, then if that ship is currently loaded, it will unload itself.")
        public double unloadDistance = 192;

        @Name("Permanently loaded")
        @Comment("If ships should be permanently. Warning: may have unforseen consequences")
        public boolean permanentlyLoaded = false;

    }

    @Name("Ship Spawn Detector Blacklist")
    @Comment("Blocks to not be included when assembling a ship.")
    public static String[] shipSpawnDetectorBlacklist = {
        "minecraft:air", "minecraft:dirt", "minecraft:grass", "minecraft:stone",
        "minecraft:tallgrass", "minecraft:water", "minecraft:flowing_water", "minecraft:sand",
        "minecraft:sandstone", "minecraft:gravel", "minecraft:ice", "minecraft:snow",
        "minecraft:snow_layer", "minecraft:lava", "minecraft:flowing_lava", "minecraft:grass_path",
        "minecraft:bedrock", "minecraft:end_portal_frame", "minecraft:end_portal",
        "minecraft:end_gateway", "minecraft:portal",
    };

    @Name("Block Mass")
    @Comment({
            "Override the auto generated mass value of blocks.",
            "The units of mass are kg."
    })
    public static String[] blockMass = {"minecraft:grass=1500", "minecraft:obsidian=20000"};

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
