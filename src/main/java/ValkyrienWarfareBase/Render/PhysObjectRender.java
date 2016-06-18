package ValkyrienWarfareBase.Render;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
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
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PhysObjectRender extends Render<PhysicsWrapperEntity>{

	public PhysObjectRender(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(PhysicsWrapperEntity entity, double x, double y, double z, float entityYaw, float partialTicks){
		GL11.glPushMatrix();
		setupTransform(entity,x,y,z,entityYaw,partialTicks);
		renderBlocks(entity,x,y,z,entityYaw,partialTicks);
		if (entity.wrapping.claimedChunks!=null&&false)
        {
			BlockPos centerDifference = entity.wrapping.getRegionCenter();
            IBlockState iblockstate = entity.worldObj.getBlockState(centerDifference);
            bindTexture(TextureMap.locationBlocksTexture);
            //System.out.println(entity.worldObj.getBlockState(centerDifference).getBlock());
            if (iblockstate.getRenderType() == EnumBlockRenderType.MODEL)
            {
                World world = entity.worldObj;

                if (iblockstate != world.getBlockState(new BlockPos(entity)) && iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE)
                {
                    GlStateManager.pushMatrix();
                    GlStateManager.disableLighting();
                    Tessellator tessellator = Tessellator.getInstance();
                    VertexBuffer vertexbuffer = tessellator.getBuffer();

                    if (this.renderOutlines)
                    {
                        GlStateManager.enableColorMaterial();
                        GlStateManager.enableOutlineMode(this.getTeamColor(entity));
                    }
                    
                    vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
                    BlockPos blockpos = new BlockPos(entity.posX, entity.getEntityBoundingBox().maxY, entity.posZ);
                    GlStateManager.translate((float)(x - (double)blockpos.getX() - 0.5D), (float)(y - (double)blockpos.getY()), (float)(z - (double)blockpos.getZ() - 0.5D));
                    BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                    blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(iblockstate), iblockstate, blockpos, vertexbuffer, false, MathHelper.getPositionRandom(new BlockPos(0,0,0)));
                    tessellator.draw();

                    if (this.renderOutlines)
                    {
                        GlStateManager.disableOutlineMode();
                        GlStateManager.disableColorMaterial();
                    }

                    GlStateManager.enableLighting();
                    GlStateManager.popMatrix();
                    super.doRender(entity, x, y, z, entityYaw, partialTicks);
                }
            }
        }
		GL11.glPopMatrix();
    }
	
	public void setupTransform(PhysicsWrapperEntity entity, double x, double y, double z, float entityYaw, float partialTicks){
		BlockPos center = entity.wrapping.centerBlockPos;
		
		
		double moddedX = entity.lastTickPosX+(entity.posX-entity.lastTickPosX)*partialTicks;
		double moddedY = entity.lastTickPosY+(entity.posY-entity.lastTickPosY)*partialTicks;
		double moddedZ = entity.lastTickPosZ+(entity.posZ-entity.lastTickPosZ)*partialTicks;
		double p0 = Minecraft.getMinecraft().thePlayer.lastTickPosX + (Minecraft.getMinecraft().thePlayer.posX - Minecraft.getMinecraft().thePlayer.lastTickPosX) * (double)partialTicks;
		double p1 = Minecraft.getMinecraft().thePlayer.lastTickPosY + (Minecraft.getMinecraft().thePlayer.posY - Minecraft.getMinecraft().thePlayer.lastTickPosY) * (double)partialTicks;
		double p2 = Minecraft.getMinecraft().thePlayer.lastTickPosZ + (Minecraft.getMinecraft().thePlayer.posZ - Minecraft.getMinecraft().thePlayer.lastTickPosZ) * (double)partialTicks;
		
//		System.out.println(moddedX);
		
		GlStateManager.translate(-p0+moddedX, -p1+moddedY, -p2+moddedZ);
		GL11.glTranslated(-center.getX(), -center.getY(), -center.getZ());
		
	}
	
	public void renderBlocks(PhysicsWrapperEntity entity, double x, double y, double z, float entityYaw, float partialTicks){
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();

		
		
		
		GL11.glCallList(entity.wrapping.renderer.glCallListSolid);
		if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(PhysicsWrapperEntity entity) {
		return TextureMap.locationBlocksTexture;
	}

}
