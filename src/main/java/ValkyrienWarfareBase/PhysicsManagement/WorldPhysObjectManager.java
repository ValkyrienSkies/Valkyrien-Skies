package ValkyrienWarfareBase.PhysicsManagement;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This class essentially handles all the issues with ticking and handling Physics Objects in the given world
 *
 * @author thebest108
 */
public class WorldPhysObjectManager {

    public final Ticket chunkLoadingTicket;
    public final HashMap<ChunkPos, PhysicsWrapperEntity> chunkPosToPhysicsEntityMap = new HashMap<ChunkPos, PhysicsWrapperEntity>();
    // private static double ShipRangeCheck = 120D;
    public World worldObj;
    public ArrayList<PhysicsWrapperEntity> physicsEntities = new ArrayList<PhysicsWrapperEntity>();
    public ArrayList<PhysicsWrapperEntity> physicsEntitiesToUnload = new ArrayList<PhysicsWrapperEntity>();
    public ArrayList<Callable<Void>> physCollisonCallables = new ArrayList<Callable<Void>>();
//	private static Field droppedChunksField;

    public WorldPhysObjectManager(World toManage) {
        worldObj = toManage;
        chunkLoadingTicket = ForgeChunkManager.requestTicket(ValkyrienWarfareMod.instance, toManage, Type.NORMAL);
    }

    /**
     * Returns the list of PhysicsEntities that aren't too far away from players to justify being ticked
     *
     * @return
     */
    public ArrayList<PhysicsWrapperEntity> getTickablePhysicsEntities() {
        ArrayList<PhysicsWrapperEntity> list = (ArrayList<PhysicsWrapperEntity>) physicsEntities.clone();

        ArrayList<PhysicsWrapperEntity> frozenShips = new ArrayList<PhysicsWrapperEntity>();

        if (worldObj instanceof WorldServer) {
            WorldServer worldServer = (WorldServer) worldObj;
            for (PhysicsWrapperEntity wrapper : list) {
                if (!wrapper.isDead) {
                    if (wrapper.wrapping.surroundingWorldChunksCache != null) {
                        int chunkCacheX = MathHelper.floor(wrapper.posX / 16D) - wrapper.wrapping.surroundingWorldChunksCache.chunkX;
                        int chunkCacheZ = MathHelper.floor(wrapper.posZ / 16D) - wrapper.wrapping.surroundingWorldChunksCache.chunkZ;

                        chunkCacheX = Math.max(0, Math.min(chunkCacheX, wrapper.wrapping.surroundingWorldChunksCache.chunkArray.length - 1));
                        chunkCacheZ = Math.max(0, Math.min(chunkCacheZ, wrapper.wrapping.surroundingWorldChunksCache.chunkArray[0].length - 1));

                        Chunk chunk = wrapper.wrapping.surroundingWorldChunksCache.chunkArray[chunkCacheX][chunkCacheZ];

//		        		Chunk chunk = wrapper.wrapping.surroundingWorldChunksCache.chunkArray[(wrapper.wrapping.surroundingWorldChunksCache.chunkArray.length)/2][(wrapper.wrapping.surroundingWorldChunksCache.chunkArray[0].length)/2];

                        if (chunk != null && !worldServer.playerChunkMap.contains(chunk.x, chunk.z)) {
                            frozenShips.add(wrapper);
                            //Then I should freeze any ships in this chunk
                        }
                    }
                } else {
                    frozenShips.add(wrapper);
                }
            }
        }

        ArrayList<PhysicsWrapperEntity> dumbShips = new ArrayList<PhysicsWrapperEntity>();

        for (PhysicsWrapperEntity wrapper : list) {
            if (wrapper.isDead || wrapper.wrapping == null || (wrapper.wrapping.physicsProcessor == null && !wrapper.world.isRemote)) {
                dumbShips.add(wrapper);
            }
        }

		/*if(droppedChunksField == null){
            try{
				if(ValkyrienWarfarePlugin.isObfuscatedEnvironment){
					droppedChunksField = ChunkProviderServer.class.getDeclaredField("field_73248_b");
				}else{
					droppedChunksField = ChunkProviderServer.class.getDeclaredField("droppedChunksSet");
				}
				droppedChunksField.setAccessible(true);
			}catch(Exception e){}
		}
		ChunkProviderServer serverProvider = (ChunkProviderServer) worldObj.getChunkProvider();

		try{
			Set<Long> droppedChunks = (Set<Long>) droppedChunksField.get(serverProvider);

			for(PhysicsWrapperEntity entity:list){
				int chunkX = entity.chunkCoordX;
				int chunkZ = entity.chunkCoordZ;
				if(droppedChunks.contains(ChunkPos.chunkXZ2Int(chunkX, chunkZ))){
					frozenShips.add(entity);
				}
			}
		}catch(Exception e){}*/

        list.removeAll(frozenShips);
        list.removeAll(dumbShips);

        return list;
    }

    public void onLoad(PhysicsWrapperEntity loaded) {
        if (!loaded.wrapping.fromSplit) {
            if (loaded.world.isRemote) {
                ArrayList<PhysicsWrapperEntity> potentialMatches = new ArrayList<PhysicsWrapperEntity>();
                for (PhysicsWrapperEntity wrapper : physicsEntities) {
                    if (wrapper.getPersistentID().equals(loaded.getPersistentID())) {
                        potentialMatches.add(wrapper);
                    }
                }
                for (PhysicsWrapperEntity caught : potentialMatches) {
                    physicsEntities.remove(caught);
                    physCollisonCallables.remove(caught.wrapping.collisionCallable);
                    caught.wrapping.onThisUnload();
//					System.out.println("Caught one");
                }

            }
            loaded.isDead = false;
            physicsEntities.add(loaded);
            physCollisonCallables.add(loaded.wrapping.collisionCallable);
            for (Chunk[] chunks : loaded.wrapping.claimedChunks) {
                for (Chunk chunk : chunks) {
                    chunkPosToPhysicsEntityMap.put(chunk.getPos(), loaded);
                }
            }
        } else {
            // reset check to prevent strange errors
            loaded.wrapping.fromSplit = false;
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
     * In the future this will be moved to a Mixins system, for now though this is worse
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
            if (wrapper.wrapping.collisionBB.intersectsWith(expandedCheck)) {
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
                //If one of the entities riding has this entity too, then be sure to check for it
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