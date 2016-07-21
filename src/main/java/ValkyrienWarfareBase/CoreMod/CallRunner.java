package ValkyrienWarfareBase.CoreMod;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.EntityCollisionInjector;
import ValkyrienWarfareBase.Interaction.CustomPlayerInteractionManager;
import ValkyrienWarfareBase.Math.RotationMatrices;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.demo.DemoWorldManager;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;

public class CallRunner {
	
	public static void onPlaySound1(World world,@Nullable EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch)
    {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if(wrapper!=null){
			Vector posVec = new Vector(pos.getX()+.5D,pos.getY()+.5D,pos.getZ()+.5D);
			wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
			world.playSound(player, posVec.X, posVec.Y, posVec.Z, soundIn, category, volume, pitch);
		}else{
			world.playSound(player, pos, soundIn, category, volume, pitch);
		}
    }
	
	public static void onPlaySound2(World world,@Nullable EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch){
		Vector posVec = new Vector(x,y,z);
		BlockPos pos = new BlockPos(x,y,z);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if(wrapper!=null){
			wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
		}
		world.playSound(player, posVec.X, posVec.Y, posVec.Z, soundIn, category, volume, pitch);
	}
	
	public static void onPlaySound(World world,double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay)
    {
		BlockPos pos = new BlockPos(x,y,z);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if(wrapper!=null){
			Vector posVec = new Vector(x,y,z);
			wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
			x = posVec.X;
			y = posVec.Y;
			z = posVec.Z;
		}
		world.playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
    }
	
	public static double onGetDistanceSq(TileEntity ent,double x,double y,double z){
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(ent.getWorld(), ent.getPos());
		if(wrapper!=null){
			Vector vec = new Vector(x,y,z);
			wrapper.wrapping.coordTransform.fromGlobalToLocal(vec);
			return ent.getDistanceSq(vec.X, vec.Y, vec.Z);
		}
		return ent.getDistanceSq(x, y, z);
	}
	
	public static boolean onSpawnEntityInWorld(World world,Entity entity){
		BlockPos posAt = new BlockPos(entity);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, posAt);
		if(!(entity instanceof EntityFallingBlock)&&wrapper!=null&&wrapper.wrapping.coordTransform!=null){
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform,wrapper.wrapping.coordTransform.lToWRotation, entity);
		}
		return world.spawnEntityInWorld(entity);
	}
	
	public static void onSendToAllNearExcept(PlayerList list,@Nullable EntityPlayer except, double x, double y, double z, double radius, int dimension, Packet<?> packetIn)
    {
		BlockPos pos = new BlockPos(x,y,z);
		World worldIn=null;
		if(except==null){
			worldIn = DimensionManager.getWorld(dimension);
		}else{
			worldIn = except.worldObj;
		}
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);
		Vector packetPosition = new Vector(x,y,z);
		if(wrapper!=null&&wrapper.wrapping.coordTransform!=null){
			wrapper.wrapping.coordTransform.fromLocalToGlobal(packetPosition);
			
			if(packetIn instanceof SPacketSoundEffect){
				SPacketSoundEffect soundEffect = (SPacketSoundEffect)packetIn;
				packetIn = new SPacketSoundEffect(soundEffect.sound, soundEffect.category, packetPosition.X, packetPosition.Y, packetPosition.Z, soundEffect.soundVolume, soundEffect.soundPitch);
			}
//			
			if(packetIn instanceof SPacketEffect){
				SPacketEffect effect = (SPacketEffect)packetIn;
				BlockPos blockpos = new BlockPos(packetPosition.X,packetPosition.Y,packetPosition.Z);
				packetIn = new SPacketEffect(effect.soundType,blockpos,effect.soundData,effect.serverWide);
			}
		}
		
		x = packetPosition.X;
		y = packetPosition.Y;
		z = packetPosition.Z;
		
//		list.sendToAllNearExcept(except, packetPosition.X, packetPosition.Y, packetPosition.Z, radius, dimension, packetIn);
		
		for (int i = 0; i < list.playerEntityList.size(); ++i)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)list.playerEntityList.get(i);

            if (entityplayermp != except && entityplayermp.dimension == dimension)
            {
            	//NOTE: These are set to use the last variables for a good reason; dont change them
                double d0 = x - entityplayermp.posX;
                double d1 = y - entityplayermp.posY;
                double d2 = z - entityplayermp.posZ;
                
                if (d0 * d0 + d1 * d1 + d2 * d2 < radius * radius)
                {
                    entityplayermp.connection.sendPacket(packetIn);
                }else{
                	d0 = x - entityplayermp.lastTickPosX;
                    d1 = y - entityplayermp.lastTickPosY;
                    d2 = z - entityplayermp.lastTickPosZ;
                    if (d0 * d0 + d1 * d1 + d2 * d2 < radius * radius)
                    {
                        entityplayermp.connection.sendPacket(packetIn);
                    }
                }
            }
        }
    }
	
	public static boolean onSetBlockState(World world,BlockPos pos, IBlockState newState, int flags)
    {
		IBlockState oldState = world.getBlockState(pos);
		boolean toReturn = world.setBlockState(pos, newState, flags);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if(wrapper!=null){
			wrapper.wrapping.onSetBlockState(oldState, newState, pos);
			if(world.isRemote){
				wrapper.wrapping.renderer.markForUpdate();
			}
		}
		return toReturn;
    }
	
	
	public static void onMarkBlockRangeForRenderUpdate(World worldFor,int x1, int y1, int z1, int x2, int y2, int z2){
		worldFor.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
	}
	
	public static RayTraceResult onRayTraceBlocks(World world,Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock)
    {
		RayTraceResult vanillaTrace = world.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
		
		WorldPhysObjectManager physManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
		
		AxisAlignedBB playerRangeBB = new AxisAlignedBB(vec31.xCoord-1D,vec31.yCoord-1D,vec31.zCoord-1D,vec31.xCoord,vec31.yCoord,vec31.zCoord);
		
		List<PhysicsWrapperEntity> nearbyShips = physManager.getNearbyPhysObjects(world, playerRangeBB);
		boolean changed = false;
		
		Vec3d playerEyesPos = vec31;
        Vec3d playerReachVector = vec32.subtract(vec31);
        
        double reachDistance = playerReachVector.lengthVector();
		double worldResultDistFromPlayer = 420D;
		
		if(vanillaTrace!=null&&vanillaTrace.hitVec!=null){
			worldResultDistFromPlayer = vanillaTrace.hitVec.distanceTo(vec31);
		}
		
		for(PhysicsWrapperEntity wrapper:nearbyShips){
            playerEyesPos = vec31;
            playerReachVector = vec32.subtract(vec31);
            
            //Transform the coordinate system for the player eye pos
            playerEyesPos = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, playerEyesPos);
            playerReachVector = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLRotation, playerReachVector);
            
            Vec3d playerEyesReachAdded = playerEyesPos.addVector(playerReachVector.xCoord * reachDistance, playerReachVector.yCoord * reachDistance, playerReachVector.zCoord * reachDistance);
            
            RayTraceResult resultInShip = world.rayTraceBlocks(playerEyesPos, playerEyesReachAdded, false, false, true);
            
            if(resultInShip!=null&&resultInShip.hitVec!=null&&resultInShip.typeOfHit==Type.BLOCK){
	            double shipResultDistFromPlayer = resultInShip.hitVec.distanceTo(playerEyesPos);
	            
	            if(shipResultDistFromPlayer<worldResultDistFromPlayer){
	            	worldResultDistFromPlayer = shipResultDistFromPlayer;
	            	
	            	resultInShip.hitVec = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, resultInShip.hitVec);
	            	
	            	vanillaTrace = resultInShip;
	            }
            }
		}
		return vanillaTrace;
    }
	
	public static EntityPlayerMP onCreatePlayerForUser(PlayerList playerList,GameProfile profile){
		UUID uuid = EntityPlayer.getUUID(profile);
        List<EntityPlayerMP> list = Lists.<EntityPlayerMP>newArrayList();

        for (int i = 0; i < playerList.playerEntityList.size(); ++i)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)playerList.playerEntityList.get(i);

            if (entityplayermp.getUniqueID().equals(uuid))
            {
                list.add(entityplayermp);
            }
        }

        EntityPlayerMP entityplayermp2 = (EntityPlayerMP)playerList.uuidToPlayerMap.get(profile.getId());

        if (entityplayermp2 != null && !list.contains(entityplayermp2))
        {
            list.add(entityplayermp2);
        }

        for (EntityPlayerMP entityplayermp1 : list)
        {
            entityplayermp1.connection.kickPlayerFromServer("You logged in from another location");
        }

        PlayerInteractionManager playerinteractionmanager;

        if (playerList.mcServer.isDemo())
        {
            playerinteractionmanager = new DemoWorldManager(playerList.mcServer.worldServerForDimension(0));
        }
        else
        {
            playerinteractionmanager = new CustomPlayerInteractionManager(playerList.mcServer.worldServerForDimension(0));
        }

        return new EntityPlayerMP(playerList.mcServer, playerList.mcServer.worldServerForDimension(0), profile, playerinteractionmanager);
//		return playerList.createPlayerForUser(profile);
	}
	
	@SuppressWarnings("unused")
	public static EntityPlayerMP onRecreatePlayerEntity(PlayerList playerList,EntityPlayerMP playerIn, int dimension, boolean conqueredEnd)
    {
		World world = playerList.mcServer.worldServerForDimension(dimension);
        if (world == null)
        {
            dimension = 0;
        }
        else if (!world.provider.canRespawnHere())
        {
            dimension = world.provider.getRespawnDimension(playerIn);
        }

        playerIn.getServerWorld().getEntityTracker().removePlayerFromTrackers(playerIn);
        playerIn.getServerWorld().getEntityTracker().untrackEntity(playerIn);
        playerIn.getServerWorld().getPlayerChunkMap().removePlayer(playerIn);
        playerList.playerEntityList.remove(playerIn);
        playerList.mcServer.worldServerForDimension(playerIn.dimension).removeEntityDangerously(playerIn);
        BlockPos blockpos = playerIn.getBedLocation(dimension);
        boolean flag = playerIn.isSpawnForced(dimension);
        playerIn.dimension = dimension;
        PlayerInteractionManager playerinteractionmanager;

        if (playerList.mcServer.isDemo())
        {
            playerinteractionmanager = new DemoWorldManager(playerList.mcServer.worldServerForDimension(playerIn.dimension));
        }
        else
        {
            playerinteractionmanager = new PlayerInteractionManager(playerList.mcServer.worldServerForDimension(playerIn.dimension));
        }

        EntityPlayerMP entityplayermp = new EntityPlayerMP(playerList.mcServer, playerList.mcServer.worldServerForDimension(playerIn.dimension), playerIn.getGameProfile(), playerinteractionmanager);
        entityplayermp.connection = playerIn.connection;
        entityplayermp.clonePlayer(playerIn, conqueredEnd);
        entityplayermp.dimension = dimension;
       entityplayermp.setEntityId(playerIn.getEntityId());
        entityplayermp.setCommandStats(playerIn);
        entityplayermp.setPrimaryHand(playerIn.getPrimaryHand());

        for (String s : playerIn.getTags())
        {
            entityplayermp.addTag(s);
        }

        WorldServer worldserver = playerList.mcServer.worldServerForDimension(playerIn.dimension);
//        playerList.setPlayerGameTypeBasedOnOther(entityplayermp, playerIn, worldserver);
        
        
        
        if (playerIn != null)
        {
        	entityplayermp.interactionManager.setGameType(playerIn.interactionManager.getGameType());
        }
        else if (playerList.gameType != null)
        {
        	entityplayermp.interactionManager.setGameType(playerList.gameType);
        }

        entityplayermp.interactionManager.initializeGameType(worldserver.getWorldInfo().getGameType());
        

        if (blockpos != null)
        {
            BlockPos blockpos1 = EntityPlayer.getBedSpawnLocation(playerList.mcServer.worldServerForDimension(playerIn.dimension), blockpos, flag);

            if (blockpos1 != null)
            {
                entityplayermp.setLocationAndAngles((double)((float)blockpos1.getX() + 0.5F), (double)((float)blockpos1.getY() + 0.1F), (double)((float)blockpos1.getZ() + 0.5F), 0.0F, 0.0F);
                entityplayermp.setSpawnPoint(blockpos, flag);
            }
            else
            {
                entityplayermp.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
            }
        }

        worldserver.getChunkProvider().provideChunk((int)entityplayermp.posX >> 4, (int)entityplayermp.posZ >> 4);

        while (!worldserver.getCollisionBoxes(entityplayermp, entityplayermp.getEntityBoundingBox()).isEmpty() && entityplayermp.posY < 256.0D)
        {
            entityplayermp.setPosition(entityplayermp.posX, entityplayermp.posY + 1.0D, entityplayermp.posZ);
        }

        entityplayermp.connection.sendPacket(new SPacketRespawn(entityplayermp.dimension, entityplayermp.worldObj.getDifficulty(), entityplayermp.worldObj.getWorldInfo().getTerrainType(), entityplayermp.interactionManager.getGameType()));
        BlockPos blockpos2 = worldserver.getSpawnPoint();
        entityplayermp.connection.setPlayerLocation(entityplayermp.posX, entityplayermp.posY, entityplayermp.posZ, entityplayermp.rotationYaw, entityplayermp.rotationPitch);
        entityplayermp.connection.sendPacket(new SPacketSpawnPosition(blockpos2));
        entityplayermp.connection.sendPacket(new SPacketSetExperience(entityplayermp.experience, entityplayermp.experienceTotal, entityplayermp.experienceLevel));
        playerList.updateTimeAndWeatherForPlayer(entityplayermp, worldserver);
        playerList.updatePermissionLevel(entityplayermp);
        worldserver.getPlayerChunkMap().addPlayer(entityplayermp);
        worldserver.spawnEntityInWorld(entityplayermp);
        playerList.playerEntityList.add(entityplayermp);
        playerList.uuidToPlayerMap.put(entityplayermp.getUniqueID(), entityplayermp);
        entityplayermp.addSelfToInternalCraftingInventory();
        entityplayermp.setHealth(entityplayermp.getHealth());
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerRespawnEvent(entityplayermp);
        return entityplayermp;
//		return playerList.recreatePlayerEntity(playerIn, dimension, conqueredEnd);
    }
	
	public static void onEntityMove(Entity entity,double dx,double dy,double dz){
		if(!EntityCollisionInjector.alterEntityMovement(entity, dx, dy, dz)){
			entity.moveEntity(dx, dy, dz);
		}
	}
	
	public static void onEntityRemoved(World world,Entity removed){
		if(removed instanceof PhysicsWrapperEntity){
			ValkyrienWarfareMod.physicsManager.onShipUnload((PhysicsWrapperEntity) removed);
		}
		world.onEntityRemoved(removed);
	}
	
	public static void onEntityAdded(World world,Entity added){
		world.onEntityAdded(added);
	}

	public static void onChunkUnload(ChunkProviderServer provider,Chunk chunk){
		if(!ValkyrienWarfareMod.chunkManager.isChunkInShipRange(provider.worldObj,chunk.xPosition, chunk.zPosition)){
			for (int i = 0; i < chunk.entityLists.length; ++i)
	        {
	            Collection<Entity> c = chunk.entityLists[i];
	            for(Entity entity:c){
	            	if(entity instanceof PhysicsWrapperEntity){
	            		ValkyrienWarfareMod.physicsManager.getManagerForWorld(entity.worldObj).physicsEntitiesToUnload.add((PhysicsWrapperEntity) entity);
	            	}
	            }
	        }
			provider.unload(chunk);
		}
	}

}
