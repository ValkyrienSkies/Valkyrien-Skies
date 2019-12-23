package org.valkyrienskies.addon.control.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.block.BlockSpeedTelegraph;
import org.valkyrienskies.addon.control.tileentity.TileEntitySpeedTelegraph;
import org.valkyrienskies.mod.client.render.GibsModelRegistry;

public class SpeedTelegraphTileEntityRenderer extends
    TileEntitySpecialRenderer<TileEntitySpeedTelegraph> {

    @Override
    public void render(TileEntitySpeedTelegraph tileentity, double x, double y, double z,
        float partialTick, int destroyStage, float alpha) {
        IBlockState telegraphState = tileentity.getWorld().getBlockState(tileentity.getPos());

        if (telegraphState.getBlock()
            != ValkyrienSkiesControl.INSTANCE.vsControlBlocks.speedTelegraph) {
            return;
        }

        GlStateManager.resetColor();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder tessellatorBuffer = tessellator.getBuffer();

        double oldX = tessellatorBuffer.xOffset;
        double oldY = tessellatorBuffer.yOffset;
        double oldZ = tessellatorBuffer.zOffset;

        tessellatorBuffer.setTranslation(0, 0, 0);
        GL11.glTranslated(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        BlockPos originPos = tileentity.getPos();

        int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

        double multiplier = 1.9D;

        GL11.glTranslated((1D - multiplier) / 2.0D, 0, (1D - multiplier) / 2.0D);
        GL11.glScaled(multiplier, multiplier, multiplier);
        EnumFacing enumfacing = telegraphState.getValue(BlockSpeedTelegraph.FACING);
        double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

        GL11.glTranslated(0.5D, 0, 0.5D);
        GL11.glRotated(wheelAndCompassStateRotation, 0, 1, 0);
        GL11.glTranslated(-0.5D, 0, -0.5D);

        // FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), helmStateToRender, brightness);

        GibsModelRegistry.renderGibsModel("chadburn_speed_telegraph_simplevoxel_geo", brightness);

        // FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), dialState, brightness);

        GibsModelRegistry.renderGibsModel("chadburn_dial_simplevoxel_geo", brightness);

        GL11.glPushMatrix();

        GL11.glTranslated(0.497D, 0.857D, 0.5D);
        GL11.glRotated(tileentity.getHandleRenderRotation(partialTick), 0D, 0D, 1D);
        GL11.glTranslated(-0.497D, -0.857D, -0.5D);

        // FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), rightHandleState, brightness);

        GibsModelRegistry.renderGibsModel("chadburn_handles_simplevoxel_geo", brightness);

        // FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), leftHandleState, brightness);

        GL11.glPopMatrix();

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), glassState, brightness);
        GibsModelRegistry.renderGibsModel("chadburn_glass_simplevoxel_geo", brightness);

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        GL11.glPopMatrix();
        tessellatorBuffer.setTranslation(oldX, oldY, oldZ);

        GlStateManager.enableLighting();
        GlStateManager.resetColor();
    }

}