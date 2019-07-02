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

package valkyrienwarfare.mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import org.lwjgl.opengl.GL11;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.fixes.SoundFixWrapper;
import valkyrienwarfare.mod.client.render.GibsModelRegistry;
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;
import valkyrienwarfare.mod.common.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.mod.common.physics.management.WorldPhysObjectManager;
import valkyrienwarfare.mod.common.physmanagement.interaction.EntityDraggable;
import valkyrienwarfare.mod.common.util.ValkyrienUtils;

import java.util.Optional;

public class EventsClient {

    private static double oldXOff;
    private static double oldYOff;
    private static double oldZOff;

    public static void updatePlayerMouseOver(Entity entity) {
        if (entity == Minecraft.getMinecraft().player) {
            Minecraft.getMinecraft().entityRenderer.getMouseOver(Minecraft.getMinecraft().getRenderPartialTicks());
        }
    }

    @SubscribeEvent
    public void onPlaySoundEvent(PlaySoundEvent event) {
        if (Minecraft.getMinecraft().world != null) {
            ISound sound = event.getSound();
            BlockPos pos = new BlockPos(sound.getXPosF(), sound.getYPosF(), sound.getZPosF());

            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(Minecraft.getMinecraft().world, pos);
            if (physicsObject.isPresent()) {
                Vector newSoundLocation = new Vector(sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
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
                WorldPhysObjectManager manager = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getManagerForWorld(mc.world);
                if (event.phase == Phase.END) {
                    for (PhysicsWrapperEntity wrapper : manager.physicsEntities) {
                        wrapper.getPhysicsObject().onPostTickClient();
                    }
                    EntityDraggable.tickAddedVelocityForWorld(mc.world);
                }
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onDrawBlockHighlightEventFirst(DrawBlockHighlightEvent event) {
        GL11.glPushMatrix();
        BlockPos pos = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
        if (pos != null) {
            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(Minecraft.getMinecraft().world, pos);
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
                            .setupTranslation(event.getPartialTicks());
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onDrawBlockHighlightEventLast(DrawBlockHighlightEvent event) {
        BlockPos pos = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
        if (pos != null) {
            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(Minecraft.getMinecraft().world, pos);
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
        }
        GL11.glPopMatrix();
    }

    /**
     * Register textures for all the models registered in the GibsModelRegistry.
     *
     * @param event
     */
    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        GibsModelRegistry.registerTextures(event);
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        GibsModelRegistry.onModelBakeEvent(event);
    }
}
