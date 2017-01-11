package ValkyrienWarfareBase;

import java.util.HashMap;

import ValkyrienWarfareBase.Capability.IAirshipCounterCapability;
import ValkyrienWarfareBase.Interaction.CustomNetHandlerPlayServer;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsTickHandler;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Piloting.ClientPilotingManager;
import ValkyrienWarfareWorld.BlockEtheriumOre;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
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

	public HashMap<EntityPlayerMP, Double[]> lastPositions = new HashMap<EntityPlayerMP, Double[]>();

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
			EntityPlayerMP p = (EntityPlayerMP) event.player;
			if (!(p.connection instanceof CustomNetHandlerPlayServer)) {
				p.connection = new CustomNetHandlerPlayServer(p.connection);
			}

			Double[] pos = lastPositions.get(p);
			if (pos[0] != p.posX || pos[2] != p.posZ) { // Player has moved
				if (Math.abs(p.posX) > 27000000 || Math.abs(p.posZ) > 27000000) { // Player is outside of world border, tp them back
					p.attemptTeleport(pos[0], pos[1], pos[2]);
					p.addChatMessage(new TextComponentString("You can't go beyond 27000000 blocks because airships are stored there!"));
				}
			}

			pos[0] = p.posX;
			pos[1] = p.posY;
			pos[2] = p.posZ;
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

	@SubscribeEvent
	public void onEntityConstruct(AttachCapabilitiesEvent evt) {
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

}
