package ValkyrienWarfareCombat.Render;

import ValkyrienWarfareBase.Render.FastBlockModelRenderer;
import ValkyrienWarfareCombat.Entity.EntityCannonBasic;
import ValkyrienWarfareCombat.ValkyrienWarfareCombatMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class EntityCannonBasicRender extends Render<EntityCannonBasic> {

    private final IBlockState baseState, headState;

    protected EntityCannonBasicRender(RenderManager renderManager) {
        super(renderManager);
        baseState = ValkyrienWarfareCombatMod.instance.fakecannonblock.getStateFromMeta(0);
        headState = ValkyrienWarfareCombatMod.instance.fakecannonblock.getStateFromMeta(1);
    }

    @Override
    public void doRender(EntityCannonBasic entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float paritalTickYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        float paritalTickPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

        double renderYaw = -paritalTickYaw + 90f;
        double renderPitch = paritalTickPitch;

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

//		entity.posX += 15;

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();

        double oldX = vertexbuffer.xOffset;
        double oldY = vertexbuffer.yOffset;
        double oldZ = vertexbuffer.zOffset;

        GL11.glPushMatrix();

        GlStateManager.disableLighting();

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        vertexbuffer.setTranslation(0, 0, 0);

        GL11.glTranslated(x, y, z);

        double offsetAngle = entity.getBaseAngleOffset();
        GL11.glRotated(offsetAngle, 0, 1D, 0);

        GL11.glPushMatrix();

        GL11.glTranslated(-.1D, 0, 0);
        renderBase(entity, x, y, z, entityYaw, partialTicks);

        GL11.glPopMatrix();

        GL11.glTranslated(.15D, .5D, 0);

        GL11.glRotated(renderYaw - offsetAngle, 0, 1D, 0);
        GL11.glRotated(renderPitch, 0, 0, 1D);

        GL11.glTranslated(-.8D, 0, -0.25);

        GL11.glPushMatrix();
        renderHead(entity, x, y, z, entityYaw, partialTicks);
        GL11.glPopMatrix();

        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        vertexbuffer.setTranslation(oldX, oldY, oldZ);
        GlStateManager.disableLighting();
        GlStateManager.resetColor();
        GlStateManager.enableLighting();

        GL11.glPopMatrix();


    }

    private void renderBase(EntityCannonBasic entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        GL11.glPushMatrix();
        GL11.glTranslated(-0.5D, 0, -0.5D);
        FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, entity.world, baseState, entity.getBrightnessForRender(partialTicks));
        GL11.glPopMatrix();
    }

    private void renderHead(EntityCannonBasic entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        GL11.glPushMatrix();
        GL11.glTranslated(-0.5D, 0, -0.5D);
        FastBlockModelRenderer.renderBlockModel(vertexbuffer, tessellator, entity.world, headState, entity.getBrightnessForRender(partialTicks));
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCannonBasic entity) {
        return null;
    }

}
