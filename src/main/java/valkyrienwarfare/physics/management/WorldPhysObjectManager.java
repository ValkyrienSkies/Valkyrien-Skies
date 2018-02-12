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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import valkyrienwarfare.ValkyrienWarfareMod;

/**
 * This class essentially handles all the issues with ticking and handling
 * physics Objects in the given world
 *
 * @author BigBastard
 */
public class WorldPhysObjectManager {

    private final Ticket chunkLoadingTicket;
    private final Map<ChunkPos, PhysicsWrapperEntity> chunkPosToPhysicsEntityMap;
    public final World worldObj;
    public final List<PhysicsWrapperEntity> physicsEntities;
    public final List<PhysicsWrapperEntity> physicsEntitiesToUnload;
    private final List<Callable<Void>> physCollisonCallables;
    public Future physicsThreadStatus = null;

    public WorldPhysObjectManager(World toManage) {
        worldObj = toManage;
        chunkLoadingTicket = ForgeChunkManager.requestTicket(ValkyrienWarfareMod.INSTANCE, toManage, Type.NORMAL);
        physicsEntities = new ArrayList<PhysicsWrapperEntity>();
        physicsEntitiesToUnload = new ArrayList<PhysicsWrapperEntity>();
        physCollisonCallables = new ArrayList<Callable<Void>>();
        chunkPosToPhysicsEntityMap = new HashMap<ChunkPos, PhysicsWrapperEntity>();
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
                    if (wrapper.wrapping.surroundingWorldChunksCache != null) {
                        int chunkCacheX = MathHelper.floor(wrapper.posX / 16D)
                                - wrapper.wrapping.surroundingWorldChunksCache.chunkX;
                        int chunkCacheZ = MathHelper.floor(wrapper.posZ / 16D)
                                - wrapper.wrapping.surroundingWorldChunksCache.chunkZ;

                        chunkCacheX = Math.max(0, Math.min(chunkCacheX,
                                wrapper.wrapping.surroundingWorldChunksCache.chunkArray.length - 1));
                        chunkCacheZ = Math.max(0, Math.min(chunkCacheZ,
                                wrapper.wrapping.surroundingWorldChunksCache.chunkArray[0].length - 1));

                        Chunk chunk = wrapper.wrapping.surroundingWorldChunksCache.chunkArray[chunkCacheX][chunkCacheZ];

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
            if (wrapper.isDead || wrapper.wrapping == null
                    || (wrapper.wrapping.physicsProcessor == null && !wrapper.world.isRemote)) {
                dumbShips.add(wrapper);
            }
        }

        /*
         * if(droppedChunksField == null){ try{
         * if(ValkyrienWarfarePlugin.isObfuscatedEnvironment){ droppedChunksField =
         * ChunkProviderServer.class.getDeclaredField("field_73248_b"); }else{
         * droppedChunksField =
         * ChunkProviderServer.class.getDeclaredField("droppedChunksSet"); }
         * droppedChunksField.setAccessible(true); }catch(Exception e){} }
         * ChunkProviderServer serverProvider = (ChunkProviderServer)
         * worldObj.getChunkProvider();
         * 
         * try{ Set<Long> droppedChunks = (Set<Long>)
         * droppedChunksField.get(serverProvider);
         * 
         * for(PhysicsWrapperEntity entity:list){ int chunkX = entity.chunkCoordX; int
         * chunkZ = entity.chunkCoordZ;
         * if(droppedChunks.contains(ChunkPos.chunkXZ2Int(chunkX, chunkZ))){
         * frozenShips.add(entity); } } }catch(Exception e){}
         */

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
                physCollisonCallables.remove(caught.wrapping.collisionCallable);
                caught.wrapping.onThisUnload();
                // System.out.println("Caught one");
            }
        }
        loaded.isDead = false;
        physicsEntities.add(loaded);
        physCollisonCallables.add(loaded.wrapping.collisionCallable);
        // preloadPhysicsWrapperEntityMappings(loaded);

    }

    /**
     * By preloading this, TileEntities loaded within ship chunks can have a direct
     * link to the ship object while it still loading
     *
     * @param loaded
     */
    public void preloadPhysicsWrapperEntityMappings(PhysicsWrapperEntity loaded) {
        for (int x = loaded.wrapping.ownedChunks.minX; x <= loaded.wrapping.ownedChunks.maxX; x++) {
            for (int z = loaded.wrapping.ownedChunks.minZ; z <= loaded.wrapping.ownedChunks.maxZ; z++) {
                chunkPosToPhysicsEntityMap.put(new ChunkPos(x, z), loaded);
            }
        }
    }

    public void onUnload(PhysicsWrapperEntity loaded) {
        if (!loaded.world.isRemote) {
            physicsEntities.remove(loaded);
            physCollisonCallables.remove(loaded.wrapping.collisionCallable);
            loaded.wrapping.onThisUnload();
            for (Chunk[] chunks : loaded.wrapping.claimedChunks) {
                for (Chunk chunk : chunks) {
                    chunkPosToPhysicsEntityMap.remove(chunk.getPos());
                }
            }
        } else {
            loaded.isDead = true;
        }
    }

    /**
     * In the future this will be moved to a Mixins system, for now though this is
     * worse
     *
     * @param chunk
     * @return
     */
    @Deprecated
    public PhysicsWrapperEntity getManagingObjectForChunk(Chunk chunk) {
        return getManagingObjectForChunkPosition(chunk.x, chunk.z);
    }

    public PhysicsWrapperEntity getManagingObjectForChunkPosition(int chunkX, int chunkZ) {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        return chunkPosToPhysicsEntityMap.get(chunkPos);
    }

    public List<PhysicsWrapperEntity> getNearbyPhysObjects(AxisAlignedBB toCheck) {
        ArrayList<PhysicsWrapperEntity> ships = new ArrayList<PhysicsWrapperEntity>();
        AxisAlignedBB expandedCheck = toCheck.expand(6, 6, 6);

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            if (wrapper.wrapping.getCollisionBoundingBox().intersects(expandedCheck)) {
                ships.add(wrapper);
            }
        }

        return ships;
    }

    public boolean isEntityFixed(Entity entity) {
        if (getShipFixedOnto(entity, false) != null) {
            return true;
        }
        return false;
    }

    public PhysicsWrapperEntity getShipFixedOnto(Entity entity, boolean considerUUID) {
        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            if (wrapper.wrapping.isEntityFixed(entity)) {
                if (considerUUID) {
                    if (wrapper.wrapping.entityLocalPositions.containsKey(entity.getPersistentID().hashCode())) {
                        return wrapper;
                    }
                }

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

}