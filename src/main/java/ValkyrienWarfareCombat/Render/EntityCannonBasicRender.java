package ValkyrienWarfareCombat.Render;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareCombat.ValkyrienWarfareCombatMod;
import ValkyrienWarfareCombat.Entity.EntityCannonBasic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityCannonBasicRender extends Render<EntityCannonBasic>{

	protected EntityCannonBasicRender(RenderManager renderManager) {
		super(renderManager);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void doRender(EntityCannonBasic entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
		double renderYaw = -entity.rotationYaw+90;
		double renderPitch = entity.rotationPitch;
		
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        
        double oldX = vertexbuffer.xOffset;
        double oldY = vertexbuffer.yOffset;
        double oldZ = vertexbuffer.zOffset;
		
		GL11.glPushMatrix();
		
		GlStateManager.disableLighting();
        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        
        
//        BlockPos blockpos = new BlockPos(entity.posX, entity.getEntityBoundingBox().maxY, entity.posZ);
//        GlStateManager.translate((float)(x - (double)blockpos.getX() - 0.5D), (float)(y - (double)blockpos.getY()), (float)(z - (double)blockpos.getZ() - 0.5D));
       
        vertexbuffer.setTranslation((float)(0 - entity.posX), (float)(0 - entity.posY-1D), (float)(0 - entity.posZ));
        
        GL11.glTranslated(x, y, z);
        
        GL11.glPushMatrix();
        GL11.glTranslated(-.7D,0,0);
//        GL11.glScaled(1,.8,.8);
        renderBase(entity, x, y, z, entityYaw, partialTicks);
        GL11.glPopMatrix();
        
        GL11.glTranslated(.15D,.5D,0);
        GL11.glTranslated(-.6D,0,0);
        
        GL11.glRotated(renderYaw, 0, 1D, 0);
        GL11.glRotated(renderPitch, 0, 0, 1D);
        
        
        GL11.glTranslated(-.8D, 0, -0.25);
        
        GL11.glPushMatrix();
        renderHead(entity, x, y, z, entityYaw, partialTicks);
        GL11.glPopMatrix();
        
        if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        vertexbuffer.setTranslation(oldX, oldY, oldZ);
        
        GlStateManager.enableLighting();
		
		GL11.glPopMatrix();
    }
	
	private void renderBase(EntityCannonBasic entity, double x, double y, double z, float entityYaw, float partialTicks){
		Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
        
		IBlockState baseState = ValkyrienWarfareCombatMod.instance.fakeCannonBlock.getStateFromMeta(0);
		
        BlockPos blockpos = new BlockPos(entity.posX, entity.getEntityBoundingBox().maxY, entity.posZ);
//        GlStateManager.translate((float)(x - (double)blockpos.getX() - 0.5D), (float)(y - (double)blockpos.getY()), (float)(z - (double)blockpos.getZ() - 0.5D));
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        blockrendererdispatcher.getBlockModelRenderer().renderModel(entity.worldObj, blockrendererdispatcher.getModelForState(baseState), baseState, blockpos, vertexbuffer, false, MathHelper.getPositionRandom(new BlockPos(entity)));
        
        tessellator.draw();
	}
	
	private void renderHead(EntityCannonBasic entity, double x, double y, double z, float entityYaw, float partialTicks){
		Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
        
		IBlockState baseState = ValkyrienWarfareCombatMod.instance.fakeCannonBlock.getStateFromMeta(1);
		
        BlockPos blockpos = new BlockPos(entity.posX, entity.getEntityBoundingBox().maxY, entity.posZ);
//        GlStateManager.translate((float)(x - (double)blockpos.getX() - 0.5D), (float)(y - (double)blockpos.getY()), (float)(z - (double)blockpos.getZ() - 0.5D));
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        blockrendererdispatcher.getBlockModelRenderer().renderModel(entity.worldObj, blockrendererdispatcher.getModelForState(baseState), baseState, blockpos, vertexbuffer, false, MathHelper.getPositionRandom(new BlockPos(entity)));
        
        tessellator.draw();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCannonBasic entity) {
		return null;
	}

}
