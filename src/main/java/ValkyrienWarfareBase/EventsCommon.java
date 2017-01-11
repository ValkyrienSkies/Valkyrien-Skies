package ValkyrienWarfareBase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.CoreMod.ValkyrienWarfarePlugin;
import ValkyrienWarfareBase.Interaction.CustomNetHandlerPlayServer;
import ValkyrienWarfareBase.Physics.BlockMass;
import ValkyrienWarfareBase.Physics.PhysicsQueuedForce;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsTickHandler;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Piloting.ClientPilotingManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class EventsCommon {

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
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerTickEvent(PlayerTickEvent event) {
		if (!event.player.worldObj.isRemote) {
			EntityPlayerMP player = (EntityPlayerMP) event.player;
			if (!(player.connection instanceof CustomNetHandlerPlayServer)) {
				player.connection = new CustomNetHandlerPlayServer(player.connection);
			}
		}
	}

	private static final Field[] getFields(Explosion toSet){
		try{
			Field xField,yField,zField,positionField;
			
			if(!ValkyrienWarfarePlugin.isObfuscatedEnvironment) {
				xField = toSet.getClass().getDeclaredField("explosionX");
				xField.setAccessible(true);
				
				yField = toSet.getClass().getDeclaredField("explosionY");
				yField.setAccessible(true);
				
				zField = toSet.getClass().getDeclaredField("explosionZ");
				zField.setAccessible(true);
				
				positionField = toSet.getClass().getDeclaredField("position");
				positionField.setAccessible(true);
			}else{
				xField = toSet.getClass().getDeclaredField("field_77284_b");
				xField.setAccessible(true);
				
				yField = toSet.getClass().getDeclaredField("field_77285_c");
				yField.setAccessible(true);

				zField = toSet.getClass().getDeclaredField("field_77282_d");
				zField.setAccessible(true);

				positionField = toSet.getClass().getDeclaredField("position");
				positionField.setAccessible(true);
			}
			return new Field[]{xField, yField, zField, positionField};
		}catch(Exception e){}
		
		return null;
	}
	
	private static final boolean setExplosionPosition(Explosion toSet, double x, double y, double z, Field[] fields){ 
		if(fields == null){
			return false;
		}
		try{
			Field xField = fields[0], yField = fields[1], zField = fields[2], positionField = fields[3];
			
			double testX = toSet.explosionX;
			
			xField.setDouble(toSet, x);
			
			double testY = toSet.explosionY;
			
			yField.setDouble(toSet, y);
			
			double testZ = toSet.explosionZ;
			
			zField.setDouble(toSet, z);
			
			
			positionField.set(toSet, new Vec3d(x,y,z));
			
			toSet.getAffectedBlockPositions().clear();
			toSet.getPlayerKnockbackMap().clear();
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onExplosionDetonateEvent(ExplosionEvent.Detonate event){
		Explosion e = event.getExplosion();
		
		double xx = e.explosionX, yy = e.explosionY, zz = e.explosionZ;
		List<BlockPos> affectedPositionsList = new ArrayList<BlockPos>(e.getAffectedBlockPositions());
		Map<EntityPlayer, Vec3d> playerKnockbackMap = new HashMap<EntityPlayer, Vec3d>(e.getPlayerKnockbackMap());
		
		
		Vector center = new Vector(e.explosionX, e.explosionY, e.explosionZ);
		World worldIn = e.worldObj;
		float radius = e.explosionSize;

		AxisAlignedBB toCheck = new AxisAlignedBB(center.X - radius, center.Y - radius, center.Z - radius, center.X + radius, center.Y + radius, center.Z + radius);
		List<PhysicsWrapperEntity> shipsNear = ValkyrienWarfareMod.physicsManager.getManagerForWorld(e.worldObj).getNearbyPhysObjects(toCheck);
//		e.doExplosionA();
		// TODO: Make this compatible and shit!
		Field[] fields = getFields(e);
		
		for (PhysicsWrapperEntity ship : shipsNear) {
			Vector inLocal = new Vector(center);
			RotationMatrices.applyTransform(ship.wrapping.coordTransform.wToLTransform, inLocal);
			// inLocal.roundToWhole();
			
//			Explosion expl = new Explosion(ship.worldObj, null, inLocal.X, inLocal.Y, inLocal.Z, radius, false, false);

			Explosion expl = e;

			if(setExplosionPosition(e, inLocal.X, inLocal.Y, inLocal.Z, fields)){
				
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
	}
	
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onWorldLoad(WorldEvent.Load event) {
		// ValkyrienWarfareMod.chunkManager.initWorld(event.getWorld());
		ValkyrienWarfareMod.physicsManager.initWorld(event.getWorld());
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

}
