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

package valkyrienwarfare.mixin.client.renderer;

import java.util.Iterator;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.proxy.ClientProxy;
import valkyrienwarfare.physics.data.TransformType;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Shadow
    @Final
    public Map<Integer, DestroyBlockProgress> damagedBlocks;

    @Shadow
    @Final
    public TextureManager renderEngine;

    @Shadow
    @Final
    public TextureAtlasSprite[] destroyBlockIcons;

    @Shadow
    @Final
    public Minecraft mc;

    @Shadow
    public WorldClient world;
    private PhysicsWrapperEntity wrapperEntity;

    @Shadow
    public static void drawSelectionBoundingBox(AxisAlignedBB box, float red, float green, float blue, float alpha) {
    }

    @Shadow
    public abstract void preRenderDamagedBlocks();

    @Shadow
    public abstract void postRenderDamagedBlocks();

    //TODO: fix the rest of the mod to test this
    /*@Inject(method = "drawBlockDamageTexture",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
                    ordinal = 1),
            locals = LocalCapture.PRINT)
    public void preGetBlockStateDamage(Tessellator tessellatorIn, BufferBuilder bufferBuilderIn, Entity entityIn, float partialTicks, CallbackInfo callbackInfo) {
        //this.wrapperEntity = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this.world, blockpos);
    }*/

    /**
     * aa
     *
     * @author xd
     */
    @Overwrite
    public void drawBlockDamageTexture(Tessellator tessellatorIn, BufferBuilder worldRendererIn, Entity entityIn, float partialTicks) {
        double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks;
        double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks;
        double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks;

        if (!this.damagedBlocks.isEmpty()) {
            this.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            this.preRenderDamagedBlocks();
            worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
            worldRendererIn.setTranslation(-d0, -d1, -d2);
            worldRendererIn.noColor();
            Iterator<DestroyBlockProgress> iterator = this.damagedBlocks.values().iterator();

            while (iterator.hasNext()) {
                DestroyBlockProgress destroyblockprogress = iterator.next();
                BlockPos blockpos = destroyblockprogress.getPosition();
                double d3 = blockpos.getX() - d0;
                double d4 = blockpos.getY() - d1;
                double d5 = blockpos.getZ() - d2;
                Block block = this.world.getBlockState(blockpos).getBlock();
                TileEntity te = this.world.getTileEntity(blockpos);
                boolean hasBreak = block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull;
                if (!hasBreak)
                    hasBreak = te != null && te.canRenderBreaking();

                if (!hasBreak) {
                    PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this.world, blockpos);
                    if (wrapper == null && (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D)) {
                        iterator.remove();
                    } else {
                        IBlockState iblockstate = this.world.getBlockState(blockpos);
                        if (wrapper != null) {
                            wrapper.wrapping.renderer.setupTranslation(partialTicks);
                            worldRendererIn.setTranslation(-wrapper.wrapping.renderer.offsetPos.getX(), -wrapper.wrapping.renderer.offsetPos.getY(), -wrapper.wrapping.renderer.offsetPos.getZ());
                        }
                        if (iblockstate.getMaterial() != Material.AIR) {
                            int i = destroyblockprogress.getPartialBlockDamage();
                            TextureAtlasSprite textureatlassprite = this.destroyBlockIcons[i];
                            BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
                            try {
                                blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, this.world);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        worldRendererIn.setTranslation(-d0, -d1, -d2);
                        // TODO: Reverse the Matrix Transforms here
                        if (wrapper != null) {
                            tessellatorIn.draw();
                            worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
                            wrapper.wrapping.renderer.inverseTransform(partialTicks);
                        }
                    }
                }
            }

            tessellatorIn.draw();
            worldRendererIn.setTranslation(0.0D, 0.0D, 0.0D);
            this.postRenderDamagedBlocks();
        }
    }

    /**
     * aa
     *
     * @author xd
     */
    @Overwrite
    public void drawSelectionBox(EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks) {
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, movingObjectPositionIn.getBlockPos());
        if (wrapper != null && wrapper.wrapping != null && wrapper.wrapping.renderer != null && wrapper.wrapping.renderer.offsetPos != null) {
            wrapper.wrapping.renderer.setupTranslation(partialTicks);

            Minecraft.getMinecraft().entityRenderer.getMouseOver(partialTicks);

            movingObjectPositionIn = Minecraft.getMinecraft().objectMouseOver;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder BufferBuilder = tessellator.getBuffer();

            double xOff = (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks) - wrapper.wrapping.renderer.offsetPos.getX();
            double yOff = (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks) - wrapper.wrapping.renderer.offsetPos.getY();
            double zOff = (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks) - wrapper.wrapping.renderer.offsetPos.getZ();

            BufferBuilder.xOffset += xOff;
            BufferBuilder.yOffset += yOff;
            BufferBuilder.zOffset += zOff;

            this.drawSelectionBoxOriginal(player, movingObjectPositionIn, execute, partialTicks);

            BufferBuilder.xOffset -= xOff;
            BufferBuilder.yOffset -= yOff;
            BufferBuilder.zOffset -= zOff;

            wrapper.wrapping.renderer.inverseTransform(partialTicks);
        } else {
            this.drawSelectionBoxOriginal(player, movingObjectPositionIn, execute, partialTicks);
        }
    }

    public void drawSelectionBoxOriginal(EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks) {
        if (execute == 0 && movingObjectPositionIn.typeOfHit == RayTraceResult.Type.BLOCK) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            BlockPos blockpos = movingObjectPositionIn.getBlockPos();
            IBlockState iblockstate = this.world.getBlockState(blockpos);

            if (iblockstate.getMaterial() != Material.AIR && this.world.getWorldBorder().contains(blockpos)) {
                double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
                double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
                double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
                drawSelectionBoundingBox(iblockstate.getSelectedBoundingBox(this.world, blockpos).grow(0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }

    @Inject(method = "renderEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V", at = @At("HEAD"))
    public void preRenderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo callbackInfo) {
        ClientProxy.lastCamera = camera;
    }

    @Inject(method = "renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I", at = @At("HEAD"))
    public void preRenderBlockLayer(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn, CallbackInfoReturnable callbackInfo) {
        RenderHelper.disableStandardItemLighting();

        for (PhysicsWrapperEntity wrapper : ValkyrienWarfareMod.physicsManager.getManagerForWorld(this.world).physicsEntities) {
            GL11.glPushMatrix();
            if (wrapper.wrapping.renderer != null && wrapper.wrapping.renderer.shouldRender()) {
                wrapper.wrapping.renderer.renderBlockLayer(blockLayerIn, partialTicks, pass);
            }
            GL11.glPopMatrix();
        }

		/*
        if (blockLayerIn == BlockRenderLayer.TRANSLUCENT) {
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			GlStateManager.colorMask(false, false, false, false);
//	        GlStateManager.depthMask(false);
			GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 255);
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
			GL11.glStencilMask(255);
			GL11.glDepthMask(false);
			GlStateManager.clear(GL11.GL_STENCIL_BUFFER_BIT);
			
			for (PhysicsWrapperEntity wrapper : ValkyrienWarfareMod.physicsManager.getManagerForWorld(this.world).physicsEntities) {
				GL11.glPushMatrix();
				if (wrapper.wrapping.renderer != null && wrapper.wrapping.renderer.shouldRender()) {
//	                wrapper.wrapping.renderer.renderBlockLayer(BlockRenderLayer.SOLID, partialTicks, pass);
				}
				GL11.glPopMatrix();
			}
			
			GlStateManager.colorMask(true, true, true, true);
			GL11.glStencilMask(0);
			GL11.glStencilFunc(GL11.GL_NOTEQUAL, 1, 255);
			GL11.glDepthMask(true);
		}
		*/

        GlStateManager.resetColor();
        //vanilla code follows
    }


    @Inject(method = "renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I", at = @At("RETURN"))
    public void postRenderBlockLayer(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn, CallbackInfoReturnable callbackInfo) {
        /*
		if (blockLayerIn == BlockRenderLayer.TRANSLUCENT) {
			GL11.glDisable(GL11.GL_STENCIL_TEST);
			GlStateManager.popMatrix();
		}
		*/
    }

    /**
     * aa
     *
     * @author xd
     */
    //@Overwrite
    public Particle spawnEntityFX(int particleID, boolean ignoreRange, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        if (ValkyrienWarfareMod.shipsSpawnParticles) {
            BlockPos particlePos = new BlockPos(x, y, z);
            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this.world, particlePos);
            if (wrapper != null) {
                Vector newCoords = new Vector(x, y, z);
//                RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, newCoords);
                wrapper.wrapping.coordTransform.getCurrentTickTransform().transform(newCoords, TransformType.LOCAL_TO_GLOBAL);
                x = newCoords.X;
                y = newCoords.Y;
                z = newCoords.Z;
            }
        }
        //vanilla code follows
        return this.spawnParticle0(particleID, ignoreRange, false, x, y, z, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Shadow
    public Particle spawnParticle0(int particleID, boolean ignoreRange, boolean minParticles, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        return null;
    }
}
