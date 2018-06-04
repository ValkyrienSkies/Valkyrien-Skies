/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.physics.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

/**
 * This class essentially handles all the issues with ticking and handling
 * physics objects in the given world
 *
 * @author thebest108
 */
public class WorldPhysObjectManager {

    public final World worldObj;
    public final Set<PhysicsWrapperEntity> physicsEntities;
    public final List<PhysicsWrapperEntity> physicsEntitiesToUnload;
    private final Long2ObjectMap<PhysicsWrapperEntity> chunkPosToPhysicsEntityMap;
    private Future<Void> physicsThreadStatus;

    public WorldPhysObjectManager(World toManage) {
        this.worldObj = toManage;
        this.physicsEntities = ConcurrentHashMap.newKeySet();
        this.physicsEntitiesToUnload = new ArrayList<PhysicsWrapperEntity>();
        this.chunkPosToPhysicsEntityMap = new Long2ObjectOpenHashMap<PhysicsWrapperEntity>();
        this.physicsThreadStatus = null;
    }

    /**
     * Returns the list of PhysicsEntities that aren't too far away from players to
     * justify being ticked
     *
     * @return
     */
    public List<PhysicsWrapperEntity> getTickablePhysicsEntities() {
        List<PhysicsWrapperEntity> list = new ArrayList<PhysicsWrapperEntity>(physicsEntities);
        List<PhysicsWrapperEntity> frozenShips = new ArrayList<PhysicsWrapperEntity>();

        if (worldObj instanceof WorldServer) {
            WorldServer worldServer = (WorldServer) worldObj;
            for (PhysicsWrapperEntity wrapper : list) {
                if (!wrapper.isDead) {
                    if (wrapper.getPhysicsObject().cachedSurroundingChunks != null) {
                        int chunkCacheX = MathHelper.floor(wrapper.posX / 16D)
                                - wrapper.getPhysicsObject().cachedSurroundingChunks.chunkX;
                        int chunkCacheZ = MathHelper.floor(wrapper.posZ / 16D)
                                - wrapper.getPhysicsObject().cachedSurroundingChunks.chunkZ;

                        chunkCacheX = Math.max(0, Math.min(chunkCacheX,
                                wrapper.getPhysicsObject().cachedSurroundingChunks.chunkArray.length - 1));
                        chunkCacheZ = Math.max(0, Math.min(chunkCacheZ,
                                wrapper.getPhysicsObject().cachedSurroundingChunks.chunkArray[0].length - 1));

                        Chunk chunk = wrapper.getPhysicsObject().cachedSurroundingChunks.chunkArray[chunkCacheX][chunkCacheZ];

                        // Chunk chunk =
                        // wrapper.wrapping.surroundingWorldChunksCache.chunkArray[(wrapper.wrapping.surroundingWorldChunksCache.chunkArray.length)/2][(wrapper.wrapping.surroundingWorldChunksCache.chunkArray[0].length)/2];

                        if (chunk != null && !worldServer.playerChunkMap.contains(chunk.x, chunk.z)) {
                            frozenShips.add(wrapper);
                            // Then I should freeze any ships in this chunk
                        }
                    }
                } else {
                    frozenShips.add(wrapper);
                }
            }
        }

        List<PhysicsWrapperEntity> dumbShips = new ArrayList<PhysicsWrapperEntity>();

        for (PhysicsWrapperEntity wrapper : list) {
            if (wrapper.isDead || wrapper.getPhysicsObject() == null
                    || (wrapper.getPhysicsObject().physicsProcessor == null && !wrapper.world.isRemote)) {
                dumbShips.add(wrapper);
            }
        }

        list.removeAll(frozenShips);
        list.removeAll(dumbShips);

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
        physicsEntities.add(loaded);
    }

    /**
     * By preloading this, TileEntities loaded within ship chunks can have a direct
     * link to the ship object while it still loading
     *
     * @param loaded
     */
    public void preloadPhysicsWrapperEntityMappings(PhysicsWrapperEntity loaded) {
        for (int x = loaded.getPhysicsObject().ownedChunks.getMinX(); x <= loaded.getPhysicsObject().ownedChunks.getMaxX(); x++) {
            for (int z = loaded.getPhysicsObject().ownedChunks.getMinZ(); z <= loaded.getPhysicsObject().ownedChunks.getMaxZ(); z++) {
                chunkPosToPhysicsEntityMap.put(getLongFromInts(x, z), loaded);
            }
        }
    }

    public void onUnload(PhysicsWrapperEntity loaded) {
        if (!loaded.world.isRemote) {
            physicsEntities.remove(loaded);
            loaded.getPhysicsObject().onThisUnload();
            for (Chunk[] chunks : loaded.getPhysicsObject().claimedChunks) {
                for (Chunk chunk : chunks) {
                    chunkPosToPhysicsEntityMap.remove(getLongFromInts(chunk.x, chunk.z));
                }
            }
        } else {
            loaded.isDead = true;
        }
    }

    /**
     * In the future this will be moved to a Mixins system, for now though this is
     * worse.
     *
     * @param chunk
     * @return
     */
    @Deprecated
    public PhysicsWrapperEntity getManagingObjectForChunk(Chunk chunk) {
        return getManagingObjectForChunkPosition(chunk.x, chunk.z);
    }

    public PhysicsWrapperEntity getManagingObjectForChunkPosition(int chunkX, int chunkZ) {
        return chunkPosToPhysicsEntityMap.get(getLongFromInts(chunkX, chunkZ));
    }

    public List<PhysicsWrapperEntity> getNearbyPhysObjects(AxisAlignedBB toCheck) {
        ArrayList<PhysicsWrapperEntity> ships = new ArrayList<PhysicsWrapperEntity>();
        AxisAlignedBB expandedCheck = toCheck.expand(6, 6, 6);

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            if (wrapper.getPhysicsObject().getCollisionBoundingBox().intersects(expandedCheck)) {
                ships.add(wrapper);
            }
        }

        return ships;
    }

    public boolean isEntityFixed(Entity entity) {
        return getShipFixedOnto(entity) != null;
    }

    public PhysicsWrapperEntity getShipFixedOnto(Entity entity) {
        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            if (wrapper.getPhysicsObject().isEntityFixed(entity)) {
                if (wrapper.riddenByEntities.contains(entity)) {
                    return wrapper;
                }
                // If one of the entities riding has this entity too, then be sure to check for
                // it
                for (Entity e : wrapper.riddenByEntities) {
                    if (!e.isDead && e.riddenByEntities.contains(entity)) {
                        return wrapper;
                    }
                }
            }
        }
        return null;
    }

    public void setPhysicsThread(Future<Void> physicsThread) {
        this.physicsThreadStatus = physicsThread;
    }

    // Wait for the physics thread to finish before returning
    public void awaitPhysics() {
        if (physicsThreadStatus != null && !physicsThreadStatus.isDone()) {
            try {
                // Wait for the physicsThread to return before moving on.
                physicsThreadStatus.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private long getLongFromInts(int x, int z) {
    	return (long)x & 4294967295L | ((long)z & 4294967295L) << 32;
    }
}