package valkyrienwarfare.addon.control.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import valkyrienwarfare.addon.control.block.BlockLiftControl;
import valkyrienwarfare.addon.control.tileentity.TileEntityLiftControl;
import valkyrienwarfare.mod.client.render.GibsAnimationRegistry;

public class LiftControlTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityLiftControl> {

	@Override
	public void render(TileEntityLiftControl tileentity, double x, double y, double z, float partialTick,
			int destroyStage, float alpha) {

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        
        int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);
        IBlockState gearState = Minecraft.getMinecraft().world.getBlockState(tileentity.getPos());
        if (gearState.getBlock() instanceof BlockLiftControl) {
        	EnumFacing facing = gearState.getValue(BlockHorizontal.FACING);

	
			GlStateManager.translate(0.5, 0.5, 0.5);
			switch (facing) {
			case UP:
				GL11.glRotated(90, 1, 0, 0);
				break;
			case DOWN:
				GL11.glRotated(270, 1, 0, 0);
				break;
			case NORTH:
				GL11.glRotated(0, 0, 1, 0);
				break;
			case EAST:
				GL11.glRotated(270, 0, 1, 0);
				break;
			case SOUTH:
				GL11.glRotated(180, 0, 1, 0);
				break;
			case WEST:
				GL11.glRotated(90, 0, 1, 0);
				break;
			}
			GlStateManager.translate(-0.5, -0.5, -0.5);
        }

		double keyframe = ((Minecraft.getMinecraft().world.getTotalWorldTime() + partialTick) % 19) + 1;
		GibsAnimationRegistry.getAnimation("lift_control").renderAnimation(3, brightness);

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
	}

}
