package ValkyrienWarfareBase;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.logging.Level;

import ValkyrienWarfareBase.Capability.IAirshipCounterCapability;
import ValkyrienWarfareBase.CoreMod.ValkyrienWarfarePlugin;
import ValkyrienWarfareBase.Interaction.CustomNetHandlerPlayServer;
import ValkyrienWarfareBase.Interaction.ValkyrienWarfareWorldEventListener;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsTickHandler;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Piloting.ClientPilotingManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBucket;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class EventsCommon {

	public static HashMap<EntityPlayerMP, Double[]> lastPositions = new HashMap<EntityPlayerMP, Double[]>();

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityInteractEvent(EntityInteract event) {
		event.setResult(Result.ALLOW);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTickEvent(TickEvent event) {
		if (event instanceof WorldTickEvent) {
			World worldFor = ((WorldTickEvent) event).world;
			// Only run the WorldTickEvent on Server side
			if (!worldFor.isRemote) {
				if (event.phase == Phase.START) {
					PhysicsTickHandler.onWorldTickStart(worldFor);
				}
				if (event.phase == Phase.END) {
					PhysicsTickHandler.onWorldTickEnd(worldFor);
					if(worldFor instanceof WorldServer){
						addOrRemovedAllShipChunksFromMap((WorldServer) worldFor, false);
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void worldTick(TickEvent.WorldTickEvent event){
		if ((event.phase == TickEvent.Phase.END) && (!event.world.isRemote)){
			World worldFor = event.world;
			if(worldFor instanceof WorldServer){
				addOrRemovedAllShipChunksFromMap((WorldServer) worldFor, true);
			}
		}
	}

	/**
	 * Either removes or adds all Ship Chunk entries to the World. Its a stupid fix for ChickenChunks; Blame Him For This Mess!!!
	 * Necessary to prevent ChickenChunks from trying to unload the Ship Chunks, and remove the player index in WatchingPlayers while its at it!
	 * @param worldFor
	 * @param amAdding Use true to add all chunks, false to remove all chunks
	 */
	public void addOrRemovedAllShipChunksFromMap(WorldServer worldFor, boolean amAdding){
		for(PhysicsWrapperEntity wrapper:ValkyrienWarfareMod.physicsManager.getManagerForWorld(worldFor).physicsEntities){
			for(Chunk[] chunks:wrapper.wrapping.claimedChunks){
				for(Chunk chunk:chunks){
					if(amAdding){
						((WorldServer)worldFor).getChunkProvider().id2ChunkMap.put(ChunkPos.chunkXZ2Int(chunk.xPosition, chunk.zPosition), chunk);
					}else{
						((WorldServer)worldFor).getChunkProvider().id2ChunkMap.remove(ChunkPos.chunkXZ2Int(chunk.xPosition, chunk.zPosition));
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onFillBucketEvent(FillBucketEvent event){
//		event.setResult(Result.ALLOW);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerTickEvent(PlayerTickEvent event) {
		if (!event.player.worldObj.isRemote && event.player != null) {
			EntityPlayerMP p = (EntityPlayerMP) event.player;
			try{
				if (!(p.connection instanceof CustomNetHandlerPlayServer)) {
					p.connection = new CustomNetHandlerPlayServer(p.connection);
				}else{
					CustomNetHandlerPlayServer customNetHandler = (CustomNetHandlerPlayServer)p.connection;
					if(p.interactionManager.getBlockReachDistance() == CustomNetHandlerPlayServer.dummyBlockReachDist && customNetHandler.ticksSinceLastTry == 2){
						p.interactionManager.setBlockReachDistance(((CustomNetHandlerPlayServer)p.connection).lastGoodBlockReachDist);
					}
					customNetHandler.ticksSinceLastTry ++;
				}
			}catch(Exception e){
				e.printStackTrace();
			}

			Double[] pos = lastPositions.get(p);
			if(pos == null){
				pos = new Double[3];
				lastPositions.put(p, pos);
			}
			try {
			if (pos[0] != p.posX || pos[2] != p.posZ) { // Player has moved
				if (Math.abs(p.posX) > 27000000 || Math.abs(p.posZ) > 27000000) { // Player is outside of world border, tp them back
					p.attemptTeleport(pos[0], pos[1], pos[2]);
					p.addChatMessage(new TextComponentString("You can't go beyond 27000000 blocks because airships are stored there!"));
				}
			}
			} catch (NullPointerException e)	{
				ValkyrienWarfareMod.VWLogger.log(Level.WARNING, "Nullpointer EventsCommon.java:onPlayerTickEvent");
			}

			pos[0] = p.posX;
			pos[1] = p.posY;
			pos[2] = p.posZ;
		}
	}

	private static final Field[] getFields(Explosion toSet) {
		try {
			Field xField, yField, zField, positionField;

			if (!ValkyrienWarfarePlugin.isObfuscatedEnvironment) {
				xField = toSet.getClass().getDeclaredField("explosionX");
				xField.setAccessible(true);

				yField = toSet.getClass().getDeclaredField("explosionY");
				yField.setAccessible(true);

				zField = toSet.getClass().getDeclaredField("explosionZ");
				zField.setAccessible(true);

				positionField = toSet.getClass().getDeclaredField("position");
				positionField.setAccessible(true);
			} else {
				xField = toSet.getClass().getDeclaredField("field_77284_b");
				xField.setAccessible(true);

				yField = toSet.getClass().getDeclaredField("field_77285_c");
				yField.setAccessible(true);

				zField = toSet.getClass().getDeclaredField("field_77282_d");
				zField.setAccessible(true);

				positionField = toSet.getClass().getDeclaredField("position");
				positionField.setAccessible(true);
			}
			return new Field[] { xField, yField, zField, positionField };
		} catch (Exception e) {
		}

		return null;
	}

	private static final boolean setExplosionPosition(Explosion toSet, double x, double y, double z, Field[] fields) {
		if (fields == null) {
			return false;
		}
		try {
			Field xField = fields[0], yField = fields[1], zField = fields[2], positionField = fields[3];

			double testX = toSet.explosionX;

			xField.setDouble(toSet, x);

			double testY = toSet.explosionY;

			yField.setDouble(toSet, y);

			double testZ = toSet.explosionZ;

			zField.setDouble(toSet, z);

			positionField.set(toSet, new Vec3d(x, y, z));

			toSet.getAffectedBlockPositions().clear();
			toSet.getPlayerKnockbackMap().clear();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onExplosionDetonateEvent(ExplosionEvent.Detonate event) {
		Explosion e = event.getExplosion();

		double xx = e.explosionX, yy = e.explosionY, zz = e.explosionZ;
		List<BlockPos> affectedPositionsList = new ArrayList<BlockPos>(e.getAffectedBlockPositions());
		Map<EntityPlayer, Vec3d> playerKnockbackMap = new HashMap<EntityPlayer, Vec3d>(e.getPlayerKnockbackMap());

		Vector center = new Vector(e.explosionX, e.explosionY, e.explosionZ);
		World worldIn = e.worldObj;
		float radius = e.explosionSize;

		AxisAlignedBB toCheck = new AxisAlignedBB(center.X - radius, center.Y - radius, center.Z - radius, center.X + radius, center.Y + radius, center.Z + radius);
		List<PhysicsWrapperEntity> shipsNear = ValkyrienWarfareMod.physicsManager.getManagerForWorld(e.worldObj).getNearbyPhysObjects(toCheck);
		// e.doExplosionA();
		// TODO: Make this compatible and shit!
		Field[] fields = getFields(e);

		for (PhysicsWrapperEntity ship : shipsNear) {
			Vector inLocal = new Vector(center);
			RotationMatrices.applyTransform(ship.wrapping.coordTransform.wToLTransform, inLocal);
			// inLocal.roundToWhole();

			// Explosion expl = new Explosion(ship.worldObj, null, inLocal.X, inLocal.Y, inLocal.Z, radius, false, false);

			Explosion expl = e;

			if (setExplosionPosition(e, inLocal.X, inLocal.Y, inLocal.Z, fields)) {

				double waterRange = .6D;

				boolean cancelDueToWater = false;

				for (int x = (int) Math.floor(expl.explosionX - waterRange); x <= Math.ceil(expl.explosionX + waterRange); x++) {
					for (int y = (int) Math.floor(expl.explosionY - waterRange); y <= Math.ceil(expl.explosionY + waterRange); y++) {
						for (int z = (int) Math.floor(expl.explosionZ - waterRange); z <= Math.ceil(expl.explosionZ + waterRange); z++) {
							if (!cancelDueToWater) {
								IBlockState state = e.worldObj.getBlockState(new BlockPos(x, y, z));
								if (state.getBlock() instanceof BlockLiquid) {
									cancelDueToWater = true;
								}
							}
						}
					}
				}

				expl.doExplosionA();

				double affectedPositions = 0D;

				for (Object o : expl.affectedBlockPositions) {
					BlockPos pos = (BlockPos) o;
					IBlockState state = ship.worldObj.getBlockState(pos);
					Block block = state.getBlock();
					if (!block.isAir(state, worldIn, (BlockPos) o) || ship.wrapping.explodedPositionsThisTick.contains((BlockPos) o)) {
						affectedPositions++;
					}
				}

				if (!cancelDueToWater) {
					for (Object o : expl.affectedBlockPositions) {
						BlockPos pos = (BlockPos) o;

						IBlockState state = ship.worldObj.getBlockState(pos);
						Block block = state.getBlock();
						if (!block.isAir(state, worldIn, (BlockPos) o) || ship.wrapping.explodedPositionsThisTick.contains((BlockPos) o)) {
							if (block.canDropFromExplosion(expl)) {
								block.dropBlockAsItemWithChance(ship.worldObj, pos, state, 1.0F / expl.explosionSize, 0);
							}
							block.onBlockExploded(ship.worldObj, pos, expl);
							if (!worldIn.isRemote) {
								Vector posVector = new Vector(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);

								ship.wrapping.coordTransform.fromLocalToGlobal(posVector);

								double mass = BlockMass.basicMass.getMassFromState(state, pos, ship.worldObj);

								double explosionForce = Math.sqrt(e.explosionSize) * 1000D * mass;

								Vector forceVector = new Vector(pos.getX() + .5 - expl.explosionX, pos.getY() + .5 - expl.explosionY, pos.getZ() + .5 - expl.explosionZ);

								double vectorDist = forceVector.length();

								forceVector.normalize();

								forceVector.multiply(explosionForce / vectorDist);

								RotationMatrices.doRotationOnly(ship.wrapping.coordTransform.lToWRotation, forceVector);

								PhysicsQueuedForce queuedForce = new PhysicsQueuedForce(forceVector, posVector, false, 1);

								if (!ship.wrapping.explodedPositionsThisTick.contains(pos)) {
									ship.wrapping.explodedPositionsThisTick.add(pos);
								}

								ship.wrapping.queueForce(queuedForce);
							}
						}
					}
				}

			}
			e.getAffectedBlockPositions().clear();
			e.getAffectedBlockPositions().addAll(affectedPositionsList);
			e.getPlayerKnockbackMap().clear();
			e.getPlayerKnockbackMap().putAll(playerKnockbackMap);

		}
	}**/

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onWorldLoad(WorldEvent.Load event) {
		World world = event.getWorld();
		ValkyrienWarfareMod.physicsManager.initWorld(world);
		world.addEventListener(new ValkyrienWarfareWorldEventListener(world));
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onWorldUnload(WorldEvent.Unload event) {
		if (!event.getWorld().isRemote) {
			ValkyrienWarfareMod.chunkManager.removeWorld(event.getWorld());
		} else {
			ClientPilotingManager.dismountPlayer();
		}
		ValkyrienWarfareMod.physicsManager.removeWorld(event.getWorld());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChunkNBTLoad(ChunkDataEvent.Load event) {
		NBTTagCompound data = event.getData();

	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChunkNBTUnload(ChunkDataEvent.Save event) {
		NBTTagCompound data = event.getData();

	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityUntrack(PlayerEvent.StopTracking event) {
		if (!event.getEntityPlayer().worldObj.isRemote) {
			Entity ent = event.getTarget();
			if (ent instanceof PhysicsWrapperEntity) {
				((PhysicsWrapperEntity) ent).wrapping.onPlayerUntracking(event.getEntityPlayer());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {

	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerOpenContainerEvent(PlayerContainerEvent event) {
		// event.setResult(Result.ALLOW);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBreakEvent(BreakEvent event) {

	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onHarvestDropsEvent(HarvestDropsEvent event) {

	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onHarvestCheck(HarvestCheck event) {

	}

	//Notice that this event fires for both Entities and TileEntities, so an instanceof is needed to stop weird bugs
	@SubscribeEvent
	public void onEntityConstruct(AttachCapabilitiesEvent evt) {
		if(evt.getObject() instanceof EntityPlayer){
			evt.addCapability(new ResourceLocation(ValkyrienWarfareMod.MODID, "IAirshipCounter"), new ICapabilitySerializable<NBTPrimitive>() {
				IAirshipCounterCapability inst = ValkyrienWarfareMod.airshipCounter.getDefaultInstance();

				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
					return capability == ValkyrienWarfareMod.airshipCounter;
				}

				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
					return capability == ValkyrienWarfareMod.airshipCounter ? ValkyrienWarfareMod.airshipCounter.<T>cast(inst) : null;
				}

				@Override
				public NBTPrimitive serializeNBT() {
					return (NBTPrimitive) ValkyrienWarfareMod.airshipCounter.getStorage().writeNBT(ValkyrienWarfareMod.airshipCounter, inst, null);
				}

				@Override
				public void deserializeNBT(NBTPrimitive nbt) {
					ValkyrienWarfareMod.airshipCounter.getStorage().readNBT(ValkyrienWarfareMod.airshipCounter, inst, null, nbt);
				}
			});
		}
	}

	@SubscribeEvent
	public void onJoin(PlayerLoggedInEvent event) {
		if (!event.player.worldObj.isRemote) {
			lastPositions.put((EntityPlayerMP) event.player, new Double[] { 0d, 256d, 0d });
		}
	}

	@SubscribeEvent
	public void onLeave(PlayerLoggedOutEvent event) {
		if (!event.player.worldObj.isRemote) {
			lastPositions.remove((EntityPlayerMP) event.player);
		}
	}

	@SubscribeEvent
	public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		if (!event.getWorld().isRemote)	{
			PhysicsWrapperEntity physObj = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(event.getWorld(), event.getPos());
			if (physObj != null) {
				//Bucket fix, probably not that important
				if(event.getEntityPlayer().getHeldItem(event.getHand()) != null && event.getEntityPlayer().getHeldItem(event.getHand()).getItem() instanceof ItemBucket) {
					event.setResult(Result.ALLOW);
					event.setCanceled(false);
				}
				if (ValkyrienWarfareMod.runAirshipPermissions && !(physObj.wrapping.creator.equals(event.getEntityPlayer().entityUniqueID.toString()) || physObj.wrapping.allowedUsers.contains(event.getEntityPlayer().entityUniqueID.toString())))	{
					event.getEntityPlayer().addChatMessage(new TextComponentString("You need to be added to the airship to do that!" + (physObj.wrapping.creator == null || physObj.wrapping.creator.trim().isEmpty() ? " Try using \"/airshipSettings claim\"!" : "")));
					event.setCanceled(true);
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event)	{
		if (!event.getWorld().isRemote)	{
			PhysicsWrapperEntity physObj = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(event.getWorld(), event.getPos());
			if (physObj != null) {
				if (ValkyrienWarfareMod.runAirshipPermissions && !(physObj.wrapping.creator.equals(event.getPlayer().entityUniqueID.toString()) || physObj.wrapping.allowedUsers.contains(event.getPlayer().entityUniqueID.toString())))	{
					event.getPlayer().addChatMessage(new TextComponentString("You need to be added to the airship to do that!" + (physObj.wrapping.creator == null || physObj.wrapping.creator.trim().isEmpty() ? " Try using \"/airshipSettings claim\"!" : "")));
					event.setCanceled(true);
					return;
				}
			}
		}
	}

}
