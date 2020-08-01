package org.valkyrienskies.addon.control.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityValkyriumEnginePart;
import org.valkyrienskies.mod.client.render.FastBlockModelRenderer;

public class ValkyriumEnginePartTileEntityRenderer extends
    TileEntitySpecialRenderer<TileEntityValkyriumEnginePart> {

    @Override
    public void render(TileEntityValkyriumEnginePart tileentity, double x, double y, double z,
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
            IBlockState state = Blocks.IRON_BLOCK.getDefaultState();
            Tessellator tessellator = Tessellator.getInstance();
            FastBlockModelRenderer
                .renderBlockModel(tessellator, tileentity.getWorld(), state, brightness);
        } else if (tileentity.isMaster()) {
            // Im not sure why this has to be done, something is wrong with my rotation
            // intuition.
            float tileYaw =
                -tileentity.getMultiBlockSchematic().getMultiblockRotation().getYaw() + 180;

            GlStateManager.translate(.5, 0, .5);
            GlStateManager.scale(3, 3, 3);
            GlStateManager.rotate(tileYaw, 0, 1, 0);
            GlStateManager.translate(-.5, 0, -.5);

            double keyframe = tileentity.getCurrentKeyframe(partialTick);

            GibsAtomAnimationRegistry.getAnimation("valkyrium_engine")
                .renderAnimation(keyframe, brightness);
        }

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
    }
}
