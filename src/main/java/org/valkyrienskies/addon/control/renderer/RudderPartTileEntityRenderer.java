package org.valkyrienskies.addon.control.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import org.joml.AxisAngle4d;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityRudderPart;
import org.valkyrienskies.mod.client.render.GibsModelRegistry;

public class RudderPartTileEntityRenderer extends
    TileEntitySpecialRenderer<TileEntityRudderPart> {

    @Override
    public void render(TileEntityRudderPart tileentity, double x, double y, double z,
        float partialTick,
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
            GibsModelRegistry.renderGibsModel("rudder_axle_geo", brightness);
        } else {
            if (tileentity.isMaster() && tileentity.getRudderAxleAxisDirection().isPresent()) {
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

                Vector3d facingDirection = new Vector3d(axleFacing.getDirectionVec().getX(),
                    axleFacing.getDirectionVec().getY(), axleFacing.getDirectionVec().getZ());

                AxisAngle4d rotation1 = new AxisAngle4d(Math.toRadians(-pitchRot), 1, 0, 0);
                AxisAngle4d rotation2 = new AxisAngle4d(Math.toRadians(-rollRot), 0, 1, 0);

                rotation2.transform(facingDirection);
                rotation1.transform(facingDirection);

                EnumFacing facingDirectionNew = EnumFacing
                    .getFacingFromVector((float) facingDirection.x, (float) facingDirection.y,
                        (float) facingDirection.z);

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

                double rudderAxleLength = tileentity.getRudderAxleLength().get();

                GL11.glPushMatrix();
                GL11.glTranslated(.5, 0, .5);
                GL11.glScaled(rudderAxleLength, rudderAxleLength, rudderAxleLength);
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
                    GibsModelRegistry.renderGibsModel("rudder_axle_geo", brightness);
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
