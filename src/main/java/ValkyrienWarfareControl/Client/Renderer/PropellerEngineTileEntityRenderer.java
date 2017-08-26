package ValkyrienWarfareControl.Client.Renderer;

import ValkyrienWarfareBase.API.Block.Engine.BlockAirshipEngine;
import ValkyrienWarfareBase.Render.FastBlockModelRenderer;
import ValkyrienWarfareControl.TileEntity.TileEntityPropellerEngine;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

public class PropellerEngineTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityPropellerEngine> {

	@Override
	public void renderTileEntityAt(TileEntityPropellerEngine tileentity, double x, double y, double z, float partialTick, int destroyStage) {

		IBlockState state = tileentity.getWorld().getBlockState(tileentity.getPos());
		if (state.getBlock() instanceof BlockAirshipEngine) {
			EnumFacing facing = (EnumFacing) state.getValue(BlockAirshipEngine.FACING);

			IBlockState engineRenderState = getRenderState(state);
			IBlockState propellerRenderState = ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(14);

			this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer vertexbuffer = tessellator.getBuffer();

			double oldX = vertexbuffer.xOffset;
			double oldY = vertexbuffer.yOffset;
			double oldZ = vertexbuffer.zOffset;

			vertexbuffer.setTranslation(0, 0, 0);
			GL11.glTranslated(x, y, z);
			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();

			int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

//	        GL11.glScaled(1.2D, 1.2D, 1.2D);

			GL11.glTranslated(0.5D, 0.5D, 0.5D);

			switch (facing) {
				case UP:
					GL11.glRotated(-90, 1, 0, 0);
					break;
				case DOWN:
					GL11.glRotated(90, 1, 0, 0);
					break;
				case NORTH:
					GL11.glRotated(180, 0, 1, 0);
					break;
				case EAST:
					GL11.glRotated(90, 0, 1, 0);
					break;
				case SOUTH:
					GL11.glRotated(0, 0, 1, 0);
					break;
				case WEST:
					GL11.glRotated(270, 0, 1, 0);
					break;

			}

			GL11.glTranslated(-0.5D, -0.5D, -0.5D);

			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), engineRenderState, brightness);

			GL11.glPushMatrix();

			GL11.glTranslated(0.5D, 0.21D, 0.5D);
			GL11.glRotated(Math.random() * 360D, 0, 0, 1);
			GL11.glScaled(1.5D, 1.5D, 1);
			GL11.glTranslated(-0.5D, -0.21D, -0.5D);


			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), propellerRenderState, brightness);

			GL11.glPopMatrix();

			GL11.glPopMatrix();

			vertexbuffer.setTranslation(oldX, oldY, oldZ);
		}
	}

	private IBlockState getRenderState(IBlockState inWorldState) {
		if (inWorldState.getBlock() == ValkyrienWarfareControlMod.instance.ultimateEngine) {
			return ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(9);
		}
		if (inWorldState.getBlock() == ValkyrienWarfareControlMod.instance.redstoneEngine) {
			return ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(10);
		}
		if (inWorldState.getBlock() == ValkyrienWarfareControlMod.instance.eliteEngine) {
			return ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(11);
		}
		if (inWorldState.getBlock() == ValkyrienWarfareControlMod.instance.basicEngine) {
			return ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(12);
		}
		if (inWorldState.getBlock() == ValkyrienWarfareControlMod.instance.advancedEngine) {
			return ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(13);
		}

		return ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(9);
	}
}
