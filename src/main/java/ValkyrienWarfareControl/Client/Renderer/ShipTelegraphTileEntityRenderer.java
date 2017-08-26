package ValkyrienWarfareControl.Client.Renderer;

import ValkyrienWarfareBase.Render.FastBlockModelRenderer;
import ValkyrienWarfareControl.Block.BlockShipTelegraph;
import ValkyrienWarfareControl.TileEntity.TileEntityShipTelegraph;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class ShipTelegraphTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityShipTelegraph> {

	private final Class renderedTileEntityClass;

	public ShipTelegraphTileEntityRenderer(Class toRender) {
		renderedTileEntityClass = toRender;
	}

	@Override
	public void renderTileEntityAt(TileEntityShipTelegraph tileentity, double x, double y, double z, float partialTick, int destroyStage) {
		IBlockState telegraphState = tileentity.getWorld().getBlockState(tileentity.getPos());

		if (telegraphState.getBlock() != ValkyrienWarfareControlMod.INSTANCE.shipTelegraph) {
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

		BlockPos originPos = tileentity.getPos();

		IBlockState glassState = ValkyrienWarfareControlMod.INSTANCE.shipWheel.getStateFromMeta(8);
		IBlockState dialState = ValkyrienWarfareControlMod.INSTANCE.shipWheel.getStateFromMeta(7);
		IBlockState leftHandleState = ValkyrienWarfareControlMod.INSTANCE.shipWheel.getStateFromMeta(6);
		IBlockState rightHandleState = ValkyrienWarfareControlMod.INSTANCE.shipWheel.getStateFromMeta(5);
		IBlockState helmStateToRender = ValkyrienWarfareControlMod.INSTANCE.shipWheel.getStateFromMeta(4);
		int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

		double multiplier = 1.5D;

		GL11.glTranslated((1D - multiplier) / 2.0D, 0, (1D - multiplier) / 2.0D);
		GL11.glScaled(multiplier, multiplier, multiplier);
		EnumFacing enumfacing = telegraphState.getValue(BlockShipTelegraph.FACING);
		double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

		GL11.glTranslated(0.5D, 0, 0.5D);
		GL11.glRotated(wheelAndCompassStateRotation, 0, 1, 0);
		GL11.glTranslated(-0.5D, 0, -0.5D);

		FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), helmStateToRender, brightness);

		FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), dialState, brightness);

		GL11.glPushMatrix();

		GL11.glTranslated(0.497D, 0.857D, 0.5D);
		GL11.glRotated(tileentity.getHandleRenderRotation(), 0D, 0D, 1D);
		GL11.glTranslated(-0.497D, -0.857D, -0.5D);

		FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), rightHandleState, brightness);
		FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), leftHandleState, brightness);

		GL11.glPopMatrix();

		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), glassState, brightness);
		GlStateManager.disableAlpha();
		GlStateManager.disableBlend();

		GL11.glPopMatrix();
		vertexbuffer.setTranslation(oldX, oldY, oldZ);

		GlStateManager.enableLighting();
		GlStateManager.resetColor();
	}

}
