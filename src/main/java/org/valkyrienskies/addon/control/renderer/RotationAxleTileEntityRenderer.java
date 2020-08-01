package org.valkyrienskies.addon.control.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.addon.control.block.BlockRotationAxle;
import org.valkyrienskies.addon.control.block.torque.TileEntityRotationAxle;

public class RotationAxleTileEntityRenderer extends
    TileEntitySpecialRenderer<TileEntityRotationAxle> {

    @Override
    public void render(TileEntityRotationAxle tileentity, double x, double y, double z,
        float partialTick, int destroyStage, float alpha) {
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);
        IBlockState gearState = Minecraft.getMinecraft().world.getBlockState(tileentity.getPos());
        if (gearState.getBlock() instanceof BlockRotationAxle) {
            EnumFacing.Axis facingAxis = gearState.getValue(BlockRotationAxle.AXIS);

            GlStateManager.translate(0.5, 0.5, 0.5);
            switch (facingAxis) {
                case X:
                    // Rotates (1, 0, 0) -> (1, 0, 0)
                    break;
                case Y:
                    // Rotates (1, 0, 0) -> (0, 1, 0)
                    GL11.glRotated(90, 0, 0, 1);
                    break;
                case Z:
                    // Rotates (1, 0, 0) -> (0, 0, 1)
                    GL11.glRotated(-90, 0, 1, 0);
                    break;
            }
            GL11.glRotated(Math.toDegrees(tileentity.getRenderRotationRadians(partialTick)), 1, 0,
                0);
            GlStateManager.translate(-0.5, -0.5, -0.5);
        }

        double keyframe = 1;
        GibsAtomAnimationRegistry.getAnimation("rotation_axle")
            .renderAnimation(keyframe, brightness);

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
    }

}
