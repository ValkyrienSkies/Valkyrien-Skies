package ValkyrienWarfareBase.CoreMod;

import java.util.Iterator;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.Proxy.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class CallRunnerClient extends CallRunner{

	public static void onAddEffect(ParticleManager manager,Particle effect)
    {
		BlockPos pos = new BlockPos(effect.posX,effect.posY,effect.posZ);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(effect.worldObj, pos);
		if(wrapper!=null){
			Vector posVec = new Vector(effect.posX,effect.posY,effect.posZ);
			wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
			effect.setPosition(posVec.X, posVec.Y, posVec.Z);
		}
        manager.addEffect(effect);
    }
	
	public static void onDrawBlockDamageTexture(RenderGlobal renderGlobal,Tessellator tessellatorIn, VertexBuffer worldRendererIn, Entity entityIn, float partialTicks)
    {
		double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;

        if (!renderGlobal.damagedBlocks.isEmpty())
        {
            renderGlobal.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            renderGlobal.preRenderDamagedBlocks();
            worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
            worldRendererIn.setTranslation(-d0, -d1, -d2);
            worldRendererIn.noColor();
            Iterator<DestroyBlockProgress> iterator = renderGlobal.damagedBlocks.values().iterator();

            while (iterator.hasNext())
            {
                DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress)iterator.next();
                BlockPos blockpos = destroyblockprogress.getPosition();
                double d3 = (double)blockpos.getX() - d0;
                double d4 = (double)blockpos.getY() - d1;
                double d5 = (double)blockpos.getZ() - d2;
                Block block = renderGlobal.theWorld.getBlockState(blockpos).getBlock();
                TileEntity te = renderGlobal.theWorld.getTileEntity(blockpos);
                boolean hasBreak = block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull;
                if (!hasBreak) hasBreak = te != null && te.canRenderBreaking();

                if (!hasBreak)
                {
                	PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(renderGlobal.theWorld, blockpos);
                    if (wrapper==null&&(d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D))
                    {
                        iterator.remove();
                    }
                    else
                    {
                        IBlockState iblockstate = renderGlobal.theWorld.getBlockState(blockpos);
                        if(wrapper!=null){
                        	wrapper.wrapping.renderer.setupTranslation(partialTicks);
                        	worldRendererIn.setTranslation(
                        			-wrapper.wrapping.renderer.offsetPos.getX(), 
                        			-wrapper.wrapping.renderer.offsetPos.getY(), 
                        			-wrapper.wrapping.renderer.offsetPos.getZ());
                        }
                        if (iblockstate.getMaterial() != Material.AIR)
                        {
                            int i = destroyblockprogress.getPartialBlockDamage();
                            TextureAtlasSprite textureatlassprite = renderGlobal.destroyBlockIcons[i];
                            BlockRendererDispatcher blockrendererdispatcher = renderGlobal.mc.getBlockRendererDispatcher();
                            blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, renderGlobal.theWorld);
                        }
                        worldRendererIn.setTranslation(-d0, -d1, -d2);
                        //TODO: Reverse the Matrix Transforms here
                        if(wrapper!=null){
                        	tessellatorIn.draw();
                        	worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
                        	wrapper.wrapping.renderer.inverseTransform(partialTicks);
                        }
                    }
                }
            }

            tessellatorIn.draw();
            worldRendererIn.setTranslation(0.0D, 0.0D, 0.0D);
            renderGlobal.postRenderDamagedBlocks();
        }

    }
	
	public static void onDrawSelectionBox(RenderGlobal renderGlobal,EntityPlayer player,RayTraceResult movingObjectPositionIn,int execute,float partialTicks){
		if(movingObjectPositionIn.typeOfHit != RayTraceResult.Type.BLOCK){
			return;
		}
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.worldObj, movingObjectPositionIn.getBlockPos());
		if(wrapper!=null){
			GL11.glPushMatrix();
			wrapper.wrapping.renderer.setupTranslation(partialTicks);
			if (execute == 0 && movingObjectPositionIn.typeOfHit == RayTraceResult.Type.BLOCK)
	        {
	            GlStateManager.enableBlend();
	            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	            GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
	            GlStateManager.glLineWidth(2.0F);
	            GlStateManager.disableTexture2D();
	            GlStateManager.depthMask(false);
	            BlockPos blockpos = movingObjectPositionIn.getBlockPos();
	            IBlockState iblockstate = renderGlobal.theWorld.getBlockState(blockpos);
	            if (iblockstate.getMaterial() != Material.AIR && renderGlobal.theWorld.getWorldBorder().contains(blockpos))
	            {
	                double d0 = wrapper.wrapping.renderer.offsetPos.getX();
	                double d1 = wrapper.wrapping.renderer.offsetPos.getY();
	                double d2 = wrapper.wrapping.renderer.offsetPos.getZ();
	            	renderGlobal.drawSelectionBoundingBox(iblockstate.getSelectedBoundingBox(renderGlobal.theWorld, blockpos).expandXyz(0.0020000000949949026D).offset(-d0, -d1, -d2));
	            }
	            GlStateManager.depthMask(true);
	            GlStateManager.enableTexture2D();
	            GlStateManager.disableBlend();
	        }
			GL11.glPopMatrix();
		}else{
			renderGlobal.drawSelectionBox(player, movingObjectPositionIn, execute, partialTicks);
		}
	}

	public static void onRenderEntities(RenderGlobal renderGlobal,Entity renderViewEntity, ICamera camera, float partialTicks)
    {
		((ClientProxy)ValkyrienWarfareMod.proxy).lastCamera = camera;
		
		renderGlobal.renderEntities(renderViewEntity, camera, partialTicks);
		
		GlStateManager.disableAlpha();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		renderGlobal.mc.entityRenderer.enableLightmap();
		
		double playerX = TileEntityRendererDispatcher.instance.staticPlayerX;
		double playerY = TileEntityRendererDispatcher.instance.staticPlayerY;
		double playerZ = TileEntityRendererDispatcher.instance.staticPlayerZ;
		GL11.glPushMatrix();
		for(PhysicsWrapperEntity wrapper:ValkyrienWarfareMod.physicsManager.getManagerForWorld(renderGlobal.theWorld).physicsEntities){
//			Vector centerOfRotation = wrapper.wrapping.centerCoord;
			TileEntityRendererDispatcher.instance.staticPlayerX = wrapper.wrapping.renderer.offsetPos.getX();
			TileEntityRendererDispatcher.instance.staticPlayerY = wrapper.wrapping.renderer.offsetPos.getY();
			TileEntityRendererDispatcher.instance.staticPlayerZ = wrapper.wrapping.renderer.offsetPos.getZ();
			GL11.glPushMatrix();
			wrapper.wrapping.renderer.setupTranslation(partialTicks);
			wrapper.wrapping.renderer.renderTileEntities(partialTicks);
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();
		TileEntityRendererDispatcher.instance.staticPlayerX = playerX;
		TileEntityRendererDispatcher.instance.staticPlayerY = playerY;
		TileEntityRendererDispatcher.instance.staticPlayerZ = playerZ;
    }

	public static boolean onInvalidateRegionAndSetBlock(WorldClient client,BlockPos pos, IBlockState state)
    {
		int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        client.invalidateBlockReceiveRegion(i, j, k, i, j, k);
        return CallRunner.onSetBlockState(client,pos, state, 3);
    }
	
	public static int onRenderBlockLayer(RenderGlobal renderer,BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn){
		for(PhysicsWrapperEntity wrapper:ValkyrienWarfareMod.physicsManager.getManagerForWorld(renderer.theWorld).physicsEntities){
			if(wrapper.wrapping.renderer!=null&&wrapper.wrapping.renderer.shouldRender()){
				switch(blockLayerIn){
					case CUTOUT:
						if(wrapper.wrapping.renderer.needsCutoutUpdate){
							wrapper.wrapping.renderer.updateList(blockLayerIn);
						}
						break;
					case CUTOUT_MIPPED:
						if(wrapper.wrapping.renderer.needsCutoutMippedUpdate){
							wrapper.wrapping.renderer.updateList(blockLayerIn);
						}
						break;
					case SOLID:
						if(wrapper.wrapping.renderer.needsSolidUpdate){
							wrapper.wrapping.renderer.updateList(blockLayerIn);
						}
						break;
					case TRANSLUCENT:
						if(wrapper.wrapping.renderer.needsTranslucentUpdate){
							wrapper.wrapping.renderer.updateList(blockLayerIn);
						}
						break;
					default:
						break;
				}
				wrapper.wrapping.renderer.renderBlockLayer(blockLayerIn,partialTicks,pass);
			}
		}
		int toReturn = renderer.renderBlockLayer(blockLayerIn, partialTicks, pass, entityIn);
		GlStateManager.resetColor();
		return toReturn;
	}

}
