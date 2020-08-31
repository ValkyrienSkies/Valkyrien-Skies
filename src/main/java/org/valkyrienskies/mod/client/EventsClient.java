package org.valkyrienskies.mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.mod.common.entity.EntityShipMovementData;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.fixes.SoundFixWrapper;
import org.valkyrienskies.mod.client.better_portals_compatibility.ClientWorldTracker;
import org.valkyrienskies.mod.client.render.GibsModelRegistry;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityDraggable;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ship_world.IHasShipManager;
import org.valkyrienskies.mod.common.ships.ship_world.IPhysObjectWorld;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.util.VSRenderUtils;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.Optional;
import java.util.WeakHashMap;

public class EventsClient {

    private static double oldXOff;
    private static double oldYOff;
    private static double oldZOff;

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        for (World world : ClientWorldTracker.getWorlds()) {
            if (world == null) {
                // There's no world, so there's nothing to run.
                return;
            }
            // Pretend this is the world tick, because diesieben07 doesn't want WorldClient to make world tick events.
            switch (event.phase) {
                case START:
                    // Nothing for now

                    for (PhysicsObject wrapper : ((IHasShipManager) world).getManager().getAllLoadedPhysObj()) {
                        // This is necessary because Minecraft will run a raytrace right after this
                        // event to determine what the player is looking at for interaction purposes.
                        // That raytrace will use the render transform, so we must have the render
                        // transform set to a partialTick of 1.0.
                        wrapper.getShipTransformationManager()
                                .updateRenderTransform(1.0);
                    }

                    break;
                case END:
                    if (!Minecraft.getMinecraft().isGamePaused()) {
                        // Tick the IShipManager on the world client.
                        IHasShipManager shipManager = (IHasShipManager) world;
                        shipManager.getManager().tick();
                        EntityDraggable.tickAddedVelocityForWorld(world);
                    }
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onPlaySoundEvent(PlaySoundEvent event) {
        if (Minecraft.getMinecraft().world != null) {
            ISound sound = event.getSound();
            BlockPos pos = new BlockPos(sound.getXPosF(), sound.getYPosF(), sound.getZPosF());

            Optional<PhysicsObject> physicsObject = ValkyrienUtils
                .getPhysoManagingBlock(Minecraft.getMinecraft().world, pos);
            if (physicsObject.isPresent()) {
                Vector3d newSoundLocation = new Vector3d(sound.getXPosF(), sound.getYPosF(),
                    sound.getZPosF());
                physicsObject.get()
                    .getShipTransformationManager()
                    .getCurrentTickTransform()
                    .transformPosition(newSoundLocation, TransformType.SUBSPACE_TO_GLOBAL);

                SoundFixWrapper soundFix = new SoundFixWrapper(sound, newSoundLocation);

                event.setResultSound(soundFix);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onDrawBlockHighlightEventFirst(DrawBlockHighlightEvent event) {
        GL11.glPushMatrix();
        BlockPos pos = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysoManagingBlock(Minecraft.getMinecraft().world, pos);
        if (physicsObject.isPresent()) {
            RayTraceResult objectOver = Minecraft.getMinecraft().objectMouseOver;
            if (objectOver != null && objectOver.hitVec != null) {
                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                oldXOff = buffer.xOffset;
                oldYOff = buffer.yOffset;
                oldZOff = buffer.zOffset;

                buffer.setTranslation(-physicsObject.get()
                    .getShipRenderer().offsetPos.getX(), -physicsObject.get()
                    .getShipRenderer().offsetPos.getY(), -physicsObject.get()
                    .getShipRenderer().offsetPos.getZ());
                physicsObject.get()
                    .getShipRenderer()
                    .applyRenderTransform(event.getPartialTicks());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onDrawBlockHighlightEventLast(DrawBlockHighlightEvent event) {
        BlockPos pos = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysoManagingBlock(Minecraft.getMinecraft().world, pos);
        if (physicsObject.isPresent()) {
            RayTraceResult objectOver = Minecraft.getMinecraft().objectMouseOver;
            if (objectOver != null && objectOver.hitVec != null) {
                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                buffer.xOffset = oldXOff;
                buffer.yOffset = oldYOff;
                buffer.zOffset = oldZOff;
                // wrapper.wrapping.renderer.inverseTransform(event.getPartialTicks());
                // objectOver.hitVec = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, objectOver.hitVec);
            }
        }
        GL11.glPopMatrix();
    }

    /**
     * Register textures for all the models registered in the GibsModelRegistry.
     */
    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        GibsModelRegistry.registerTextures(event);
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        GibsModelRegistry.onModelBakeEvent(event);
    }

    // Used to store the lastTickPos variables of entities, that way we can restore them to their original values after
    // the rendering code has finished.
    private static final WeakHashMap<Entity, Vector3dc> lastPositionsMap = new WeakHashMap<>();

    @SubscribeEvent
    public void onRenderTickEvent(RenderTickEvent event) {
        final World world = Minecraft.getMinecraft().world;
        if (world == null) {
            return; // No ships to worry about.
        }
        double partialTicks = event.renderTickTime;
        if (Minecraft.getMinecraft().isGamePaused()) {
            partialTicks = Minecraft.getMinecraft().renderPartialTicksPaused;
        }

        if (event.phase == Phase.START) {
            lastPositionsMap.clear();
            for (PhysicsObject wrapper : ValkyrienUtils.getPhysosLoadedInWorld(world)) {
                wrapper.getShipTransformationManager().updateRenderTransform(partialTicks);
            }

            // region Fix rendering movement of entities on ships

            // All of Minecraft's code assumes that entities follow a straight line path from their previous position to
            // their current position.
            //
            // This assumption is violated by rotating ships, since the path of a point on a rotating body is a curve,
            // not a straight line. At small distances from the center of a ship this doesn't matter, but for large ships
            // the incorrect interpolation results in a bad player experience (jittery rendering resulting from the incorrect interpolation).
            //
            // So, to fix this we calculate the correct interpolated position of the entity, and then we modify the lastTickPos
            // variables so that Minecraft's interpolation code computes the correct value.
            for (final Entity entity : world.getLoadedEntityList()) {
                final EntityShipMovementData entityShipMovementData = ValkyrienUtils.getEntityShipMovementDataFor(entity);
                if (entityShipMovementData.getLastTouchedShip() != null && entityShipMovementData.getTicksSinceTouchedShip() == 0) {
                    final PhysicsObject shipPhysicsObject = ValkyrienUtils.getPhysObjWorld(world).getPhysObjectFromUUID(
                            entityShipMovementData.getLastTouchedShip().getUuid()
                    );
                    if (shipPhysicsObject == null) {
                        System.err.println("shipPhysicsObject was null?");
                        continue;
                    }
                    final ShipTransform prevTickTransform = shipPhysicsObject.getPrevTickShipTransform();
                    final ShipTransform shipRenderTransform = shipPhysicsObject.getShipTransformationManager().getRenderTransform();
                    final Vector3dc entityAddedVelocity = entityShipMovementData.getAddedLinearVelocity();

                    // The velocity the entity was moving without the added velocity from the ship
                    final double entityMovementX = entity.posX - entityAddedVelocity.x() - entity.lastTickPosX;
                    final double entityMovementY = entity.posY - entityAddedVelocity.y() - entity.lastTickPosY;
                    final double entityMovementZ = entity.posZ - entityAddedVelocity.z() - entity.lastTickPosZ;

                    // Compute the position the entity should be rendered at this frame
                    final Vector3d entityShouldBeHere = new Vector3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ);
                    prevTickTransform.transformPosition(entityShouldBeHere, TransformType.GLOBAL_TO_SUBSPACE);
                    shipRenderTransform.transformPosition(entityShouldBeHere, TransformType.SUBSPACE_TO_GLOBAL);
                    entityShouldBeHere.add(entityMovementX * partialTicks, entityMovementY * partialTicks, entityMovementZ * partialTicks);

                    // Save the entity lastTickPos in the map
                    lastPositionsMap.put(entity, new Vector3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ));

                    // Then update lastTickPos such that Minecraft's interpolation code will render entity at entityShouldBeHere.
                    entity.lastTickPosX = (entityShouldBeHere.x() - (entity.posX * partialTicks)) / (1 - partialTicks);
                    entity.lastTickPosY = (entityShouldBeHere.y() - (entity.posY * partialTicks)) / (1 - partialTicks);
                    entity.lastTickPosZ = (entityShouldBeHere.z() - (entity.posZ * partialTicks)) / (1 - partialTicks);
                }
            }
            // endregion
        } else {
            // Once the rendering code has finished we restore the lastTickPos variables to their old values.
            for (final Entity entity : world.getLoadedEntityList()) {
                if (lastPositionsMap.containsKey(entity)) {
                    final Vector3dc entityLastPosition = lastPositionsMap.get(entity);
                    entity.lastTickPosX = entityLastPosition.x();
                    entity.lastTickPosY = entityLastPosition.y();
                    entity.lastTickPosZ = entityLastPosition.z();
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = Minecraft.getMinecraft().world;
        if (mc.getRenderManager().isDebugBoundingBox() && !mc.isReducedDebug() && world != null) {
            float partialTicks = event.getPartialTicks();
            Vector3dc offset =
                VSRenderUtils.getEntityPartialPosition(mc.getRenderViewEntity(), partialTicks).negate();

            for (PhysicsObject physo : ValkyrienUtils.getPhysosLoadedInWorld(world)) {
                physo.getShipRenderer().renderDebugInfo(offset);
            }
        }
    }

    /**
     * Used to handle when a chunk in a ship gets updated. This allows us to create ships on the client without
     * requiring all the chunks are present at the time of ship creation.
     */
    @SubscribeEvent
    public void onChunkLoadEvent(ChunkEvent.Load event) {
        if (!event.getWorld().isRemote) {
            return;
        }
        Chunk chunk = event.getChunk();
        QueryableShipData queryableShipData = QueryableShipData.get(event.getWorld());
        Optional<ShipData> shipClaimingOptional = queryableShipData.getShipFromChunk(chunk.x, chunk.z);
        if (shipClaimingOptional.isPresent()) {
            ShipData shipData = shipClaimingOptional.get();
            IPhysObjectWorld physObjectWorld = ValkyrienUtils.getPhysObjWorld(event.getWorld());
            PhysicsObject physicsObject = physObjectWorld.getPhysObjectFromUUID(shipData.getUuid());
            if (physicsObject != null) {
                physicsObject.updateChunk(chunk);
            }
        }
    }
}
