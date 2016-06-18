package ValkyrienWarfareBase.CoreMod;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;

public class CallRunnerClient {

	public static int onRenderBlockLayer(RenderGlobal renderer,BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn){
		for(Entity ent:Minecraft.getMinecraft().theWorld.loadedEntityList){
			if(ent instanceof PhysicsWrapperEntity){
				PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity)ent;
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
				
			}
		}
		return renderer.renderBlockLayer(blockLayerIn, partialTicks, pass, entityIn);
	}
	
	public static void onPreRenderChunk(ChunkRenderContainer container,RenderChunk toRender){
		container.preRenderChunk(toRender);
	}
	
	/**
	 * This fucking sucks how Minecraft's garbage code forces me to do this ALL TO ADD SOME
	 * RENDER CHUNKS!!!!
	 * @param renderGlobal
	 * @param viewEntity
	 * @param partialTicks
	 * @param camera
	 * @param frameCount
	 * @param playerSpectator
	 */
	public static void onSetupTerrain(RenderGlobal renderGlobal,Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator){
		renderGlobal.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
		/*if (renderGlobal.mc.gameSettings.renderDistanceChunks != renderGlobal.renderDistanceChunks)
        {
            renderGlobal.loadRenderers();
        }

        renderGlobal.theWorld.theProfiler.startSection("camera");
        double d0 = viewEntity.posX - renderGlobal.frustumUpdatePosX;
        double d1 = viewEntity.posY - renderGlobal.frustumUpdatePosY;
        double d2 = viewEntity.posZ - renderGlobal.frustumUpdatePosZ;

        if (renderGlobal.frustumUpdatePosChunkX != viewEntity.chunkCoordX || renderGlobal.frustumUpdatePosChunkY != viewEntity.chunkCoordY || renderGlobal.frustumUpdatePosChunkZ != viewEntity.chunkCoordZ || d0 * d0 + d1 * d1 + d2 * d2 > 16.0D)
        {
            renderGlobal.frustumUpdatePosX = viewEntity.posX;
            renderGlobal.frustumUpdatePosY = viewEntity.posY;
            renderGlobal.frustumUpdatePosZ = viewEntity.posZ;
            renderGlobal.frustumUpdatePosChunkX = viewEntity.chunkCoordX;
            renderGlobal.frustumUpdatePosChunkY = viewEntity.chunkCoordY;
            renderGlobal.frustumUpdatePosChunkZ = viewEntity.chunkCoordZ;
            renderGlobal.viewFrustum.updateChunkPositions(viewEntity.posX, viewEntity.posZ);
        }

        renderGlobal.theWorld.theProfiler.endStartSection("renderlistcamera");
        double d3 = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double d4 = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
        double d5 = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;
        renderGlobal.renderContainer.initialize(d3, d4, d5);
        renderGlobal.theWorld.theProfiler.endStartSection("cull");

        if (renderGlobal.debugFixedClippingHelper != null)
        {
            Frustum frustum = new Frustum(renderGlobal.debugFixedClippingHelper);
            frustum.setPosition(renderGlobal.debugTerrainFrustumPosition.x, renderGlobal.debugTerrainFrustumPosition.y, renderGlobal.debugTerrainFrustumPosition.z);
            camera = frustum;
        }

        renderGlobal.mc.mcProfiler.endStartSection("culling");
        BlockPos blockpos1 = new BlockPos(d3, d4 + (double)viewEntity.getEyeHeight(), d5);
        RenderChunk renderchunk = renderGlobal.viewFrustum.getRenderChunk(blockpos1);
        BlockPos blockpos = new BlockPos(MathHelper.floor_double(d3 / 16.0D) * 16, MathHelper.floor_double(d4 / 16.0D) * 16, MathHelper.floor_double(d5 / 16.0D) * 16);
        renderGlobal.displayListEntitiesDirty = renderGlobal.displayListEntitiesDirty || !renderGlobal.chunksToUpdate.isEmpty() || viewEntity.posX != renderGlobal.lastViewEntityX || viewEntity.posY != renderGlobal.lastViewEntityY || viewEntity.posZ != renderGlobal.lastViewEntityZ || (double)viewEntity.rotationPitch != renderGlobal.lastViewEntityPitch || (double)viewEntity.rotationYaw != renderGlobal.lastViewEntityYaw;
        renderGlobal.lastViewEntityX = viewEntity.posX;
        renderGlobal.lastViewEntityY = viewEntity.posY;
        renderGlobal.lastViewEntityZ = viewEntity.posZ;
        renderGlobal.lastViewEntityPitch = (double)viewEntity.rotationPitch;
        renderGlobal.lastViewEntityYaw = (double)viewEntity.rotationYaw;
        boolean flag = renderGlobal.debugFixedClippingHelper != null;
        renderGlobal.mc.mcProfiler.endStartSection("update");

        if (!flag && renderGlobal.displayListEntitiesDirty)
        {
            renderGlobal.displayListEntitiesDirty = false;
            renderGlobal.renderInfos = Lists.<RenderGlobal.ContainerLocalRenderInformation>newArrayList();
            Queue<RenderGlobal.ContainerLocalRenderInformation> queue = Lists.<RenderGlobal.ContainerLocalRenderInformation>newLinkedList();
            Entity.setRenderDistanceWeight(MathHelper.clamp_double((double)renderGlobal.mc.gameSettings.renderDistanceChunks / 8.0D, 1.0D, 2.5D));
            boolean flag1 = renderGlobal.mc.renderChunksMany;

            if (renderchunk != null)
            {
                boolean flag2 = false;
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation3 = new RenderGlobal.ContainerLocalRenderInformation(renderchunk, (EnumFacing)null, 0);
                Set<EnumFacing> set1 = renderGlobal.getVisibleFacings(blockpos1);

                if (set1.size() == 1)
                {
                    Vector3f vector3f = renderGlobal.getViewVector(viewEntity, partialTicks);
                    EnumFacing enumfacing = EnumFacing.getFacingFromVector(vector3f.x, vector3f.y, vector3f.z).getOpposite();
                    set1.remove(enumfacing);
                }

                if (set1.isEmpty())
                {
                    flag2 = true;
                }

                if (flag2 && !playerSpectator)
                {
                    renderGlobal.renderInfos.add(renderglobal$containerlocalrenderinformation3);
                }
                else
                {
                    if (playerSpectator && renderGlobal.theWorld.getBlockState(blockpos1).isOpaqueCube())
                    {
                        flag1 = false;
                    }

                    renderchunk.setFrameIndex(frameCount);
                    queue.add(renderglobal$containerlocalrenderinformation3);
                }
            }
            else
            {
                int i = blockpos1.getY() > 0 ? 248 : 8;

                for (int j = -renderGlobal.renderDistanceChunks; j <= renderGlobal.renderDistanceChunks; ++j)
                {
                    for (int k = -renderGlobal.renderDistanceChunks; k <= renderGlobal.renderDistanceChunks; ++k)
                    {
                        RenderChunk renderchunk1 = renderGlobal.viewFrustum.getRenderChunk(new BlockPos((j << 4) + 8, i, (k << 4) + 8));

                        if (renderchunk1 != null && ((ICamera)camera).isBoundingBoxInFrustum(renderchunk1.boundingBox))
                        {
                            renderchunk1.setFrameIndex(frameCount);
                            queue.add(new RenderGlobal.ContainerLocalRenderInformation(renderchunk1, (EnumFacing)null, 0));
                        }
                    }
                }
            }

            renderGlobal.mc.mcProfiler.startSection("iteration");

            while (!((Queue)queue).isEmpty())
            {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 = (RenderGlobal.ContainerLocalRenderInformation)queue.poll();
                RenderChunk renderchunk3 = renderglobal$containerlocalrenderinformation1.renderChunk;
                EnumFacing enumfacing2 = renderglobal$containerlocalrenderinformation1.facing;
                BlockPos blockpos3 = renderchunk3.getPosition();
                renderGlobal.renderInfos.add(renderglobal$containerlocalrenderinformation1);

                for (EnumFacing enumfacing1 : EnumFacing.values())
                {
                    RenderChunk renderchunk2 = renderGlobal.getRenderChunkOffset(blockpos, renderchunk3, enumfacing1);

                    if ((!flag1 || !renderglobal$containerlocalrenderinformation1.setFacing.contains(enumfacing1.getOpposite())) && (!flag1 || enumfacing2 == null || renderchunk3.getCompiledChunk().isVisible(enumfacing2.getOpposite(), enumfacing1)) && renderchunk2 != null && renderchunk2.setFrameIndex(frameCount) && ((ICamera)camera).isBoundingBoxInFrustum(renderchunk2.boundingBox))
                    {
                        RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = new RenderGlobal.ContainerLocalRenderInformation(renderchunk2, enumfacing1, renderglobal$containerlocalrenderinformation1.counter + 1);
                        renderglobal$containerlocalrenderinformation.setFacing.addAll(renderglobal$containerlocalrenderinformation1.setFacing);
                        renderglobal$containerlocalrenderinformation.setFacing.add(enumfacing1);
                        queue.add(renderglobal$containerlocalrenderinformation);
                    }
                }
            }

            renderGlobal.mc.mcProfiler.endSection();
        }

        renderGlobal.mc.mcProfiler.endStartSection("captureFrustum");

        if (renderGlobal.debugFixTerrainFrustum)
        {
            renderGlobal.fixTerrainFrustum(d3, d4, d5);
            renderGlobal.debugFixTerrainFrustum = false;
        }

        renderGlobal.mc.mcProfiler.endStartSection("rebuildNear");
        Set<RenderChunk> set = renderGlobal.chunksToUpdate;
        renderGlobal.chunksToUpdate = Sets.<RenderChunk>newLinkedHashSet();

        for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation2 : renderGlobal.renderInfos)
        {
            RenderChunk renderchunk4 = renderglobal$containerlocalrenderinformation2.renderChunk;

            if (renderchunk4.isNeedsUpdate() || set.contains(renderchunk4))
            {
                renderGlobal.displayListEntitiesDirty = true;
                BlockPos blockpos2 = renderchunk4.getPosition().add(8, 8, 8);
                boolean flag3 = blockpos2.distanceSq(blockpos1) < 768.0D;

                if (!renderchunk4.isNeedsUpdateCustom() && !flag3)
                {
                    renderGlobal.chunksToUpdate.add(renderchunk4);
                }
                else
                {
                    renderGlobal.mc.mcProfiler.startSection("build near");
                    renderGlobal.renderDispatcher.updateChunkNow(renderchunk4);
                    renderchunk4.clearNeedsUpdate();
                    renderGlobal.mc.mcProfiler.endSection();
                }
            }
        }

        renderGlobal.chunksToUpdate.addAll(set);
        renderGlobal.mc.mcProfiler.endSection();*/
	}
}
