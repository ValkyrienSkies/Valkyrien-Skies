package org.valkyrienskies.mod.common.config;

import java.util.ArrayList;
import java.util.List;

/**
 * How do I make a Config class? See {@link VSConfig} for a good example
 *
 * <ol>
 *     <li>Extend {@link VSConfigTemplate}</li>
 *     <li>Implement a "sync" function</li>
 *     <li>Add to mod event bus and also enable sync</li>
 *     <li>Make sure your event bus method is static!</li>
 * </ol>
 * <p>
 * Example:
 * <pre>{@code
 *    @Config(
 *        modid = ValkyrienSkiesMod.MOD_ID,
 * 		name = ValkyrienSkiesMod.MOD_NAME
 * 	)
 * 	public class VSConfig extends VSConfigTemplate {
 *        @Name("Enable gravity")
 * 		public static boolean doGravity = true;
 *
 * 		public static void sync() {
 * 			ConfigManager.sync(ValkyrienSkiesMod.MOD_ID, Type.INSTANCE);
 *
 * 			VSConfig.onSync();
 *        }
 *
 *        @Mod.EventBusSubscriber(modid = ValkyrienSkiesMod.MOD_ID)
 * 		private static class EventHandler {
 *
 *            @SubscribeEvent
 *            public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
 * 				if (event.getModID().equals(ValkyrienSkiesMod.MOD_ID)) {
 * 					sync();
 *                }
 *            }
 *        }
 *    }
 * }</pre>
 */
public class VSConfigTemplate {

    private static List<Runnable> onSync = new ArrayList<>();

    public static void registerSyncEvent(Runnable run) {
        onSync.add(run);
    }

    public static void deregisterSyncEvent(Runnable run) {
        onSync.remove(run);
    }

    protected static void onSync() {
        onSync.forEach(Runnable::run);
    }

}
