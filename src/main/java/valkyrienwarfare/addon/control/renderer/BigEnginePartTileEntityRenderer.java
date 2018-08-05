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
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.block.BlockShipHelm;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityBigEnginePart;
import valkyrienwarfare.mod.client.render.FastBlockModelRenderer;
import valkyrienwarfare.mod.client.render.GibsAnimationRegistry;

public class BigEnginePartTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityBigEnginePart> {

	@Override
	public void render(TileEntityBigEnginePart tileentity, double x, double y, double z, float partialTick,
			int destroyStage, float alpha) {

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GL11.glTranslated(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        // TODO: Better rendering cache
        int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

        GL11.glPushMatrix();

        if (!tileentity.isPartOfAssembledMultiblock()) {
			IBlockState state = Blocks.IRON_BLOCK.getDefaultState();
			Tessellator tessellator = Tessellator.getInstance();
			FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), state, brightness);

		} else if (tileentity.isMaster()) {
			
			
			GlStateManager.rotate(tileentity.getMultiblockRotation().getYaw(), 0, 1, 0);
			GlStateManager.scale(3, 3, 3);
			GlStateManager.translate(-1D/3D, 0, -2D/3D);
			
			double keyframe = ((Minecraft.getMinecraft().world.getTotalWorldTime() + partialTick) % 99) + 1;
			
			GibsAnimationRegistry.getAnimation("bigengine").renderAnimation(keyframe, brightness);
		}
        
        GL11.glPopMatrix();
        GL11.glPopMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
	}
}
