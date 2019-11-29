package org.valkyrienskies.mod.common.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import java.util.Collection;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.mod.common.physics.bullet.MeshCreator.Triangle;
import org.valkyrienskies.mod.common.util.JOML;

public class MeshDebugOverlayRenderer {

    public static void renderTriangles(Collection<Triangle> triangles, Vector3dc offsetJOML) {
        GlStateManager.pushMatrix();

        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();

        Vector3 offset = JOML.toGDX(offsetJOML);

        for (Triangle tri : triangles) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBuffer();
            builder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

            // The points of the triangle
            Vector3 a = tri.getA().add(offset);
            Vector3 b = tri.getB().add(offset);
            Vector3 c = tri.getC().add(offset);
            float red = 0f, green = 1f, blue = 0f, alpha = 1f;

            builder.pos(a.x, a.y, a.z).color(red, green, blue, alpha).endVertex();
            builder.pos(b.x, b.y, b.z).color(red, green, blue, alpha).endVertex();
            builder.pos(c.x, c.y, c.z).color(red, green, blue, alpha).endVertex();
            builder.pos(a.x, a.y, a.z).color(red, green, blue, alpha).endVertex();

            tessellator.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
    }

}
