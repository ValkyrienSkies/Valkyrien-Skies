package org.valkyrienskies.addon.control.renderer;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import org.valkyrienskies.addon.control.block.BlockGearbox;
import org.valkyrienskies.addon.control.tileentity.TileEntityGearbox;
import org.valkyrienskies.mod.client.render.GibsModelRegistry;

public class GearboxTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityGearbox> {

    private static final ImmutableMap<EnumFacing, String> FACING_TO_AXLE_NAME;

    static {
        FACING_TO_AXLE_NAME = ImmutableMap.<EnumFacing, String>builder()
            .put(EnumFacing.DOWN, "gearbox_top_geo")
            .put(EnumFacing.UP, "gearbox_bottom_geo")
            .put(EnumFacing.SOUTH, "gearbox_front_geo")
            .put(EnumFacing.NORTH, "gearbox_back_geo")
            .put(EnumFacing.WEST, "gearbox_right_geo")
            .put(EnumFacing.EAST, "gearbox_left_geo")
            .build();
    }

    @Override
    public void render(TileEntityGearbox tileentity, double x, double y, double z,
        float partialTick,
        int destroyStage, float alpha) {

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

        // Render the side axles
        float renderRotation = (float) Math
            .toDegrees(tileentity.getRenderRotationRadians(partialTick));
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
                GlStateManager
                    .rotate((float) effectiveRenderRotation, facingVec.getX(), facingVec.getY(),
                        facingVec.getZ());
                GlStateManager.translate(-.5, -.5, -.5);
                GibsModelRegistry.renderGibsModel(FACING_TO_AXLE_NAME.get(facing), brightness);
                GlStateManager.popMatrix();
            }
        }

        IBlockState gearState = Minecraft.getMinecraft().world.getBlockState(tileentity.getPos());
        if (gearState.getBlock() instanceof BlockGearbox) {
            EnumFacing facing = tileentity.getRenderFacing();
            GlStateManager.pushMatrix();

            GlStateManager.translate(0.5, 0.5, 0.5);
            switch (facing) {
                case UP:
                    GlStateManager.rotate(-90, 1, 0, 0);
                    break;
                case DOWN:
                    GlStateManager.rotate(90, 1, 0, 0);
                    break;
                case NORTH:
                    GlStateManager.rotate(180, 0, 1, 0);
                    break;
                case EAST:
                    GlStateManager.rotate(90, 0, 1, 0);
                    break;
                case SOUTH:
                    GlStateManager.rotate(0, 0, 1, 0);
                    break;
                case WEST:
                    GlStateManager.rotate(270, 0, 1, 0);
                    break;
            }
            GlStateManager.translate(-0.5, -0.5, -0.5);
        }

        // The rotation clamped from 0 to 360
        double rotationFrom0To360 = renderRotation % 360;
        if (rotationFrom0To360 < 0) {
            rotationFrom0To360 += 360;
        }
        double keyframe = (rotationFrom0To360 * 99D / 360D) + 1;
        GibsAtomAnimationRegistry.getAnimation("gearbox").renderAnimation(keyframe, brightness);

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
    }
}
