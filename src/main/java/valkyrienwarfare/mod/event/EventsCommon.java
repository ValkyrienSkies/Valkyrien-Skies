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

package valkyrienwarfare.mod.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
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
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.combat.entity.EntityMountingWeaponBase;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.capability.IAirshipCounterCapability;
import valkyrienwarfare.mod.coordinates.TransformType;
import valkyrienwarfare.mod.multithreaded.VWThreadManager;
import valkyrienwarfare.mod.physmanagement.interaction.VWWorldEventListener;
import valkyrienwarfare.physics.management.PhysicsTickHandler;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class EventsCommon {

    public static final Map<EntityPlayer, Double[]> lastPositions = new HashMap<EntityPlayer, Double[]>();

    @SubscribeEvent()
    public void onPlayerSleepInBedEvent(PlayerSleepInBedEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        BlockPos pos = event.getPos();
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, pos);
        if (wrapper != null) {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                player.sendMessage(new TextComponentString("Spawn Point Set!"));
                player.setSpawnPoint(pos, true);
                event.setResult(SleepResult.NOT_POSSIBLE_HERE);
            }
        }
    }

    @SubscribeEvent()
    public void onRightClickBlock(RightClickBlock event) {
        if (!event.getWorld().isRemote) {
            ItemStack stack = event.getItemStack();
            if (stack != null && stack.getItem() instanceof ItemNameTag) {
                BlockPos posAt = event.getPos();
                EntityPlayer player = event.getEntityPlayer();
                World world = event.getWorld();
                PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, posAt);
                if (wrapper != null) {
                    wrapper.setCustomNameTag(stack.getDisplayName());
                    --stack.stackSize;
                    event.setCanceled(true);
                }
            }
        }
    }

    // TODO: Fix conflicts with EventListener.onEntityAdded()
    // MAYBE REMOVE DUE TO CONFLICTS
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        World world = entity.world;
        BlockPos posAt = new BlockPos(entity);
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, posAt);
        if (!(entity instanceof EntityFallingBlock) && wrapper != null && wrapper.getPhysicsObject().getShipTransformationManager() != null) {
            if (entity instanceof EntityMountingWeaponBase || entity instanceof EntityArmorStand
                    || entity instanceof EntityPig || entity instanceof EntityBoat) {
                // entity.startRiding(wrapper);
                wrapper.getPhysicsObject().fixEntity(entity, new Vector(entity));
                wrapper.getPhysicsObject().queueEntityForMounting(entity);
            }
            RotationMatrices.applyTransform(wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform(), entity,
                    TransformType.SUBSPACE_TO_GLOBAL);
        }
    }

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
                } else if (event.phase == Phase.END) {
                	// TODO: This is a big source of tick lag.
                    PhysicsTickHandler.onWorldTickEnd(worldFor);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerTickEvent(PlayerTickEvent event) {
        if (!event.player.world.isRemote && event.player != null) {
            EntityPlayerMP p = (EntityPlayerMP) event.player;

            Double[] pos = lastPositions.get(p);
            if (pos == null) {
                pos = new Double[3];
                lastPositions.put(p, pos);
            }
            try {
                if (pos[0] != p.posX || pos[2] != p.posZ) { // Player has moved
                    if (Math.abs(p.posX) > 27000000 || Math.abs(p.posZ) > 27000000) { // Player is outside of world
                        // border, tp them back
                        p.attemptTeleport(pos[0], pos[1], pos[2]);
                        p.sendMessage(new TextComponentString(
                                "You can't go beyond 27000000 blocks because airships are stored there!"));
                    }
                }
            } catch (NullPointerException e) {
                ValkyrienWarfareMod.VWLogger.log(Level.WARNING, "Nullpointer EventsCommon.java:onPlayerTickEvent");
            }

            pos[0] = p.posX;
            pos[1] = p.posY;
            pos[2] = p.posZ;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldEvent.Load event) {
        event.getWorld().addEventListener(new VWWorldEventListener(event.getWorld()));
        // Don't make any VW threads for client worlds
        if (!event.getWorld().isRemote) {
        	ValkyrienWarfareMod.chunkManager.initWorld(event.getWorld());
            VWThreadManager.createVWThreadForWorld(event.getWorld());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            ValkyrienWarfareMod.chunkManager.removeWorld(event.getWorld());
        } else {
            // Fixes memory leak; @DaPorkChop please don't leave static maps lying around D:
            lastPositions.clear();
        }
        ValkyrienWarfareMod.physicsManager.removeWorld(event.getWorld());
        // Don't make any VW threads for client worlds
        if (!event.getWorld().isRemote) {
            VWThreadManager.killVWThreadForWorld(event.getWorld());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityUntrack(PlayerEvent.StopTracking event) {
        if (!event.getEntityPlayer().world.isRemote) {
            Entity ent = event.getTarget();
            if (ent instanceof PhysicsWrapperEntity) {
                ((PhysicsWrapperEntity) ent).getPhysicsObject().onPlayerUntracking(event.getEntityPlayer());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        BlockPos pos = event.getPos();
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(event.getWorld(), pos);
        if (wrapper != null) {
            event.setResult(Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerOpenContainerEvent(PlayerContainerEvent event) {
        event.setResult(Result.ALLOW);
    }

    // Notice that this event fires for both Entities and TileEntities, so an
    // instanceof is needed to stop weird bugs
    @SubscribeEvent
    public void onEntityConstruct(AttachCapabilitiesEvent evt) {
        if (evt.getObject() instanceof EntityPlayer) {
            evt.addCapability(new ResourceLocation(ValkyrienWarfareMod.MODID, "AirshipCounter"),
                    new ICapabilitySerializable<NBTTagIntArray>() {
                        IAirshipCounterCapability inst = ValkyrienWarfareMod.airshipCounter.getDefaultInstance();

                        @Override
                        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                            return capability == ValkyrienWarfareMod.airshipCounter;
                        }

                        @Override
                        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                            return capability == ValkyrienWarfareMod.airshipCounter
                                    ? ValkyrienWarfareMod.airshipCounter.<T>cast(inst)
                                    : null;
                        }

                        @Override
                        public NBTTagIntArray serializeNBT() {
                            return (NBTTagIntArray) ValkyrienWarfareMod.airshipCounter.getStorage()
                                    .writeNBT(ValkyrienWarfareMod.airshipCounter, inst, null);
                        }

                        @Override
                        public void deserializeNBT(NBTTagIntArray nbt) {
                            // Otherwise its old, then ignore it
                            if (nbt instanceof NBTTagIntArray) {
                                ValkyrienWarfareMod.airshipCounter.getStorage()
                                        .readNBT(ValkyrienWarfareMod.airshipCounter, inst, null, nbt);
                            }
                        }
                    });
        }
    }

    @SubscribeEvent
    public void onJoin(PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            lastPositions.put(player, new Double[]{0D, 256D, 0D});

            if (player.getName().equals("Drake_Eldridge") || player.getDisplayName().equals("Drake_Eldridge")) {
                WorldServer server = (WorldServer) event.player.world;

                if (Math.random() < .01D) {
                    player.setPosition(player.posX, 696969, player.posZ);
                    server.mcServer.getPlayerList().sendMessage(new TextComponentString("Cheers m8!"));
                }
                server.mcServer.getPlayerList().sendMessage(new TextComponentString(
                        "DEL is a very special boy, and this annoying greeting is made just for him"));

                for (int i = 0; i < 3; i++) {
                    server.mcServer.getPlayerList()
                            .sendMessage(new TextComponentString("VW Version Alpha Beta (Outdated)"));
                }
            }
        }
    }

    @SubscribeEvent
    public void onLeave(PlayerLoggedOutEvent event) {
        if (!event.player.world.isRemote) {
            lastPositions.remove(event.player);
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getWorld().isRemote) {
            PhysicsWrapperEntity physObj = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(event.getWorld(),
                    event.getPos());
            if (physObj != null) {
                if (ValkyrienWarfareMod.runAirshipPermissions && !(physObj.getPhysicsObject().getCreator()
                        .equals(event.getEntityPlayer().entityUniqueID.toString())
                        || physObj.getPhysicsObject().getAllowedUsers().contains(event.getEntityPlayer().entityUniqueID.toString()))) {
                    event.getEntityPlayer()
                            .sendMessage(new TextComponentString("You need to be added to the airship to do that!"
                                    + (physObj.getPhysicsObject().getCreator() == null || physObj.getPhysicsObject().getCreator().trim().isEmpty()
                                    ? " Try using \"/airshipSettings claim\"!"
                                    : "")));
                    event.setCanceled(true);
                    return;
                } else {
                    event.setResult(Result.ALLOW);
                    event.setCanceled(false);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreakFirst(BlockEvent.BreakEvent event) {
        event.setResult(Result.ALLOW);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote) {
            PhysicsWrapperEntity physObj = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(event.getWorld(),
                    event.getPos());
            if (physObj != null) {
                if (ValkyrienWarfareMod.runAirshipPermissions && !(physObj.getPhysicsObject().getCreator()
                        .equals(event.getPlayer().entityUniqueID.toString())
                        || physObj.getPhysicsObject().getAllowedUsers().contains(event.getPlayer().entityUniqueID.toString()))) {
                    event.getPlayer()
                            .sendMessage(new TextComponentString("You need to be added to the airship to do that!"
                                    + (physObj.getPhysicsObject().getCreator() == null || physObj.getPhysicsObject().getCreator().trim().isEmpty()
                                    ? " Try using \"/airshipSettings claim\"!"
                                    : "")));
                    event.setCanceled(true);
                    return;
                }
            }
        }
        onBlockChange(event.getWorld(), event.getPos(), event.getState(), Blocks.AIR.getDefaultState());
    }

    private void onBlockChange(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        // System.out.println(oldState.getBlock().getLocalizedName());
        // System.out.println(newState.getBlock().getLocalizedName());
        PhysicsWrapperEntity physObj = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
        if (physObj != null) {
            // physObj.wrapping.onSetBlockState(oldState, newState, pos);
            // System.out.println("Sucess!");
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlaceEvent(BlockEvent.PlaceEvent event) {
        PhysicsWrapperEntity physObj = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(event.getWorld(),
                event.getPos());
        if (physObj != null) {
            if (ValkyrienWarfareMod.runAirshipPermissions
                    && !(physObj.getPhysicsObject().getCreator().equals(event.getPlayer().entityUniqueID.toString())
                    || physObj.getPhysicsObject().getAllowedUsers().contains(event.getPlayer().entityUniqueID.toString()))) {
                event.getPlayer()
                        .sendMessage(new TextComponentString("You need to be added to the airship to do that!"
                                + (physObj.getPhysicsObject().getCreator() == null || physObj.getPhysicsObject().getCreator().trim().isEmpty()
                                ? " Try using \"/airshipSettings claim\"!"
                                : "")));
                event.setCanceled(true);
                return;
            }
        }
        onBlockChange(event.getWorld(), event.getPos(), event.getBlockSnapshot().getReplacedBlock(),
                event.getBlockSnapshot().getCurrentBlock());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockEvent(BlockEvent event) {
        // System.out.println(event.getClass());
    }

}
