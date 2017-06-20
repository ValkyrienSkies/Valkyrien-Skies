package ValkyrienWarfareControl.Client.Renderer;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareBase.Render.FastBlockModelRenderer;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.Block.BlockShipHelm;
import ValkyrienWarfareControl.TileEntity.TileEntityShipHelm;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class ShipHelmTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityShipHelm> {

	private final Class renderedTileEntityClass;

	public ShipHelmTileEntityRenderer(Class toRender){
		renderedTileEntityClass = toRender;
	}

	@Override
	public void renderTileEntityAt(TileEntityShipHelm tileentity, double x, double y, double z, float partialTick, int destroyStage) {
		if(tileentity instanceof TileEntityShipHelm){
			IBlockState helmState = tileentity.getWorld().getBlockState(tileentity.getPos());

			if(helmState.getBlock() != ValkyrienWarfareControlMod.instance.shipHelm) {
				return;
			}

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

		    double smoothCompassDif = (tileentity.compassAngle - tileentity.lastCompassAngle);
		    if(smoothCompassDif < -180){
		    	smoothCompassDif += 360;
		    }
		    if(smoothCompassDif > 180){
		    	smoothCompassDif -= 360;
		    }

		    double smoothWheelDif = (tileentity.wheelRotation - tileentity.lastWheelRotation);
		    if(smoothWheelDif < -180){
		    	smoothWheelDif += 360;
		    }
		    if(smoothWheelDif > 180){
		    	smoothWheelDif -= 360;
		    }

		    double smoothCompass = tileentity.lastCompassAngle + (smoothCompassDif) * partialTick;
		    double smoothWheel = tileentity.lastWheelRotation + (smoothWheelDif) * partialTick;

			BlockPos originPos = tileentity.getPos();


			IBlockState wheelState = ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(0);
			IBlockState compassState = ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(1);
			IBlockState glassState = ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(2);
			IBlockState helmStateToRender = ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(3);
			//TODO: Better rendering cache
			int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

			double multiplier = 2.0D;
			GL11.glTranslated((1D - multiplier) / 2.0D, 0, (1D - multiplier) / 2.0D);
			GL11.glScaled(multiplier, multiplier, multiplier);
			EnumFacing enumfacing = (EnumFacing)helmState.getValue(BlockShipHelm.FACING);
			double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();


			GL11.glTranslated(0.5D, 0, 0.5D);
			GL11.glRotated(wheelAndCompassStateRotation, 0, 1, 0);
			GL11.glTranslated(-0.5D, 0, -0.5D);

			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), helmStateToRender, brightness);


			GL11.glPushMatrix();
			GL11.glTranslated(.5, .522, 0);
			GL11.glRotated(smoothWheel, 0, 0, 1);
			GL11.glTranslated(-.5, -.522, 0);
			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), wheelState, brightness);
			GL11.glPopMatrix();

		    GL11.glPushMatrix();
		    GL11.glTranslated(0.5D, 0, 0.5D);
		    GL11.glRotated(smoothCompass, 0, 1, 0);
		    GL11.glTranslated(-0.5D, 0, -0.5D);
			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), compassState, brightness);
			GL11.glPopMatrix();

			GlStateManager.enableAlpha();
		    GlStateManager.enableBlend();
		    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), glassState, brightness);
			GlStateManager.disableAlpha();
		    GlStateManager.disableBlend();

			GL11.glPopMatrix();

			vertexbuffer.setTranslation(oldX, oldY, oldZ);
		}
	}

}
