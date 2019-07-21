package valkyrienwarfare.mod.common;

import net.minecraftforge.common.config.Configuration;
import valkyrienwarfare.mod.common.math.Vector;

import java.util.concurrent.Executors;

public class ValkyrienWarfareConfig {

    public static final String CATEGORY_INGAME = "ingame";

    public static int maxShipSize = 1500000;
    public static double shipUpperLimit = 1000D;
    public static double shipLowerLimit = -30D;
    public static int maxAirships = -1;
    public static boolean runAirshipPermissions = false;
    public static int threadCount = -1;
    public static boolean doGravity = true;
    public static boolean doPhysicsBlocks = true;
    public static boolean doAirshipRotation = true;
    public static boolean doAirshipMovement = true;
    public static boolean doEtheriumLifting = true;
    public static double physSpeed = .01D;
    public static Vector gravity = new Vector(0, -9.8D, 0);

    /**
     * Called by the game when loading the configuration file, also called whenever
     * the player makes a change in the MOD OPTIONS menu, effectively reloading all
     * the configuration values
     *
     * @param conf
     */
    public static void applyConfig(Configuration conf) {
        Configuration CONFIG = ValkyrienWarfareMod.CONFIG;
        // General
        shipUpperLimit = CONFIG.get(Configuration.CATEGORY_GENERAL,
                "Ship Y-Height Maximum", 1000D).getDouble();
        shipLowerLimit = CONFIG.get(Configuration.CATEGORY_GENERAL,
                "Ship Y-Height Minimum", -30D).getDouble();
        maxAirships = CONFIG.get(Configuration.CATEGORY_GENERAL,
                "Max airships per player", -1,
                "Players can't own more than this many airships at once. Set to -1 to disable.").getInt();
        runAirshipPermissions = CONFIG.get(Configuration.CATEGORY_GENERAL,
                "Enable airship permissions", false,
                "Enables the airship permissions system").getBoolean();
        threadCount = CONFIG.get(Configuration.CATEGORY_GENERAL,
                "Physics thread count", Runtime.getRuntime().availableProcessors() - 2,
                "The number of threads to use for physics, " +
                        "recommended to use your cpu's thread count minus 2." +
                        "Cannot be set at runtime.").getInt();
        // In-game
        doGravity = CONFIG.get(CATEGORY_INGAME,
                "doGravity", true).getBoolean();
        doPhysicsBlocks = CONFIG.get(CATEGORY_INGAME,
                "doPhysicsBlocks", true).getBoolean();
        doAirshipRotation = CONFIG.get(CATEGORY_INGAME,
                "doAirshipRotation", true).getBoolean();
        doAirshipMovement = CONFIG.get(CATEGORY_INGAME,
                "doAirshipMovement", true).getBoolean();
        maxShipSize = CONFIG.get(CATEGORY_INGAME,
                "maxShipSize", 15000).getInt();
        physSpeed = CONFIG.get(CATEGORY_INGAME, "physSpeed", 0.01D).getDouble();

        double gravityVecX = CONFIG.get(CATEGORY_INGAME, "gravityVecX", 0D).getDouble();
        double gravityVecY = CONFIG.get(CATEGORY_INGAME, "gravityVecX", -9.8D).getDouble();
        double gravityVecZ = CONFIG.get(CATEGORY_INGAME, "gravityVecX", 0D).getDouble();

        gravity = new Vector(gravityVecX, gravityVecY, gravityVecZ);


        if (ValkyrienWarfareMod.PHYSICS_THREADS_EXECUTOR == null) {
            ValkyrienWarfareMod.PHYSICS_THREADS_EXECUTOR = Executors.newFixedThreadPool(Math.max(2, threadCount));
        }
        ValkyrienWarfareMod.addons.forEach(m -> m.applyConfig(ValkyrienWarfareMod.CONFIG));
    }
}
