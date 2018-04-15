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

package valkyrienwarfare.addon.control.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.block.BlockShipHelm;
import valkyrienwarfare.addon.control.tileentity.TileEntityShipHelm;
import valkyrienwarfare.mod.client.render.FastBlockModelRenderer;

public class ShipHelmTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityShipHelm> {

    private final Class renderedTileEntityClass;

    public ShipHelmTileEntityRenderer(Class toRender) {
        renderedTileEntityClass = toRender;
    }

    @Override
    public void render(TileEntityShipHelm tileentity, double x, double y, double z, float partialTick, int destroyStage,
            float alpha) {
        if (tileentity instanceof TileEntityShipHelm) {
            IBlockState helmState = tileentity.getWorld().getBlockState(tileentity.getPos());

            if (helmState.getBlock() != ValkyrienWarfareControl.INSTANCE.blocks.shipHelm) {
                return;
            }

            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder BufferBuilder = tessellator.getBuffer();

            double oldX = BufferBuilder.xOffset;
            double oldY = BufferBuilder.yOffset;
            double oldZ = BufferBuilder.zOffset;

            BufferBuilder.setTranslation(0, 0, 0);
            GL11.glTranslated(x, y, z);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();

            double smoothCompassDif = (tileentity.compassAngle - tileentity.lastCompassAngle);
            if (smoothCompassDif < -180) {
                smoothCompassDif += 360;
            }
            if (smoothCompassDif > 180) {
                smoothCompassDif -= 360;
            }

            double smoothWheelDif = (tileentity.wheelRotation - tileentity.lastWheelRotation);
            if (smoothWheelDif < -180) {
                smoothWheelDif += 360;
            }
            if (smoothWheelDif > 180) {
                smoothWheelDif -= 360;
            }

            double smoothCompass = tileentity.lastCompassAngle + (smoothCompassDif) * partialTick + 180D;
            double smoothWheel = tileentity.lastWheelRotation + (smoothWheelDif) * partialTick;
            BlockPos originPos = tileentity.getPos();

            IBlockState wheelState = ValkyrienWarfareControl.INSTANCE.blocks.shipWheel.getStateFromMeta(0);
            IBlockState compassState = ValkyrienWarfareControl.INSTANCE.blocks.shipWheel.getStateFromMeta(1);
            IBlockState glassState = ValkyrienWarfareControl.INSTANCE.blocks.shipWheel.getStateFromMeta(2);
            IBlockState helmStateToRender = ValkyrienWarfareControl.INSTANCE.blocks.shipWheel.getStateFromMeta(3);
            // TODO: Better rendering cache
            int brightness = tileentity.getWorld().getCombinedLight(tileentity.getPos(), 0);

            double multiplier = 2.0D;
            GL11.glTranslated((1D - multiplier) / 2.0D, 0, (1D - multiplier) / 2.0D);
            GL11.glScaled(multiplier, multiplier, multiplier);
            EnumFacing enumfacing = helmState.getValue(BlockShipHelm.FACING);
            double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

            GL11.glTranslated(0.5D, 0, 0.5D);
            GL11.glRotated(wheelAndCompassStateRotation, 0, 1, 0);
            GL11.glTranslated(-0.5D, 0, -0.5D);
            FastBlockModelRenderer.renderBlockModel(BufferBuilder, tessellator, tileentity.getWorld(),
                    helmStateToRender, brightness);

            GL11.glPushMatrix();
            GL11.glTranslated(.5, .522, 0);
            GL11.glRotated(smoothWheel, 0, 0, 1);
            GL11.glTranslated(-.5, -.522, 0);
            FastBlockModelRenderer.renderBlockModel(BufferBuilder, tessellator, tileentity.getWorld(), wheelState,
                    brightness);
            GL11.glPopMatrix();

            GL11.glPushMatrix();
            GL11.glTranslated(0.5D, 0, 0.5D);
            GL11.glRotated(smoothCompass, 0, 1, 0);
            GL11.glTranslated(-0.5D, 0, -0.5D);
            FastBlockModelRenderer.renderBlockModel(BufferBuilder, tessellator, tileentity.getWorld(), compassState,
                    brightness);
            GL11.glPopMatrix();

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            FastBlockModelRenderer.renderBlockModel(BufferBuilder, tessellator, tileentity.getWorld(), glassState,
                    brightness);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();

            GL11.glPopMatrix();

            BufferBuilder.setTranslation(oldX, oldY, oldZ);
            GlStateManager.enableLighting();
            GlStateManager.resetColor();
        }
    }

}
