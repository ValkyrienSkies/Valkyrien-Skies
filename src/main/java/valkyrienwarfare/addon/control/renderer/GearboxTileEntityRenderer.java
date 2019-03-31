package valkyrienwarfare.addon.control.renderer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import valkyrienwarfare.addon.control.block.BlockGearbox;
import valkyrienwarfare.addon.control.block.torque.IRotationNode;
import valkyrienwarfare.addon.control.tileentity.TileEntityGearbox;
import valkyrienwarfare.mod.client.render.GibsAnimationRegistry;
import valkyrienwarfare.mod.client.render.GibsModelRegistry;

import java.util.Map;
import java.util.Optional;

public class GearboxTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityGearbox> {

	private static final ImmutableMap<EnumFacing, String> FACING_TO_AXLE_NAME;

	static {
		FACING_TO_AXLE_NAME = ImmutableMap.<EnumFacing, String>builder()
				.put(EnumFacing.DOWN, "gearboxvtopengineaxel_geo")
				.put(EnumFacing.UP, "gearboxvbottomengineaxel_geo")
				.put(EnumFacing.NORTH, "gearboxvbottomengineaxel_geo")
				.put(EnumFacing.SOUTH, "gearboxvbottomengineaxel_geo")
				.put(EnumFacing.EAST, "gearboxvbottomengineaxel_geo")
				.put(EnumFacing.WEST, "gearboxvbottomengineaxel_geo")
				.build();
	}

	@Override
	public void render(TileEntityGearbox tileentity, double x, double y, double z, float partialTick,
			int destroyStage, float alpha) {

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
		int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);


		ImmutableMap<EnumFacing, String> FACING_TO_AXLE_NAME_TEMP = ImmutableMap.<EnumFacing, String>builder()
				.put(EnumFacing.DOWN, "gearboxvtopengineaxel_geo")
				.put(EnumFacing.UP, "gearboxbottomengineaxel_geo")
				.put(EnumFacing.SOUTH, "gearboxfrontengineaxel_geo")
				.put(EnumFacing.NORTH, "gearboxbackengineaxel_geo")
				.put(EnumFacing.WEST, "gearboxrightengineaxel_geo")
				.put(EnumFacing.EAST, "gearboxleftengineaxel_geo")
				.build();

		// Render the side axles
		float renderRotation = (float) Math.toDegrees(tileentity.getRenderRotationRadians(partialTick));
		// Then render the six sides:
		Optional<Double>[] connectedAngularVelocityRatios = tileentity.getConnectedSidesRatios();
		for (EnumFacing facing : EnumFacing.values()) {
			Optional<Double> ratio = connectedAngularVelocityRatios[facing.ordinal()];
			if (ratio.isPresent()) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(.5, .5, .5);
				Vec3i facingVec = facing.getDirectionVec();
				// The render rotation for this side. Mathematically I can add constants to this and it will still be
				// correct.
				double effectiveRenderRotation = renderRotation * ratio.get();
				GlStateManager.rotate((float) effectiveRenderRotation, facingVec.getX(), facingVec.getY(), facingVec.getZ());
				GlStateManager.translate(-.5, -.5, -.5);
				GibsModelRegistry.renderGibsModel(FACING_TO_AXLE_NAME_TEMP.get(facing), brightness);
				GlStateManager.popMatrix();
			}
		}


        IBlockState gearState = Minecraft.getMinecraft().world.getBlockState(tileentity.getPos());
        if (gearState.getBlock() instanceof BlockGearbox) {
        	EnumFacing facing = gearState.getValue(BlockHorizontal.FACING);
	        GlStateManager.pushMatrix();
	
			GlStateManager.translate(0.5, 0.5, 0.5);
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
			GlStateManager.translate(-0.5, -0.5, -0.5);
        }

		double keyframe = ((Minecraft.getMinecraft().world.getTotalWorldTime() + partialTick) % 99) + 1;
		GibsAnimationRegistry.getAnimation("gearbox").renderAnimation(keyframe, brightness);

		GlStateManager.popMatrix();
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
        GlStateManager.resetColor();
	}
}
