package org.valkyrienskies.addon.control.renderer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityGiantPropellerPart;

public class GiantPropellerPartTileEntityRenderer extends
    TileEntitySpecialRenderer<TileEntityGiantPropellerPart> {

    @Override
    public void render(TileEntityGiantPropellerPart tileentity, double x, double y, double z,
        float partialTick,
        int destroyStage, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

        if (!tileentity.isPartOfAssembledMultiblock()) {
            GlStateManager.pushMatrix();
            // GibsModelRegistry.renderGibsModel("aipropeller_geo", brightness);
            GibsAtomAnimationRegistry.getAnimation("giant_propeller").renderAnimation(1, brightness);
            GlStateManager.popMatrix();
        } else if (tileentity.isMaster()) {
            GlStateManager.pushMatrix();
            EnumFacing propellerFacing = tileentity.getPropellerFacing();
            int propellerRadius = (tileentity.getPropellerRadius() * 2) + 1;

            GlStateManager.translate(.5, .5, .5);
            GlStateManager.scale(propellerRadius, propellerRadius, propellerRadius);

            switch (propellerFacing) {
                case NORTH:
                    GlStateManager.rotate(180, 0, 1, 0);
                    break;
                case EAST:
                    GlStateManager.rotate(90, 0, 1, 0);
                    break;
                case SOUTH:
                    // GlStateManager.rotate(0,0,1,0);
                    break;
                case WEST:
                    GlStateManager.rotate(270, 0, 1, 0);
                    break;
                case UP:
                    GlStateManager.rotate(-90, 1, 0, 0);
                    break;
                case DOWN:
                    GlStateManager.rotate(90, 1, 0, 0);
                    break;
            }

            GlStateManager.rotate(tileentity.getPropellerAngle(partialTick), 0, 0, 1);
            GlStateManager.translate(-.5, -.5, -.5);

            // GibsModelRegistry.renderGibsModel("propeller_geo", brightness);
            GibsAtomAnimationRegistry.getAnimation("giant_propeller").renderAnimation(1, brightness);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }
}