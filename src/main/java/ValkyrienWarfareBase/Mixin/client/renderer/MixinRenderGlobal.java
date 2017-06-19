package ValkyrienWarfareBase.Mixin.client.renderer;

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

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.Proxy.ClientProxy;
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
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
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

    @Shadow
    public static void drawSelectionBoundingBox(AxisAlignedBB box, float red, float green, float blue, float alpha) {
    }

    @Shadow
    public abstract void preRenderDamagedBlocks();

    @Shadow
    public abstract void postRenderDamagedBlocks();

    @Overwrite
    public void drawBlockDamageTexture(Tessellator tessellatorIn, VertexBuffer worldRendererIn, Entity entityIn, float partialTicks) {
        double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;

        if (!this.damagedBlocks.isEmpty()) {
            this.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            this.preRenderDamagedBlocks();
            worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
            worldRendererIn.setTranslation(-d0, -d1, -d2);
            worldRendererIn.noColor();
            Iterator<DestroyBlockProgress> iterator = this.damagedBlocks.values().iterator();

            while (iterator.hasNext()) {
                DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress) iterator.next();
                BlockPos blockpos = destroyblockprogress.getPosition();
                double d3 = (double) blockpos.getX() - d0;
                double d4 = (double) blockpos.getY() - d1;
                double d5 = (double) blockpos.getZ() - d2;
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

    @Overwrite
    public void drawSelectionBox(EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks) {
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, movingObjectPositionIn.getBlockPos());
        if (wrapper != null && wrapper.wrapping != null && wrapper.wrapping.renderer != null && wrapper.wrapping.renderer.offsetPos != null) {
            wrapper.wrapping.renderer.setupTranslation(partialTicks);

            Minecraft.getMinecraft().entityRenderer.getMouseOver(partialTicks);

            movingObjectPositionIn = Minecraft.getMinecraft().objectMouseOver;

            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();

            double xOff = (player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks) - wrapper.wrapping.renderer.offsetPos.getX();
            double yOff = (player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks) - wrapper.wrapping.renderer.offsetPos.getY();
            double zOff = (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks) - wrapper.wrapping.renderer.offsetPos.getZ();

            vertexbuffer.xOffset += xOff;
            vertexbuffer.yOffset += yOff;
            vertexbuffer.zOffset += zOff;

            this.drawSelectionBoxOriginal(player, movingObjectPositionIn, execute, partialTicks);

            vertexbuffer.xOffset -= xOff;
            vertexbuffer.yOffset -= yOff;
            vertexbuffer.zOffset -= zOff;

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
                double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
                double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
                double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
                drawSelectionBoundingBox(iblockstate.getSelectedBoundingBox(this.world, blockpos).expandXyz(0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }

    @Inject(method = "renderEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V", at = @At("HEAD"))
    public void preRenderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo callbackInfo) {
        ((ClientProxy) ValkyrienWarfareMod.proxy).lastCamera = camera;
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
        GlStateManager.resetColor();
        //vanilla code follows
    }

    @Overwrite
    public Particle spawnEntityFX(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        if (ValkyrienWarfareMod.shipsSpawnParticles) {
            BlockPos particlePos = new BlockPos(xCoord, yCoord, zCoord);
            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this.world, particlePos);
            if (wrapper != null) {
                Vector newCoords = new Vector(xCoord, yCoord, zCoord);
                RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, newCoords);

                xCoord = newCoords.X;
                yCoord = newCoords.Y;
                zCoord = newCoords.Z;
            }
        }
        //vanilla code follows
        return this.spawnParticle0(particleID, ignoreRange, false, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Shadow
    public Particle spawnParticle0(int particleID, boolean ignoreRange, boolean minParticles, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        return null;
    }
}
