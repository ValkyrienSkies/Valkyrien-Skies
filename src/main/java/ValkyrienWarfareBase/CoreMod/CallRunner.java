package ValkyrienWarfareBase.CoreMod;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.Collision.EntityCollisionInjector;
import ValkyrienWarfareBase.Interaction.CustomPlayerInteractionManager;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.demo.DemoWorldManager;
import net.minecraft.world.gen.ChunkProviderServer;

public class CallRunner {
	
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
            entityplayermp1.playerNetServerHandler.kickPlayerFromServer("You logged in from another location");
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

        playerIn.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(playerIn);
        playerIn.getServerForPlayer().getEntityTracker().untrackEntity(playerIn);
        playerIn.getServerForPlayer().getPlayerChunkManager().removePlayer(playerIn);
        playerList.playerEntityList.remove(playerIn);
        playerList.mcServer.worldServerForDimension(playerIn.dimension).removePlayerEntityDangerously(playerIn);
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
            playerinteractionmanager = new CustomPlayerInteractionManager(playerList.mcServer.worldServerForDimension(playerIn.dimension));
        }

        EntityPlayerMP entityplayermp = new EntityPlayerMP(playerList.mcServer, playerList.mcServer.worldServerForDimension(playerIn.dimension), playerIn.getGameProfile(), playerinteractionmanager);
        entityplayermp.playerNetServerHandler = playerIn.playerNetServerHandler;
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
                entityplayermp.playerNetServerHandler.sendPacket(new SPacketChangeGameState(0, 0.0F));
            }
        }

        worldserver.getChunkProvider().provideChunk((int)entityplayermp.posX >> 4, (int)entityplayermp.posZ >> 4);

        while (!worldserver.getCubes(entityplayermp, entityplayermp.getEntityBoundingBox()).isEmpty() && entityplayermp.posY < 256.0D)
        {
            entityplayermp.setPosition(entityplayermp.posX, entityplayermp.posY + 1.0D, entityplayermp.posZ);
        }

        entityplayermp.playerNetServerHandler.sendPacket(new SPacketRespawn(entityplayermp.dimension, entityplayermp.worldObj.getDifficulty(), entityplayermp.worldObj.getWorldInfo().getTerrainType(), entityplayermp.interactionManager.getGameType()));
        BlockPos blockpos2 = worldserver.getSpawnPoint();
        entityplayermp.playerNetServerHandler.setPlayerLocation(entityplayermp.posX, entityplayermp.posY, entityplayermp.posZ, entityplayermp.rotationYaw, entityplayermp.rotationPitch);
        entityplayermp.playerNetServerHandler.sendPacket(new SPacketSpawnPosition(blockpos2));
        entityplayermp.playerNetServerHandler.sendPacket(new SPacketSetExperience(entityplayermp.experience, entityplayermp.experienceTotal, entityplayermp.experienceLevel));
        playerList.updateTimeAndWeatherForPlayer(entityplayermp, worldserver);
        playerList.updatePermissionLevel(entityplayermp);
        worldserver.getPlayerChunkManager().addPlayer(entityplayermp);
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
	
	public static void onDropChunk(ChunkProviderServer provider,int x,int z){
		if(!ValkyrienWarfareMod.chunkManager.isChunkInShipRange(provider.worldObj,x, z)){
			provider.dropChunk(x, z);
		}
	}

}
