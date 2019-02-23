package valkyrienwarfare.addon.control.renderer;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;
import valkyrienwarfare.addon.control.block.BlockAxle;
import valkyrienwarfare.addon.control.block.torque.TileEntityAxle;
import valkyrienwarfare.mod.client.render.GibsAnimationRegistry;

public class AxleTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityAxle> {

    @Override
    public void render(TileEntityAxle tileentity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);
        IBlockState gearState = Minecraft.getMinecraft().world.getBlockState(tileentity.getPos());
        if (gearState.getBlock() instanceof BlockAxle) {
            EnumFacing.Axis facingAxis = gearState.getValue(BlockAxle.AXIS);

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
            GlStateManager.translate(-0.5, -0.5, -0.5);
        }

        double keyframe = 1;
        GibsAnimationRegistry.getAnimation("rotation_train_axle").renderAnimation(keyframe, brightness);

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
    }

}
