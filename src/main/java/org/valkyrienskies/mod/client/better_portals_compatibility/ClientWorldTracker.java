package org.valkyrienskies.mod.client.better_portals_compatibility;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Simply keeps track of the multiple worlds that can exist on a client. This is used to provide compatibility for the
 * BetterPortals mod.
 */
public class ClientWorldTracker {

    // Use Trove maps because they're fast.
    private static final TIntObjectMap<World> worldMap = new TIntObjectHashMap<>();

    public static void onWorldLoad(@Nonnull World world) {
        if (worldMap.containsKey(world.provider.getDimension())) {
            throw new IllegalArgumentException("Already loaded " + world);
        }
        worldMap.put(world.provider.getDimension(), world);
    }

    public static void onWorldUnload(@Nonnull World world) {
        if (!worldMap.containsKey(world.provider.getDimension())) {
            throw new IllegalArgumentException("This world isn't loaded " + world);
        }
        worldMap.remove(world.provider.getDimension());
    }

    @Nonnull
    public static World getWorldFor(int dimensionId) {
        World world = worldMap.get(dimensionId);
        if (world == null) {
            throw new IllegalArgumentException("No such world for dimension id " + dimensionId);
        }
        return world;
    }

    public static Iterable<World> getWorlds() {
        return worldMap.valueCollection();
    }
}
