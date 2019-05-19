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

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import valkyrienwarfare.addon.control.nodenetwork.BasicNodeTileEntity;
import valkyrienwarfare.addon.control.nodenetwork.VWNode_TileEntity;

public class BasicNodeTileEntityRenderer extends TileEntitySpecialRenderer<BasicNodeTileEntity> {

    @Override
    public void render(BasicNodeTileEntity te, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        GlStateManager.disableBlend();
        VWNode_TileEntity tileNode = te.getNode();
        if (tileNode != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(.5D, -1D, .5D);
            // GL11.glTranslated(0, y, 0);

            for (BlockPos otherPos : tileNode.getLinkedNodesPos()) {
                TileEntity otherTile = getWorld().getTileEntity(otherPos);
                if (otherTile instanceof BasicNodeTileEntity) {
                    // Don't render the same connection twice.
                    if (otherTile.getPos()
                            .compareTo(te.getPos()) < 0) {
                        continue;
                    }
                }
                // render wire between these two blockPos
                GL11.glPushMatrix();
                // GlStateManager.resetColor();

                double startX = te.getPos()
                        .getX();
                double startY = te.getPos()
                        .getY();
                double startZ = te.getPos()
                        .getZ();

                double endX = (startX * 2) - otherPos.getX();
                double endY = (startY * 2) - otherPos.getY() - 1.5;
                double endZ = (startZ * 2) - otherPos.getZ();

                renderWire(x, y, z, startX, startY, startZ, endX, endY, endZ);

                // GL11.glEnd();
                GL11.glPopMatrix();
            }

            GlStateManager.resetColor();
            // bindTexture(new ResourceLocation("textures/entity/lead_knot.png"));
            // GlStateManager.scale(-1.0F, -1.0F, 1.0F);

            // ModelLeashKnot knotRenderer = new ModelLeashKnot();
            // knotRenderer.knotRenderer.render(0.0625F);
            // BlockPos originPos = te.getPos();

            GL11.glPopMatrix();
        }
    }

    protected void renderWire(double x, double y, double z, double entity1x, double entity1y, double entity1z,
                              double entity2x, double entity2y, double entity2z) {
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        float wireR = .6f;
        float wireG = .25f;
        float wireB = .02f;
        float wireAlpha = 1.0f;
        // Vec3d vec = new Vec3d(x,y,z);
        // if (vec.lengthSquared() < .01D) {
        // System.out.println("REE");
        // }
        // y = .5D;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        double d0 = 0;// this.interpolateValue(entity.prevRotationYaw, entity.rotationYaw,
        // partialTicks * 0.5F) * 0.01745329238474369D;
        double d1 = 0;// this.interpolateValue(entity.prevRotationPitch, entity.rotationPitch,
        // partialTicks * 0.5F) * 0.01745329238474369D;
        double d2 = Math.cos(d0);
        double d3 = Math.sin(d0);
        double d4 = Math.sin(d1);

        // if (entity instanceof EntityHanging)
        // {
        d4 = -1.0D;
        // }

        double fakeYaw = 0;// Math.PI / 6D;

        double d6 = entity1x;
        double d7 = entity1y;
        double d8 = entity1z;
        double d9 = fakeYaw;

        d2 = 0;// fakeWidth;;
        d3 = 0;// fakeWidth;
        double d10 = entity2x + d2;
        double d11 = entity2y;
        double d12 = entity2z + d3;
        x = x + d2;
        z = z + d3;
        double d13 = ((float) (d6 - d10));
        double d14 = ((float) (d7 - d11));
        double d15 = ((float) (d8 - d12));
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        int i = 24;
        double d16 = 0.025D;
        bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

        for (int j = 0; j <= 24; ++j) {
            float f3 = j / 24.0F;
            bufferbuilder.pos(x + d13 * f3 + 0.0D, y + d14 * (f3 * f3 + f3) * 0.5D + ((24.0F - j) / 18.0F + 0.125F),
                    z + d15 * f3).color(wireR, wireG, wireB, wireAlpha).endVertex();
            bufferbuilder
                    .pos(x + d13 * f3 + 0.025D,
                            y + d14 * (f3 * f3 + f3) * 0.5D + ((24.0F - j) / 18.0F + 0.125F) + 0.025D, z + d15 * f3)
                    .color(wireR, wireG, wireB, wireAlpha).endVertex();
        }

        tessellator.draw();
        bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

        for (int k = 0; k <= 24; ++k) {
            float f4 = 204F / 255F;// .282F;//0.5F;
            float f5 = 122F / 255F;// .176F;//0.4F;
            float f6 = 0;// 0.078F;//0.3F;

            // f4 *= 2.2F;
            // f5 *= 2.2F;
            // f6 *= 2.2F;

            if (k % 2 == 0) {
                f4 *= 0.7F;
                f5 *= 0.7F;
                f6 *= 0.7F;
            }

            float f7 = k / 24.0F;
            bufferbuilder.pos(x + d13 * f7 + 0.0D,
                    y + d14 * (f7 * f7 + f7) * 0.5D + ((24.0F - k) / 18.0F + 0.125F) + 0.025D, z + d15 * f7)
                    .color(wireR, wireG, wireB, wireAlpha).endVertex();
            bufferbuilder.pos(x + d13 * f7 + 0.025D, y + d14 * (f7 * f7 + f7) * 0.5D + ((24.0F - k) / 18.0F + 0.125F),
                    z + d15 * f7 + 0.025D).color(wireR, wireG, wireB, wireAlpha).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    private double interpolateValue(double start, double end, double pct) {
        return start + (end - start) * pct;
    }

}
