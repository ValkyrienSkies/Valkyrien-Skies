package valkyrienwarfare.addon.control.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityRudderAxlePart;
import valkyrienwarfare.math.RotationMatrices;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.client.render.FastBlockModelRenderer;
import valkyrienwarfare.mod.client.render.GibsModelRegistry;

public class RudderAxlePartTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityRudderAxlePart> {

	@Override
	public void render(TileEntityRudderAxlePart tileentity, double x, double y, double z, float partialTick,
			int destroyStage, float alpha) {
		double RUDDER_AXLE_SCALE_FACTOR = 4D;
		
		Tessellator tessellator = Tessellator.getInstance();
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

        GlStateManager.pushMatrix();
        if (!tileentity.isPartOfAssembledMultiblock()) {
        	GL11.glTranslated(0.5, 0, 0.5);
        	GL11.glScaled(RUDDER_AXLE_SCALE_FACTOR, 1, RUDDER_AXLE_SCALE_FACTOR);
			GL11.glTranslated(-0.5, 0, -0.5);
        	GibsModelRegistry.renderGibsModel("rudder_axel_geo", brightness);
		} else {
			if (tileentity.isMaster()) {
				IBlockState state = Blocks.DISPENSER.getDefaultState();
				
				EnumFacing axleAxis = tileentity.getRudderAxleAxisDirection().get();
				EnumFacing axleFacing = tileentity.getRudderAxleFacingDirection().get();
				
				double pitchRot = 0;
				double rollRot = 0;
				switch (axleAxis) {
					case DOWN:
						pitchRot = 180;
						break;
					case UP:
						// No changes to pitch or roll
						break;
					case NORTH:
						pitchRot = -90;
						break;
					case SOUTH:
						pitchRot = 90;
						break;
					case WEST:
						pitchRot = -90;
						rollRot = 90;
						break;
					case EAST:
						pitchRot = -90;
						rollRot = -90;
						break;
				}

				Vector facingDirection = new Vector(axleFacing.getDirectionVec().getX(),
						axleFacing.getDirectionVec().getY(), axleFacing.getDirectionVec().getZ());
				
				double[] rotationMatrix = RotationMatrices.getRotationMatrix(-pitchRot, 0, 0);
				// I'll be honest this doesn't make much sense, but it shouldn't matter anywhere outside of here anyway.
				double[] rotationMatrix2 = RotationMatrices.getRotationMatrix(0, -rollRot, 0);
				RotationMatrices.applyTransform(rotationMatrix2, facingDirection);
				RotationMatrices.applyTransform(rotationMatrix, facingDirection);
				
				EnumFacing facingDirectionNew = EnumFacing.getFacingFromVector((float) facingDirection.X, (float) facingDirection.Y, (float) facingDirection.Z);
				
				// Pitch rotation
				GL11.glTranslated(0, 0.5, 0.5);
				GL11.glRotated(pitchRot, 1, 0, 0);
				GL11.glTranslated(0, -0.5, -0.5);
				// Roll rotation
				GL11.glTranslated(0.5, 0.5, 0);
				GL11.glRotated(rollRot, 0, 0, 1);
				GL11.glTranslated(-0.5, -0.5, 0);
				
				GL11.glPushMatrix();
				
				double rotYaw = -facingDirectionNew.getHorizontalAngle() + 180;
				// Yaw rotation
				GL11.glTranslated(.5, 0, .5);
				GL11.glRotated(rotYaw, 0, 1, 0);
				GL11.glTranslated(-.5, 0, -.5);
				
				// Render rudder cloth code goes here:
				GL11.glPushMatrix();
				GL11.glTranslated(.5, 0, .5);
				GL11.glRotated(tileentity.getRenderRudderAngle(partialTick) + 90, 0, 1, 0);
				GL11.glTranslated(-.5, 0, -.5);
				// GL11.glTranslated(0, 0, -1);
				// Temp while I wait for Del
				
				double rudderAxelLength = tileentity.getRudderAxleLength().get();
				
				GL11.glPushMatrix();
				GL11.glTranslated(.5, 0, .5);
				GL11.glScaled(rudderAxelLength, rudderAxelLength, rudderAxelLength);
				GL11.glTranslated(-.5, 0, -.5);

				GibsModelRegistry.renderGibsModel("rudder_geo", brightness);
				GL11.glPopMatrix();
				GL11.glPopMatrix();

				for (int i = 0; i < tileentity.getRudderAxleLength().get(); i++) {
					// FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), state, brightness);
					GL11.glPushMatrix();
					// To center the axle relative to the rotating rudder.
					// GL11.glTranslated(0, 0, RUDDER_OFFSET);

					GL11.glTranslated(0.5, 0.5, 0.5);
		        	GL11.glScaled(RUDDER_AXLE_SCALE_FACTOR, 1, RUDDER_AXLE_SCALE_FACTOR);
					GL11.glTranslated(-0.5, -0.5, -0.5);
					GibsModelRegistry.renderGibsModel("rudder_axel_geo", brightness);
					GL11.glPopMatrix();
					GL11.glTranslated(0, 1, 0);
				}
				GL11.glPopMatrix();
			}
		}

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
	}
}
