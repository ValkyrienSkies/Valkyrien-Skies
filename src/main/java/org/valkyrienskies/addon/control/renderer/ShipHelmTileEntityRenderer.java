package org.valkyrienskies.addon.control.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.block.BlockShipHelm;
import org.valkyrienskies.addon.control.tileentity.TileEntityShipHelm;
import org.valkyrienskies.mod.client.render.GibsModelRegistry;

public class ShipHelmTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityShipHelm> {

    @Override
    public void render(TileEntityShipHelm tileentity, double x, double y, double z,
        float partialTick, int destroyStage,
        float alpha) {
        if (tileentity instanceof TileEntityShipHelm) {
            IBlockState helmState = tileentity.getWorld().getBlockState(tileentity.getPos());

            if (helmState.getBlock() != ValkyrienSkiesControl.INSTANCE.vsControlBlocks.shipHelm) {
                return;
            }

            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();

            GL11.glTranslated(x, y, z);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();

            double smoothCompassDif = (tileentity.compassAngle - tileentity.lastCompassAngle);
            if (smoothCompassDif < -180) {
                smoothCompassDif += 360;
            }
            if (smoothCompassDif > 180) {
                smoothCompassDif -= 360;
            }

            double smoothWheelDif = (tileentity.wheelRotation - tileentity.lastWheelRotation);
            if (smoothWheelDif < -180) {
                smoothWheelDif += 360;
            }
            if (smoothWheelDif > 180) {
                smoothWheelDif -= 360;
            }

            double smoothCompass =
                tileentity.lastCompassAngle + (smoothCompassDif) * partialTick + 180D;
            double smoothWheel = tileentity.lastWheelRotation + (smoothWheelDif) * partialTick;
            BlockPos originPos = tileentity.getPos();

            IBlockState wheelState = ValkyrienSkiesControl.INSTANCE.vsControlBlocks.shipWheel
                .getStateFromMeta(0);
            IBlockState compassState = ValkyrienSkiesControl.INSTANCE.vsControlBlocks.shipWheel
                .getStateFromMeta(1);
            IBlockState glassState = ValkyrienSkiesControl.INSTANCE.vsControlBlocks.shipWheel
                .getStateFromMeta(2);
            IBlockState helmStateToRender = ValkyrienSkiesControl.INSTANCE.vsControlBlocks.shipWheel
                .getStateFromMeta(3);
            // TODO: Better rendering cache
            int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

            double multiplier = 2.0D;
            GL11.glTranslated((1D - multiplier) / 2.0D, 0, (1D - multiplier) / 2.0D);
            GL11.glScaled(multiplier, multiplier, multiplier);
            EnumFacing enumfacing = helmState.getValue(BlockShipHelm.FACING);
            double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

            GL11.glTranslated(0.5D, 0, 0.5D);
            GL11.glRotated(wheelAndCompassStateRotation, 0, 1, 0);
            GL11.glTranslated(-0.5D, 0, -0.5D);
            GibsModelRegistry.renderGibsModel("ship_helm_base", brightness);

            GL11.glPushMatrix();
            GL11.glTranslated(.5, .522, 0);
            GL11.glRotated(smoothWheel, 0, 0, 1);
            GL11.glTranslated(-.5, -.522, 0);
            GibsModelRegistry.renderGibsModel("ship_helm_wheel", brightness);
            GL11.glPopMatrix();

            GL11.glPushMatrix();
            GL11.glTranslated(0.5D, 0, 0.5D);
            GL11.glRotated(smoothCompass, 0, 1, 0);
            GL11.glTranslated(-0.5D, 0, -0.5D);
            GibsModelRegistry.renderGibsModel("ship_helm_dial", brightness);

            GL11.glPopMatrix();
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GibsModelRegistry.renderGibsModel("ship_helm_dial_glass", brightness);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GL11.glPopMatrix();

            GlStateManager.enableLighting();
            GlStateManager.resetColor();
        }
    }

}