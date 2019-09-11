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

package org.valkyrienskies.mod.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.valkyrienskies.fixes.IPhysicsChunk;
import org.valkyrienskies.mod.common.coordinates.CoordinateSpaceType;
import org.valkyrienskies.mod.common.entity.EntityMountable;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.PhysicsTickHandler;
import org.valkyrienskies.mod.common.physmanagement.interaction.VWWorldEventListener;
import org.valkyrienskies.mod.common.physmanagement.shipdata.IValkyrienSkiesWorldData;
import org.valkyrienskies.mod.common.ship_handling.IHasShipManager;
import org.valkyrienskies.mod.common.ship_handling.WorldClientShipManager;
import org.valkyrienskies.mod.common.ship_handling.WorldServerShipManager;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

public class EventsCommon {

    public static final Map<EntityPlayer, double[]> lastPositions = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(EventsCommon.class);

    @SubscribeEvent()
    public void onPlayerSleepInBedEvent(PlayerSleepInBedEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        BlockPos pos = event.getPos();
        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysicsObject(player.getEntityWorld(), pos);

        if (physicsObject.isPresent()) {
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
                Optional<PhysicsObject> physicsObject = ValkyrienUtils
                    .getPhysicsObject(world, posAt);

                if (physicsObject.isPresent()) {
                    physicsObject.get()
                        .wrapperEntity()
                        .setCustomNameTag(stack.getDisplayName());
                    --stack.stackSize;
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();

        World world = entity.world;
        BlockPos posAt = new BlockPos(entity);

        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(world, posAt);
        if (!event.getWorld().isRemote && physicsObject.isPresent()
            && !(entity instanceof EntityFallingBlock)) {
            if (entity instanceof EntityArmorStand
                || entity instanceof EntityPig || entity instanceof EntityBoat) {
                EntityMountable entityMountable = new EntityMountable(world,
                    entity.getPositionVector(), CoordinateSpaceType.SUBSPACE_COORDINATES, posAt);
                world.spawnEntity(entityMountable);
                entity.startRiding(entityMountable);
            }
            physicsObject.get()
                .shipTransformationManager()
                .getCurrentTickTransform().transform(entity,
                TransformType.SUBSPACE_TO_GLOBAL);
            // TODO: This should work but it doesn't because of sponge. Instead we have to rely on MixinChunk.preAddEntity() to fix this
            // event.setCanceled(true);
            // event.getWorld().spawnEntity(entity);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTickEvent(WorldTickEvent event) {
        // This only gets called server side, because forge wants it that way. But in case they
        // change their mind, this exception will crash the game to notify us of the change.
        if (event.side == Side.CLIENT) {
            throw new IllegalStateException("This event should never get called client side");
        }
        World world = event.world;
        switch (event.phase) {
            case START:
                PhysicsTickHandler.onWorldTickStart(world);
                break;
            case END:
                IHasShipManager shipManager = (IHasShipManager) world;
                shipManager.getManager().tick();
                PhysicsTickHandler.onWorldTickEnd(world);
                break;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerTickEvent(PlayerTickEvent event) {
        if (!event.player.world.isRemote && event.player != null) {
            EntityPlayerMP p = (EntityPlayerMP) event.player;

            double[] pos = lastPositions.get(p);
            if (pos == null) {
                pos = new double[3];
                lastPositions.put(p, pos);
            }
            try {
                if (pos[0] != p.posX || pos[2] != p.posZ) { // Player has moved
                    if (Math.abs(p.posX) > 27000000
                        || Math.abs(p.posZ) > 27000000) { // Player is outside of world
                        // border, tp them back
                        p.attemptTeleport(pos[0], pos[1], pos[2]);
                        p.sendMessage(new TextComponentString(
                            "You can't go beyond 27000000 blocks because airships are stored there!"));
                    }
                }
            } catch (NullPointerException e) {
                logger.warn("Nullpointer EventsCommon.java:onPlayerTickEvent");
            }

            pos[0] = p.posX;
            pos[1] = p.posY;
            pos[2] = p.posZ;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        event.getWorld().addEventListener(new VWWorldEventListener(world));
        IHasShipManager shipManager = (IHasShipManager) world;
        if (!event.getWorld().isRemote) {
            ValkyrienSkiesMod.VW_CHUNK_MANAGER.initWorld(world);
            shipManager.setManager(WorldServerShipManager::new);
        } else {
            shipManager.setManager(WorldClientShipManager::new);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            ValkyrienSkiesMod.VW_CHUNK_MANAGER.removeWorld(event.getWorld());
        } else {
            // Fixes memory leak; @DaPorkChop please don't leave static maps lying around D:
            lastPositions.clear();
        }
        ValkyrienSkiesMod.VW_PHYSICS_MANAGER.removeWorld(event.getWorld());
        IHasShipManager shipManager = (IHasShipManager) event.getWorld();
        shipManager.getManager().onWorldUnload();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityUntrack(PlayerEvent.StopTracking event) {
        if (!event.getEntityPlayer().world.isRemote) {
            Entity ent = event.getTarget();
            if (ent instanceof PhysicsWrapperEntity) {
                ((PhysicsWrapperEntity) ent).getPhysicsObject()
                    .onPlayerUntracking(event.getEntityPlayer());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        BlockPos pos = event.getPos();

        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysicsObject(event.getWorld(), pos);
        if (physicsObject.isPresent()) {
            event.setResult(Result.ALLOW);
        }
    }

    @SubscribeEvent
    public void attachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        event.addCapability(
            new ResourceLocation(ValkyrienSkiesMod.MOD_ID, "world_data_capability"),
            new ICapabilitySerializable<NBTBase>() {
                IValkyrienSkiesWorldData inst = ValkyrienSkiesMod.VS_WORLD_DATA
                    .getDefaultInstance();

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                    return capability == ValkyrienSkiesMod.VS_WORLD_DATA;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                    return capability == ValkyrienSkiesMod.VS_WORLD_DATA
                        ? ValkyrienSkiesMod.VS_WORLD_DATA.<T>cast(inst)
                        : null;
                }

                @Override
                public NBTBase serializeNBT() {
                    return ValkyrienSkiesMod.VS_WORLD_DATA.getStorage()
                        .writeNBT(ValkyrienSkiesMod.VS_WORLD_DATA, inst, null);
                }

                @Override
                public void deserializeNBT(NBTBase nbt) {
                    // Otherwise its old, then ignore it
                    ValkyrienSkiesMod.VS_WORLD_DATA.getStorage()
                        .readNBT(ValkyrienSkiesMod.VS_WORLD_DATA, inst, null, nbt);
                }
            });
    }

    @SubscribeEvent
    public void onJoin(PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            lastPositions.put(player, new double[]{0D, 256D, 0D});

            if (player.getName()
                .equals("Drake_Eldridge") || player.getName()
                .equals("thebest108") || player.getName()
                .equals("DaPorkChop_")) {
                WorldServer server = (WorldServer) event.player.world;

                // 20% chance of getting memed on!
                if (Math.random() < .2) {
                    server.server.getPlayerList()
                        .sendMessage(new TextComponentString(
                            TextFormatting.BLUE + "An absolute " + TextFormatting.RED
                                + TextFormatting.ITALIC + "legend" + TextFormatting.BLUE
                                + " has arrived! Welcome " + TextFormatting.GOLD
                                + TextFormatting.BOLD + player.getName()));
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreakFirst(BlockEvent event) {
        BlockPos pos = event.getPos();
        Chunk chunk = event.getWorld()
            .getChunk(pos);
        IPhysicsChunk physicsChunk = (IPhysicsChunk) chunk;
        if (physicsChunk.getPhysicsObjectOptional()
            .isPresent()) {
            event.setResult(Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onExplosionStart(ExplosionEvent.Start event) {
        // Only run on server side
        if (!event.getWorld().isRemote) {
            Explosion explosion = event.getExplosion();
            Vector center = new Vector(explosion.x, explosion.y, explosion.z);
            // Explosion radius
            float radius = explosion.size;
            AxisAlignedBB toCheck = new AxisAlignedBB(center.X - radius, center.Y - radius,
                center.Z - radius,
                center.X + radius, center.Y + radius, center.Z + radius);
            // Find nearby ships, we will check if the explosion effects them
            List<PhysicsWrapperEntity> shipsNear = ValkyrienSkiesMod.VW_PHYSICS_MANAGER
                .getManagerForWorld(event.getWorld())
                .getNearbyPhysObjects(toCheck);
            // Process the explosion on the nearby ships
            for (PhysicsWrapperEntity ship : shipsNear) {
                Vector inLocal = new Vector(center);

                ship.getPhysicsObject().shipTransformationManager().getCurrentTickTransform()
                    .transform(inLocal, TransformType.GLOBAL_TO_SUBSPACE);
                // inLocal.roundToWhole();
                Explosion expl = new Explosion(event.getWorld(), null, inLocal.X, inLocal.Y,
                    inLocal.Z, radius, explosion.causesFire, true);

                double waterRange = .6D;

                boolean cancelDueToWater = false;

                for (int x = (int) Math.floor(expl.x - waterRange);
                    x <= Math.ceil(expl.x + waterRange); x++) {
                    for (int y = (int) Math.floor(expl.y - waterRange);
                        y <= Math.ceil(expl.y + waterRange); y++) {
                        for (int z = (int) Math.floor(expl.z - waterRange);
                            z <= Math.ceil(expl.z + waterRange); z++) {
                            if (!cancelDueToWater) {
                                IBlockState state = event.getWorld()
                                    .getBlockState(new BlockPos(x, y, z));
                                if (state.getBlock() instanceof BlockLiquid) {
                                    cancelDueToWater = true;
                                }
                            }
                        }
                    }
                }

                if (!cancelDueToWater) {
                    expl.doExplosionA();
                    event.getExplosion().affectedBlockPositions.addAll(expl.affectedBlockPositions);
                }

            }
        }
    }

    @SubscribeEvent
    public void onEntityTravelToDimension(EntityTravelToDimensionEvent event)   {
        if (event.getEntity() instanceof PhysicsWrapperEntity)  {
            //prevent ships from changing dimensions, because it can and will break everything very badly
            event.setCanceled(true);
        }
    }
}
