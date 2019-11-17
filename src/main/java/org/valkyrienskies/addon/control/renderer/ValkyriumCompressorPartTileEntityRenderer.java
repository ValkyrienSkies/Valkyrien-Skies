package org.valkyrienskies.addon.control.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityValkyriumCompressorPart;
import org.valkyrienskies.mod.client.render.FastBlockModelRenderer;
import org.valkyrienskies.mod.client.render.GibsAnimationRegistry;
import org.valkyrienskies.mod.common.math.RotationMatrices;
import org.valkyrienskies.mod.common.math.Vector;

public class ValkyriumCompressorPartTileEntityRenderer extends
    TileEntitySpecialRenderer<TileEntityValkyriumCompressorPart> {

    @Override
    public void render(TileEntityValkyriumCompressorPart tileentity, double x, double y, double z,
        float partialTick,
        int destroyStage, float alpha) {

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

        GlStateManager.pushMatrix();
        if (!tileentity.isPartOfAssembledMultiblock()) {
            IBlockState state = Blocks.GOLD_BLOCK.getDefaultState();
            Tessellator tessellator = Tessellator.getInstance();
            FastBlockModelRenderer
                .renderBlockModel(tessellator, tileentity.getWorld(), state, brightness);
        } else {
            if (tileentity.isMaster()) {
                double keyframe = tileentity.getCurrentKeyframe(partialTick);

                GlStateManager.pushMatrix();

                float rotationYaw = tileentity.getMultiBlockSchematic().getMultiblockRotation()
                    .getYaw();

                Vector centerOffset = new Vector(.5, 0, .5);
                RotationMatrices
                    .applyTransform(RotationMatrices.getRotationMatrix(0, -rotationYaw, 0),
                        centerOffset);
                GlStateManager.translate(centerOffset.X, centerOffset.Y, centerOffset.Z);

                GlStateManager.translate(.5, 0, .5);
                GlStateManager.scale(2, 2, 2);
                GlStateManager.rotate(-rotationYaw, 0, 1, 0);
                GlStateManager.translate(-.5, 0, -.5);

                GibsAnimationRegistry.getAnimation("valkyrium_compressor")
                    .renderAnimation(keyframe, brightness);
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
    }
}
