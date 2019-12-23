package org.valkyrienskies.mod.client;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.fixes.SoundFixWrapper;
import org.valkyrienskies.mod.client.render.GibsModelRegistry;
import org.valkyrienskies.mod.client.render.infuser_core_rendering.InfuserCoreBakedModel;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.interaction.EntityDraggable;
import org.valkyrienskies.mod.common.ship_handling.IHasShipManager;
import org.valkyrienskies.mod.common.util.VSRenderUtils;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

public class EventsClient {

    private static double oldXOff;
    private static double oldYOff;
    private static double oldZOff;

    public static void updatePlayerMouseOver(Entity entity) {
        if (entity == Minecraft.getMinecraft().player) {
            Minecraft.getMinecraft().entityRenderer
                .getMouseOver(Minecraft.getMinecraft().getRenderPartialTicks());
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        World world = Minecraft.getMinecraft().world;
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
                // Tick the IShipManager on the world client.
                IHasShipManager shipManager = (IHasShipManager) world;
                shipManager.getManager().tick();
                EntityDraggable.tickAddedVelocityForWorld(world);
                break;
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
                Vector newSoundLocation = new Vector(sound.getXPosF(), sound.getYPosF(),
                    sound.getZPosF());
                physicsObject.get()
                    .getShipTransformationManager()
                    .getCurrentTickTransform()
                    .transform(newSoundLocation, TransformType.SUBSPACE_TO_GLOBAL);

                SoundFixWrapper soundFix = new SoundFixWrapper(sound, newSoundLocation);

                event.setResultSound(soundFix);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTickEvent(ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            if (!mc.isGamePaused()) {
                /*
                WorldPhysObjectManager manager = ValkyrienSkiesMod.VS_PHYSICS_MANAGER
                    .getManagerForWorld(mc.world);
                if (event.phase == Phase.END) {
                    for (PhysicsWrapperEntity wrapper : manager.physicsEntities) {
                        wrapper.getPhysicsObject().onPostTickClient();
                    }
                    EntityDraggable.tickAddedVelocityForWorld(mc.world);
                }

                 */
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

        ResourceLocation modelResourceLocation = new ResourceLocation(ValkyrienSkiesMod.MOD_ID,
            "item/infuser_core_main");
        try {
            IModel model = ModelLoaderRegistry.getModel(modelResourceLocation);
            IBakedModel inventoryModel = model
                .bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                    ModelLoader.defaultTextureGetter());
            IBakedModel handModel = event.getModelRegistry()
                .getObject(new ModelResourceLocation(
                    ValkyrienSkiesMod.MOD_ID + ":" + ValkyrienSkiesMod.INSTANCE.physicsCore
                        .getTranslationKey()
                        .substring(5), "inventory"));

            event.getModelRegistry()
                .putObject(
                    new ModelResourceLocation(ValkyrienSkiesMod.MOD_ID + ":testmodel", "inventory"),
                    new InfuserCoreBakedModel(handModel, inventoryModel));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        ResourceLocation mainCoreInventoryTexture = new ResourceLocation(ValkyrienSkiesMod.MOD_ID,
            "items/main_core");
        ResourceLocation smallCoreInventoryTexture = new ResourceLocation(ValkyrienSkiesMod.MOD_ID,
            "items/small_core");
        event.getMap()
            .registerSprite(mainCoreInventoryTexture);
        event.getMap()
            .registerSprite(smallCoreInventoryTexture);
    }

    @SubscribeEvent
    public void onRenderTickEvent(RenderTickEvent event) {
        World world = Minecraft.getMinecraft().world;
        if (world == null) {
            return; // No ships to worry about.
        }
        double partialTicks = event.renderTickTime;
        if (Minecraft.getMinecraft().isGamePaused()) {
            partialTicks = Minecraft.getMinecraft().renderPartialTicksPaused;
        }

        if (event.phase == Phase.START) {
            for (PhysicsObject wrapper : ValkyrienUtils.getPhysosLoadedInWorld(world)) {
                wrapper.getShipTransformationManager().updateRenderTransform(partialTicks);
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
                VSRenderUtils.getEntityPartialPosition(mc.player, partialTicks).negate();

            for (PhysicsObject physo : ValkyrienUtils.getPhysosLoadedInWorld(world)) {
                physo.getShipRenderer().renderDebugInfo(offset);
            }
        }
    }
}
