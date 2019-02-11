package valkyrienwarfare.addon.control.renderer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityGiantPropellerPart;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityRudderAxlePart;
import valkyrienwarfare.mod.client.render.GibsModelRegistry;

public class GiantPropellerPartTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityGiantPropellerPart> {

    @Override
    public void render(TileEntityGiantPropellerPart tileentity, double x, double y, double z, float partialTick,
                       int destroyStage, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

        GlStateManager.pushMatrix();
        GibsModelRegistry.renderGibsModel("propeller_geo", brightness);
        GL11.glPopMatrix();
        GlStateManager.popMatrix();
    }
}