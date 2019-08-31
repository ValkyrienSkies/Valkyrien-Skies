/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.addon.control.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.block.BlockShipTelegraph;
import org.valkyrienskies.addon.control.tileentity.TileEntityShipTelegraph;
import org.valkyrienskies.mod.client.render.GibsModelRegistry;

public class ShipTelegraphTileEntityRenderer extends
    TileEntitySpecialRenderer<TileEntityShipTelegraph> {

    @Override
    public void render(TileEntityShipTelegraph tileentity, double x, double y, double z,
        float partialTick, int destroyStage, float alpha) {
        IBlockState telegraphState = tileentity.getWorld().getBlockState(tileentity.getPos());

        if (telegraphState.getBlock()
            != ValkyrienSkiesControl.INSTANCE.vwControlBlocks.shipTelegraph) {
            return;
        }

        GlStateManager.resetColor();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder tessellatorBuffer = tessellator.getBuffer();

        double oldX = tessellatorBuffer.xOffset;
        double oldY = tessellatorBuffer.yOffset;
        double oldZ = tessellatorBuffer.zOffset;

        tessellatorBuffer.setTranslation(0, 0, 0);
        GL11.glTranslated(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        BlockPos originPos = tileentity.getPos();

        int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

        double multiplier = 1.9D;

        GL11.glTranslated((1D - multiplier) / 2.0D, 0, (1D - multiplier) / 2.0D);
        GL11.glScaled(multiplier, multiplier, multiplier);
        EnumFacing enumfacing = telegraphState.getValue(BlockShipTelegraph.FACING);
        double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

        GL11.glTranslated(0.5D, 0, 0.5D);
        GL11.glRotated(wheelAndCompassStateRotation, 0, 1, 0);
        GL11.glTranslated(-0.5D, 0, -0.5D);

        // FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), helmStateToRender, brightness);

        GibsModelRegistry.renderGibsModel("chadburn_speedtelegraph_simplevoxel_geo", brightness);

        // FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), dialState, brightness);

        GibsModelRegistry.renderGibsModel("chadburn_dial_simplevoxel_geo", brightness);

        GL11.glPushMatrix();

        GL11.glTranslated(0.497D, 0.857D, 0.5D);
        GL11.glRotated(tileentity.getHandleRenderRotation(partialTick), 0D, 0D, 1D);
        GL11.glTranslated(-0.497D, -0.857D, -0.5D);

        // FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), rightHandleState, brightness);

        GibsModelRegistry.renderGibsModel("chadburn_handles_simplevoxel_geo", brightness);

        // FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), leftHandleState, brightness);

        GL11.glPopMatrix();

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // FastBlockModelRenderer.renderBlockModel(tessellator, tileentity.getWorld(), glassState, brightness);
        GibsModelRegistry.renderGibsModel("chadburn_glass_simplevoxel_geo", brightness);

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        GL11.glPopMatrix();
        tessellatorBuffer.setTranslation(oldX, oldY, oldZ);

        GlStateManager.enableLighting();
        GlStateManager.resetColor();
    }

}