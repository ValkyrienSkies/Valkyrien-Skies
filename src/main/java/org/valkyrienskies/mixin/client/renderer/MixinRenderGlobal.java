package org.valkyrienskies.mixin.client.renderer;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
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
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.ship_handling.IHasShipManager;
import org.valkyrienskies.mod.common.ship_handling.IPhysObjectWorld;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import org.valkyrienskies.mod.proxy.ClientProxy;

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
    public static void drawSelectionBoundingBox(AxisAlignedBB box, float red, float green,
                                                float blue, float alpha) {
    }

    @Shadow
    public abstract void preRenderDamagedBlocks();

    @Shadow
    public abstract void postRenderDamagedBlocks();

    /**
     * aa
     *
     * @author xd
     */
    @Overwrite
    public void drawBlockDamageTexture(Tessellator tessellatorIn, BufferBuilder worldRendererIn,
                                       Entity entityIn, float partialTicks) {
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
                boolean hasBreak = block instanceof BlockChest || block instanceof BlockEnderChest
                        || block instanceof BlockSign || block instanceof BlockSkull;
                if (!hasBreak) {
                    hasBreak = te != null && te.canRenderBreaking();
                }

                if (!hasBreak) {
                    Optional<PhysicsObject> physicsObject = ValkyrienUtils
                            .getPhysoManagingBlock(world, blockpos);
                    if (!physicsObject.isPresent() && (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D)) {
                        iterator.remove();
                    } else {
                        IBlockState iblockstate = this.world.getBlockState(blockpos);
                        if (physicsObject.isPresent()) {
                            physicsObject.get()
                                    .getShipRenderer()
                                    .applyRenderTransform(partialTicks);
                            worldRendererIn.setTranslation(-physicsObject.get()
                                    .getShipRenderer().offsetPos.getX(), -physicsObject.get()
                                    .getShipRenderer().offsetPos.getY(), -physicsObject.get()
                                    .getShipRenderer().offsetPos.getZ());
                        }
                        if (iblockstate.getMaterial() != Material.AIR) {
                            int i = destroyblockprogress.getPartialBlockDamage();
                            TextureAtlasSprite textureatlassprite = this.destroyBlockIcons[i];
                            BlockRendererDispatcher blockrendererdispatcher = this.mc
                                    .getBlockRendererDispatcher();
                            try {
                                blockrendererdispatcher
                                        .renderBlockDamage(iblockstate, blockpos, textureatlassprite,
                                                this.world);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        worldRendererIn.setTranslation(-d0, -d1, -d2);
                        // TODO: Reverse the Matrix Transforms here
                        if (physicsObject.isPresent()) {
                            tessellatorIn.draw();
                            worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
                            physicsObject.get()
                                    .getShipRenderer()
                                    .inverseTransform(partialTicks);
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
    public void drawSelectionBox(EntityPlayer player, RayTraceResult movingObjectPositionIn,
                                 int execute, float partialTicks) {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils
                .getPhysoManagingBlock(player.world, movingObjectPositionIn.getBlockPos());
        if (physicsObject.isPresent()) {
            physicsObject.get()
                    .getShipRenderer()
                    .applyRenderTransform(partialTicks);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder BufferBuilder = tessellator.getBuffer();

            double xOff = (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks)
                    - physicsObject.get()
                    .getShipRenderer().offsetPos.getX();
            double yOff = (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks)
                    - physicsObject.get()
                    .getShipRenderer().offsetPos.getY();
            double zOff = (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks)
                    - physicsObject.get()
                    .getShipRenderer().offsetPos.getZ();

            BufferBuilder.xOffset += xOff;
            BufferBuilder.yOffset += yOff;
            BufferBuilder.zOffset += zOff;

            this.drawSelectionBoxOriginal(player, movingObjectPositionIn, execute, partialTicks);

            BufferBuilder.xOffset -= xOff;
            BufferBuilder.yOffset -= yOff;
            BufferBuilder.zOffset -= zOff;

            physicsObject.get()
                    .getShipRenderer()
                    .inverseTransform(partialTicks);
        } else {
            this.drawSelectionBoxOriginal(player, movingObjectPositionIn, execute, partialTicks);
        }
    }

    private void drawSelectionBoxOriginal(EntityPlayer player,
                                          RayTraceResult movingObjectPositionIn,
                                          int execute, float partialTicks) {
        if (execute == 0 && movingObjectPositionIn.typeOfHit == RayTraceResult.Type.BLOCK) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            BlockPos blockpos = movingObjectPositionIn.getBlockPos();
            IBlockState iblockstate = this.world.getBlockState(blockpos);

            if (iblockstate.getMaterial() != Material.AIR && this.world.getWorldBorder()
                    .contains(blockpos)) {
                double d0 =
                        player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
                double d1 =
                        player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
                double d2 =
                        player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
                drawSelectionBoundingBox(iblockstate.getSelectedBoundingBox(this.world, blockpos)
                        .grow(0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }

    @Inject(method = "renderEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V", at = @At("HEAD"))
    private void preRenderEntities(Entity renderViewEntity, ICamera camera, float partialTicks,
                                   CallbackInfo callbackInfo) {
        ClientProxy.lastCamera = camera;
    }

    @Inject(method = "renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I", at = @At("HEAD"))
    private void preRenderBlockLayer(BlockRenderLayer blockLayerIn, double partialTicks, int pass,
                                     Entity entityIn, CallbackInfoReturnable callbackInfo) {
        RenderHelper.disableStandardItemLighting();

        IPhysObjectWorld physObjectWorld = ((IHasShipManager) Minecraft.getMinecraft().world).getManager();
        for (PhysicsObject wrapper : physObjectWorld.getAllLoadedPhysObj()) {
            GL11.glPushMatrix();
            if (wrapper.getShipRenderer() != null && wrapper
                    .getShipRenderer().shouldRender()) {
                wrapper.getShipRenderer()
                        .renderBlockLayer(blockLayerIn, partialTicks, pass);
            }
            GL11.glPopMatrix();
        }

        GlStateManager.resetColor();
    }

    @Inject(method = "markBlocksForUpdate", at = @At("HEAD"))
    private void preMarkBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
        boolean updateImmediately, CallbackInfo ci) {

        Optional<PhysicsObject> physicsObject =
            ValkyrienUtils.getPhysoManagingBlock(world, new BlockPos(minX, minY, minZ));
        physicsObject.ifPresent(p ->
            p.getShipRenderer().updateRange(minX, minY, minZ, maxX, maxY, maxZ, updateImmediately));
    }
}
