package org.valkyrienskies.mod.common.physics.management;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.physmanagement.chunk.VSChunkClaim;

/**
 * This class essentially handles all the issues with ticking and handling physics objects in the
 * given world
 *
 * @author thebest108
 */
public class WorldPhysObjectManager {

    public final World worldObj;
    public final Set<PhysicsWrapperEntity> physicsEntities;
    private final Long2ObjectMap<PhysicsWrapperEntity> chunkPosToPhysicsEntityMap;

    public WorldPhysObjectManager(World toManage) {
        this.worldObj = toManage;
        this.physicsEntities = ConcurrentHashMap.newKeySet();
        this.chunkPosToPhysicsEntityMap = new Long2ObjectOpenHashMap<>();
    }

    /**
     * Returns the list of PhysicsEntities that aren't too far away from players to justify being
     * ticked
     *
     * @return
     */
    public List<PhysicsWrapperEntity> getTickablePhysicsEntities() {
        List<PhysicsWrapperEntity> list = new ArrayList<>(physicsEntities);
        Iterator<PhysicsWrapperEntity> iterator = list.iterator();
        while (iterator.hasNext()) {
            PhysicsWrapperEntity wrapperEntity = iterator.next();
            if (!wrapperEntity.getPhysicsObject()
                .isFullyLoaded()) {
                // Don't tick ships that aren't fully loaded.
                iterator.remove();
            }
        }
        return list;
    }

    public void onLoad(PhysicsWrapperEntity loaded) {
        if (loaded.world.isRemote) {
            List<PhysicsWrapperEntity> potentialMatches = new ArrayList<PhysicsWrapperEntity>();
            for (PhysicsWrapperEntity wrapper : physicsEntities) {
                if (wrapper.getPersistentID().equals(loaded.getPersistentID())) {
                    potentialMatches.add(wrapper);
                }
            }
            for (PhysicsWrapperEntity caught : potentialMatches) {
                physicsEntities.remove(caught);
                caught.getPhysicsObject().onThisUnload();
                // System.out.println("Caught one");
            }
        }
        loaded.isDead = false;
        loaded.getPhysicsObject().resetConsecutiveProperTicks();
        physicsEntities.add(loaded);
    }

    /**
     * By preloading this, TileEntities loaded within ship chunks can have a direct link to the ship
     * object while it still loading
     *
     * @param loaded
     */
    public void preloadPhysicsWrapperEntityMappings(PhysicsWrapperEntity loaded) {
        for (int x = loaded.getPhysicsObject().getOwnedChunks().minX();
             x <= loaded.getPhysicsObject().getOwnedChunks().maxX(); x++) {
            for (int z = loaded.getPhysicsObject().getOwnedChunks().minZ();
                 z <= loaded.getPhysicsObject().getOwnedChunks().maxZ(); z++) {
                chunkPosToPhysicsEntityMap.put(getLongFromInts(x, z), loaded);
            }
        }
    }

    public void onUnload(PhysicsWrapperEntity loaded) {
        if (!loaded.world.isRemote) {
            physicsEntities.remove(loaded);
            loaded.getPhysicsObject().onThisUnload();
            VSChunkClaim vsChunkClaim = loaded.getPhysicsObject().getOwnedChunks();
            for (int chunkX = vsChunkClaim.minX(); chunkX <= vsChunkClaim.maxX(); chunkX++) {
                for (int chunkZ = vsChunkClaim.minZ(); chunkZ <= vsChunkClaim.maxZ();
                     chunkZ++) {
                    chunkPosToPhysicsEntityMap.remove(getLongFromInts(chunkX, chunkZ));
                }
            }
        } else {
            loaded.isDead = true;
            loaded.getPhysicsObject()
                .onThisUnload();
        }
        // Remove this ship from all our maps, we do not want to memory leak.
        this.physicsEntities.remove(loaded);
        List<Long> keysToRemove = new ArrayList();
        for (Map.Entry<Long, PhysicsWrapperEntity> entry : chunkPosToPhysicsEntityMap.entrySet()) {
            if (entry.getValue() == loaded) {
                keysToRemove.add(entry.getKey());
            }
        }
        for (Long keyToRemove : keysToRemove) {
            chunkPosToPhysicsEntityMap.remove(keyToRemove);
        }
        loaded.getPhysicsObject().resetConsecutiveProperTicks();
    }

    public List<PhysicsWrapperEntity> getNearbyPhysObjects(AxisAlignedBB toCheck) {
        ArrayList<PhysicsWrapperEntity> ships = new ArrayList<PhysicsWrapperEntity>();
        AxisAlignedBB expandedCheck = toCheck.expand(6, 6, 6);

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            // This .expand() is only needed on server side, which tells me something is wrong with server side bounding
            // boxes
            if (wrapper.getPhysicsObject()
                .isFullyLoaded() && wrapper.getPhysicsObject()
                .getShipBoundingBox()
                .expand(2, 2, 2)
                .intersects(expandedCheck)) {
                ships.add(wrapper);
            }
        }

        return ships;
    }

    private long getLongFromInts(int x, int z) {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }
}