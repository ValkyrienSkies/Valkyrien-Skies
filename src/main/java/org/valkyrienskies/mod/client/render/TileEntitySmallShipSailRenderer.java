package org.valkyrienskies.mod.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.block.BlockSmallShipSail;
import org.valkyrienskies.mod.common.tileentity.TileEntitySmallShipSail;

public class TileEntitySmallShipSailRenderer extends TileEntitySpecialRenderer<TileEntitySmallShipSail> {

    private static final double RUDDER_AXLE_SCALE_FACTOR = 2.0;

    @Override
    public void render(TileEntitySmallShipSail tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        final IBlockState blockState = tileEntity.getWorld().getBlockState(tileEntity.getPos());

        if (blockState.getBlock() != ValkyrienSkiesMod.INSTANCE.smallShipSail) return;

        GlStateManager.pushMatrix();

        super.render(tileEntity, x, y, z, partialTicks, destroyStage, alpha);

        GlStateManager.translate(x, y, z);
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        int brightness = tileEntity.getWorld().getCombinedLight(tileEntity.getPos(), 0);

        // Render the sail part
        GlStateManager.pushMatrix();

        // The height of the sail
        double height = 10.0;

        GL11.glTranslated(.5, 0.0, .5);
        GlStateManager.scale(height, height, height);
        GL11.glTranslated(-.5, 0.0, -.5);

        // Render the sail part
        {
            GL11.glPushMatrix();

            final EnumFacing axleFacing = blockState.getValue(BlockSmallShipSail.FACING);
            final double rotYaw = axleFacing.getHorizontalAngle() + 90.0;

            // Roll rotation (Flip rudder upside down to make a sail)
            GL11.glTranslated(.5, .5, .5);
            GL11.glRotated(180.0, 1.0, 0.0, 0.0);
            GL11.glTranslated(-.5, -.5, -.5);

            // Yaw rotation
            GL11.glTranslated(.5, .5, .5);
            GL11.glRotated(rotYaw + tileEntity.getRotationForRendering(partialTicks), 0.0, 1.0, 0.0);
            GL11.glTranslated(-.5, -.5, -.5);

            GibsModelRegistry.renderGibsModel("boats_rudder_geo", brightness);

            GL11.glPopMatrix();
        }

        // Render the axel part
        {
            GL11.glPushMatrix();
            // To center the axle relative to the rotating rudder.
            // GL11.glTranslated(0, 0, RUDDER_OFFSET);
            GL11.glTranslated(0.5, 0.5, 0.5);
            GL11.glScaled(RUDDER_AXLE_SCALE_FACTOR, 1.0, RUDDER_AXLE_SCALE_FACTOR);
            GL11.glTranslated(-0.5, -0.5, -0.5);
            GibsModelRegistry.renderGibsModel("boats_rudder_axle_geo", brightness);
            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
    }
}
