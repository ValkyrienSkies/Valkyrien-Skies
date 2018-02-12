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

import net.minecraft.client.model.ModelLeashKnot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.addon.control.nodenetwork.BasicNodeTileEntity;
import valkyrienwarfare.addon.control.nodenetwork.Node;

public class BasicNodeTileEntityRenderer extends TileEntitySpecialRenderer {

    private final Class renderedTileEntityClass;

    public BasicNodeTileEntityRenderer(Class toRender) {
        renderedTileEntityClass = toRender;
    }

    @Override
    public void render(TileEntity te, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (te instanceof BasicNodeTileEntity) {
            Node tileNode = ((BasicNodeTileEntity) (te)).getNode();
            if (tileNode != null) {
                GL11.glPushMatrix();
                GlStateManager.resetColor();
                GlStateManager.enableAlpha();
                bindTexture(new ResourceLocation("textures/entity/lead_knot.png"));
                GL11.glTranslated(x + .5D, y + .5D, z + .5D);
                // GlStateManager.scale(-1.0F, -1.0F, 1.0F);

                ModelLeashKnot knotRenderer = new ModelLeashKnot();
                knotRenderer.knotRenderer.render(0.0625F);
                BlockPos originPos = te.getPos();

                // double x = originPos.getX() + .5D;

                for (BlockPos otherPos : tileNode.getConnectedNodesBlockPos()) {
                    // render wire between these two blockPos
                    GL11.glPushMatrix();

                    double otherX = otherPos.getX() - TileEntityRendererDispatcher.staticPlayerX - x;
                    double otherY = otherPos.getY() - TileEntityRendererDispatcher.staticPlayerY - y;
                    double otherZ = otherPos.getZ() - TileEntityRendererDispatcher.staticPlayerZ - z;

                    // System.out.println(otherZ);
                    // GL11.glTranslated(-te.getPos().getX(), -te.getPos().getY(),
                    // -te.getPos().getZ());

                    GL11.glLineWidth(2.5f);
                    GL11.glColor3d(1.0, 0.0, 0.0);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3d(0, 0, 0D);
                    GL11.glVertex3d(otherX, otherY, otherZ);
                    GL11.glEnd();
                    GL11.glPopMatrix();
                }
                GL11.glPopMatrix();
            }
        }
    }

}
