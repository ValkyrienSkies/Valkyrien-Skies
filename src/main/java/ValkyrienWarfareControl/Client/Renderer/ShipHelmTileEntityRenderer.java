package ValkyrienWarfareControl.Client.Renderer;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.CoreMod.CallRunnerClient;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class ShipHelmTileEntityRenderer extends TileEntitySpecialRenderer {

	private final Class renderedTileEntityClass;

	public ShipHelmTileEntityRenderer(Class toRender){
		renderedTileEntityClass = toRender;
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float partialTick, int destroyStage) {
		if(tileentity instanceof TileEntityShipHelm){
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

			IBlockState helmState = tileentity.getWorld().getBlockState(tileentity.getPos());
			IBlockState wheelState = ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(0);
			IBlockState compassState = ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(1);
			IBlockState glassState = ValkyrienWarfareControlMod.instance.shipWheel.getStateFromMeta(2);

			int brightness = CallRunnerClient.onGetCombinedLight(tileentity.getWorld(), tileentity.getPos(), 0);

			double multiplier = 2.0D;

			GL11.glTranslated((1D - multiplier) / 2.0D, 0, (1D - multiplier) / 2.0D);
			GL11.glScaled(multiplier, multiplier, multiplier);

			EnumFacing enumfacing = (EnumFacing)helmState.getValue(BlockShipHelm.FACING);

			double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

			double wheelAngle = 0;

			BlockPos spawnPos = tileentity.getWorld().getSpawnPoint();

			Vector compassPoint = new Vector(tileentity.getPos().getX(), tileentity.getPos().getY(), tileentity.getPos().getZ());
			compassPoint.add(1D, 2, 1D);

			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(tileentity.getWorld(), tileentity.getPos());
			if(wrapper != null){
				RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, compassPoint);
			}

			Vector compassDirection = new Vector(compassPoint);
			compassDirection.subtract(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

			if(wrapper != null){
				RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLRotation, compassDirection);
			}

			compassDirection.normalize();
			double compassYaw = Math.atan2(compassDirection.X, compassDirection.Z);

			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), helmState, brightness);

			GL11.glTranslated(0.5D, 0, 0.5D);
			GL11.glRotated(wheelAndCompassStateRotation, 0, 1, 0);
			GL11.glTranslated(-0.5D, 0, -0.5D);

			GL11.glPushMatrix();
			GL11.glTranslated(.5, .52, 0);
			GL11.glRotated(wheelAngle, 0, 0, 1);
			GL11.glTranslated(-.5, -.52, 0);
			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), wheelState, brightness);
			GL11.glPopMatrix();

			double compassRotation = Math.toDegrees(compassYaw) - wheelAndCompassStateRotation;//0;//Math.PI / 2D;

		    GL11.glPushMatrix();
		    GL11.glTranslated(0.5D, 0, 0.5D);
		    GL11.glRotated(compassRotation, 0, 1, 0);
		    GL11.glTranslated(-0.5D, 0, -0.5D);
			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), compassState, brightness);
			GL11.glPopMatrix();

			GlStateManager.enableAlpha();
		    GlStateManager.enableBlend();
			FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, tileentity.getWorld(), glassState, brightness);
			GlStateManager.disableAlpha();
		    GlStateManager.disableBlend();

			GL11.glPopMatrix();

			vertexbuffer.setTranslation(oldX, oldY, oldZ);
		}
	}

}
