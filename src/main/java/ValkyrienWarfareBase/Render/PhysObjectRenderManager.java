package ValkyrienWarfareBase.Render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Interaction.FixedEntityData;
import ValkyrienWarfareBase.Math.Quaternion;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.Proxy.ClientProxy;
import ValkyrienWarfareCombat.Entity.EntityCannonBasic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;

/**
 * Object owned by each physObject responsible for handling all rendering operations
 * 
 * @author thebest108
 *
 */
public class PhysObjectRenderManager {

	public boolean needsSolidUpdate = true, needsCutoutUpdate = true, needsCutoutMippedUpdate = true, needsTranslucentUpdate = true;
	public int glCallListSolid = -1;
	public int glCallListTranslucent = -1;
	public int glCallListCutout = -1;
	public int glCallListCutoutMipped = -1;
	public PhysicsObject parent;
	// This pos is used to prevent Z-Buffer Errors D:
	// It's actual value is completely irrelevant as long as it's close to the
	// Ship's centerBlockPos
	public BlockPos offsetPos;
	public double curPartialTick;
	private FloatBuffer transformBuffer = null;
	public PhysRenderChunk[][] renderChunks;

	public PhysObjectRenderManager(PhysicsObject toRender) {
		parent = toRender;
	}

	public void updateOffsetPos(BlockPos newPos) {
		offsetPos = newPos;
	}

	public void updateList(BlockRenderLayer layerToUpdate) {
		if (offsetPos == null) {
			return;
		}
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(7, DefaultVertexFormats.BLOCK);
		worldrenderer.setTranslation(-offsetPos.getX(), -offsetPos.getY(), -offsetPos.getZ());
		GL11.glPushMatrix();
		switch (layerToUpdate) {
		case CUTOUT:
			GLAllocation.deleteDisplayLists(glCallListCutout);
			glCallListCutout = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(glCallListCutout, GL11.GL_COMPILE);
			break;
		case CUTOUT_MIPPED:
			GLAllocation.deleteDisplayLists(glCallListCutoutMipped);
			glCallListCutoutMipped = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(glCallListCutoutMipped, GL11.GL_COMPILE);
			break;
		case SOLID:
			GLAllocation.deleteDisplayLists(glCallListSolid);
			glCallListSolid = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(glCallListSolid, GL11.GL_COMPILE);
			break;
		case TRANSLUCENT:
			GLAllocation.deleteDisplayLists(glCallListTranslucent);
			glCallListTranslucent = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(glCallListTranslucent, GL11.GL_COMPILE);
			break;
		default:
			break;
		}

		GlStateManager.pushMatrix();
		// worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		IBlockState iblockstate;
		// if (Minecraft.isAmbientOcclusionEnabled()) {
		// GlStateManager.shadeModel(GL11.GL_SMOOTH);
		// } else {
		// GlStateManager.shadeModel(GL11.GL_FLAT);
		// }
		ForgeHooksClient.setRenderLayer(layerToUpdate);
		for (BlockPos pos : parent.blockPositions) {
			iblockstate = parent.worldObj.getBlockState(pos);
			if (iblockstate.getBlock().canRenderInLayer(iblockstate, layerToUpdate)) {
				Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(iblockstate, pos, parent.worldObj, worldrenderer);
			}
		}
		tessellator.draw();
		// worldrenderer.finishDrawing();
		ForgeHooksClient.setRenderLayer(null);
		GlStateManager.popMatrix();
		GL11.glEndList();
		GL11.glPopMatrix();
		worldrenderer.setTranslation(0, 0, 0);

		switch (layerToUpdate) {
		case CUTOUT:
			needsCutoutUpdate = false;
			break;
		case CUTOUT_MIPPED:
			needsCutoutMippedUpdate = false;
			break;
		case SOLID:
			needsSolidUpdate = false;
			break;
		case TRANSLUCENT:
			needsTranslucentUpdate = false;
			break;
		default:
			break;
		}
	}

	public void renderBlockLayer(BlockRenderLayer layerToRender, double partialTicks, int pass) {
		if (renderChunks == null) {
			if (parent.claimedChunks == null) {
				return;
			}
			renderChunks = new PhysRenderChunk[parent.claimedChunks.length][parent.claimedChunks.length];
			for (int xChunk = 0; xChunk < parent.claimedChunks.length; xChunk++) {
				for (int zChunk = 0; zChunk < parent.claimedChunks.length; zChunk++) {
					renderChunks[xChunk][zChunk] = new PhysRenderChunk(parent, parent.claimedChunks[xChunk][zChunk]);
				}
			}
		}

		GL11.glPushMatrix();
		Minecraft.getMinecraft().entityRenderer.enableLightmap();
		int i = parent.wrapper.getBrightnessForRender((float) partialTicks);

		int j = i % 65536;
		int k = i / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		setupTranslation(partialTicks);
		for (PhysRenderChunk[] chunkArray : renderChunks) {
			for (PhysRenderChunk renderChunk : chunkArray) {
				renderChunk.renderBlockLayer(layerToRender, partialTicks, pass);
			}
		}

		Minecraft.getMinecraft().entityRenderer.disableLightmap();
		GL11.glPopMatrix();
	}

	public void updateRange(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		if (renderChunks == null || parent == null || parent.ownedChunks == null) {
			return;
		}

		int minChunkX = minX >> 4;
		int maxChunkX = maxX >> 4;
		int minChunkZ = minZ >> 4;
		int maxChunkZ = maxZ >> 4;

		int minBlockArrayY = Math.max(0, minY >> 4);
		int maxBlockArrayY = Math.min(15, maxY >> 4);

		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				//TODO: Fix this render bug
				if(chunkX >= parent.ownedChunks.minX && chunkZ >= parent.ownedChunks.minZ){
					PhysRenderChunk renderChunk = renderChunks[chunkX - parent.ownedChunks.minX][chunkZ - parent.ownedChunks.minZ];
					renderChunk.updateLayers(minBlockArrayY, maxBlockArrayY);
				}else{
					ValkyrienWarfareMod.VWLogger.info("updateRange Just attempted to update blocks outside of a Ship's Block Range. ANY ERRORS PAST THIS ARE LIKELY RELATED!");
				}
			}
		}
	}

	public void renderTileEntities(float partialTicks) {
		for (BlockPos pos : parent.blockPositions) {
			TileEntity tileEnt = parent.worldObj.getTileEntity(pos);
			if (tileEnt != null) {
				try {
					TileEntityRendererDispatcher.instance.renderTileEntity(tileEnt, partialTicks, -1);
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}
	}

	public void renderEntities(float partialTicks) {
		ArrayList<FixedEntityData> fixedEntities = new ArrayList();// ArrayList<FixedEntityData>) parent.fixedEntities.clone();
		List<Entity> mountedEntities = parent.wrapper.riddenByEntities;
		for (Entity mounted : mountedEntities) {
			Vector localPosition = parent.getLocalPositionForEntity(mounted);
			
			if(localPosition != null){
				//Copy this vector, don't want to alter the original
				localPosition = new Vector(localPosition);
				
				localPosition.X -= offsetPos.getX();
				localPosition.Y -= offsetPos.getY();
				localPosition.Z -= offsetPos.getZ();
				
				Vector originalEntityPos = new Vector(mounted.posX, mounted.posY, mounted.posZ);
				Vector originalLastEntityPos = new Vector(mounted.lastTickPosX, mounted.lastTickPosY, mounted.lastTickPosZ);
	
				mounted.posX = mounted.lastTickPosX = localPosition.X;
				mounted.posY = mounted.lastTickPosY = localPosition.Y;
				mounted.posZ = mounted.lastTickPosZ = localPosition.Z;
	
//				System.out.println("test");
				if (!mounted.isDead && mounted != Minecraft.getMinecraft().getRenderViewEntity() || Minecraft.getMinecraft().gameSettings.thirdPersonView > 0) {
					if(mounted instanceof EntityCannonBasic){
						System.out.println("test");
					}
					GL11.glPushMatrix();
					int i = mounted.getBrightnessForRender(partialTicks);
					if (mounted.isBurning()) {
						i = 15728880;
					}
					int j = i % 65536;
					int k = i / 65536;
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j / 1.0F, (float) k / 1.0F);
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					float yaw = mounted.prevRotationYaw + (mounted.rotationYaw - mounted.prevRotationYaw) * partialTicks;
					double x = localPosition.X;
					double y = localPosition.Y;
					double z = localPosition.Z;
					Minecraft.getMinecraft().getRenderManager().doRenderEntity(mounted, x, y, z, yaw, partialTicks, false);
					GL11.glPopMatrix();
				}
	
				mounted.posX = originalEntityPos.X;
				mounted.posY = originalEntityPos.Y;
				mounted.posZ = originalEntityPos.Z;
				mounted.lastTickPosX = originalLastEntityPos.X;
				mounted.lastTickPosY = originalLastEntityPos.Y;
				mounted.lastTickPosZ = originalLastEntityPos.Z;
			
			}
		}
	}

	public boolean shouldRender() {
		ICamera camera = ((ClientProxy) ValkyrienWarfareMod.proxy).lastCamera;
		return camera == null || camera.isBoundingBoxInFrustum(parent.collisionBB);
	}

	public void setupTranslation(double partialTicks) {
		updateTranslation(partialTicks);
		// if(curPartialTick!=partialTicks){
		// updateTranslation(partialTicks);
		// }else{
		// updateTranslation(partialTicks);
		// curPartialTick = partialTicks;
		// }
	}

	public void updateTranslation(double partialTicks) {
		PhysicsWrapperEntity entity = parent.wrapper;
		Vector centerOfRotation = entity.wrapping.centerCoord;
		curPartialTick = partialTicks;

		double moddedX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
		double moddedY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
		double moddedZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
		double p0 = Minecraft.getMinecraft().thePlayer.lastTickPosX + (Minecraft.getMinecraft().thePlayer.posX - Minecraft.getMinecraft().thePlayer.lastTickPosX) * (double) partialTicks;
		double p1 = Minecraft.getMinecraft().thePlayer.lastTickPosY + (Minecraft.getMinecraft().thePlayer.posY - Minecraft.getMinecraft().thePlayer.lastTickPosY) * (double) partialTicks;
		double p2 = Minecraft.getMinecraft().thePlayer.lastTickPosZ + (Minecraft.getMinecraft().thePlayer.posZ - Minecraft.getMinecraft().thePlayer.lastTickPosZ) * (double) partialTicks;

		Quaternion smoothRotation = getSmoothRotationQuat(partialTicks);
		double[] radians = smoothRotation.toRadians();

		double moddedPitch = Math.toDegrees(radians[0]);
		double moddedYaw = Math.toDegrees(radians[1]);
		double moddedRoll = Math.toDegrees(radians[2]);

		parent.coordTransform.updateRenderMatrices(moddedX, moddedY, moddedZ, moddedPitch, moddedYaw, moddedRoll);

		if (offsetPos != null) {
			double offsetX = offsetPos.getX() - centerOfRotation.X;
			double offsetY = offsetPos.getY() - centerOfRotation.Y;
			double offsetZ = offsetPos.getZ() - centerOfRotation.Z;

			GlStateManager.translate(-p0 + moddedX, -p1 + moddedY, -p2 + moddedZ);
			GL11.glRotated(moddedPitch, 1D, 0, 0);
			GL11.glRotated(moddedYaw, 0, 1D, 0);
			GL11.glRotated(moddedRoll, 0, 0, 1D);
			GL11.glTranslated(offsetX, offsetY, offsetZ);
			// transformBuffer = BufferUtils.createFloatBuffer(16);
		}
	}

	public Quaternion getSmoothRotationQuat(double partialTick) {
		PhysicsWrapperEntity entity = parent.wrapper;
		double[] oldRotation = RotationMatrices.getDoubleIdentity();
		oldRotation = RotationMatrices.rotateAndTranslate(oldRotation, entity.prevPitch, entity.prevYaw, entity.prevRoll, new Vector());
		Quaternion oneTickBefore = Quaternion.QuaternionFromMatrix(oldRotation);
		double[] newRotation = RotationMatrices.getDoubleIdentity();
		newRotation = RotationMatrices.rotateAndTranslate(newRotation, entity.pitch, entity.yaw, entity.roll, new Vector());
		Quaternion nextQuat = Quaternion.QuaternionFromMatrix(newRotation);
		return Quaternion.getBetweenQuat(oneTickBefore, nextQuat, partialTick);
	}

	// TODO: Program me
	public void inverseTransform(double partialTicks) {

	}

	public void markForUpdate() {
		needsCutoutUpdate = true;
		needsCutoutMippedUpdate = true;
		needsSolidUpdate = true;
		needsTranslucentUpdate = true;
	}

}
