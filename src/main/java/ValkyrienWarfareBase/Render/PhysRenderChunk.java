package ValkyrienWarfareBase.Render;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.client.ForgeHooksClient;

public class PhysRenderChunk {

	public RenderLayer[] layers = new RenderLayer[16];
	public PhysicsObject toRender;
	public Chunk renderChunk;

	public PhysRenderChunk(PhysicsObject toRender, Chunk renderChunk) {
		this.toRender = toRender;
		this.renderChunk = renderChunk;
		for (int i = 0; i < 16; i++) {
			ExtendedBlockStorage storage = renderChunk.storageArrays[i];
			if (storage != null) {
				RenderLayer renderLayer = new RenderLayer(renderChunk, i * 16, i * 16 + 15, this);
				layers[i] = renderLayer;
			}
		}
	}

	public void renderBlockLayer(BlockRenderLayer layerToRender, double partialTicks, int pass) {
		for (int i = 0; i < 16; i++) {
			RenderLayer layer = layers[i];
			if (layer != null) {
				layer.renderBlockLayer(layerToRender, partialTicks, pass);
			}
		}
	}

	public void updateLayers(int minLayer, int maxLayer) {
		for (int layerY = minLayer; layerY <= maxLayer; layerY++) {
			RenderLayer layer = layers[layerY];
			if (layer != null) {
				layer.updateRenderLists();
			} else {
				RenderLayer renderLayer = new RenderLayer(renderChunk, layerY * 16, layerY * 16 + 15, this);
				layers[layerY] = renderLayer;
			}
		}
	}

	public class RenderLayer {

		Chunk chunkToRender;
		int yMin, yMax;
		int glCallListCutout, glCallListCutoutMipped, glCallListSolid, glCallListTranslucent;
		PhysRenderChunk parent;
		boolean needsCutoutUpdate, needsCutoutMippedUpdate, needsSolidUpdate, needsTranslucentUpdate;

		public RenderLayer(Chunk chunk, int yMin, int yMax, PhysRenderChunk parent) {
			chunkToRender = chunk;
			this.yMin = yMin;
			this.yMax = yMax;
			this.parent = parent;
			updateRenderLists();
			glCallListCutout = GLAllocation.generateDisplayLists(4);
			glCallListCutoutMipped = glCallListCutout + 1;
			glCallListSolid = glCallListCutout + 2;
			glCallListTranslucent = glCallListCutout + 3;
		}

		public void updateRenderLists() {
			needsCutoutUpdate = true;
			needsCutoutMippedUpdate = true;
			needsSolidUpdate = true;
			needsTranslucentUpdate = true;
		}

		public void renderBlockLayer(BlockRenderLayer layerToRender, double partialTicks, int pass) {
			switch (layerToRender) {
			case CUTOUT:
				if (needsCutoutUpdate) {
					updateList(layerToRender);
				}
				GL11.glCallList(glCallListCutout);
				break;
			case CUTOUT_MIPPED:
				if (needsCutoutMippedUpdate) {
					updateList(layerToRender);
				}
				GL11.glCallList(glCallListCutoutMipped);
				break;
			case SOLID:
				if (needsSolidUpdate) {
					updateList(layerToRender);
				}
				GL11.glCallList(glCallListSolid);
				break;
			case TRANSLUCENT:
				if (needsTranslucentUpdate) {
					updateList(layerToRender);
				}
				GL11.glCallList(glCallListTranslucent);
				break;
			default:
				break;
			}
		}

		public void updateList(BlockRenderLayer layerToUpdate) {
			if (parent.toRender.renderer == null) {
				return;
			}
			BlockPos offsetPos = parent.toRender.renderer.offsetPos;
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
				GL11.glNewList(glCallListCutout, GL11.GL_COMPILE);
				break;
			case CUTOUT_MIPPED:
				GLAllocation.deleteDisplayLists(glCallListCutoutMipped);
				GL11.glNewList(glCallListCutoutMipped, GL11.GL_COMPILE);
				break;
			case SOLID:
				GLAllocation.deleteDisplayLists(glCallListSolid);
				GL11.glNewList(glCallListSolid, GL11.GL_COMPILE);
				break;
			case TRANSLUCENT:
				GLAllocation.deleteDisplayLists(glCallListTranslucent);
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
			MutableBlockPos pos = new MutableBlockPos();
			for (int x = chunkToRender.xPosition * 16; x < chunkToRender.xPosition * 16 + 16; x++) {
				for (int z = chunkToRender.zPosition * 16; z < chunkToRender.zPosition * 16 + 16; z++) {
					for (int y = yMin; y <= yMax; y++) {
						pos.setPos(x, y, z);
						iblockstate = chunkToRender.getBlockState(pos);
						if (iblockstate.getBlock().canRenderInLayer(iblockstate, layerToUpdate)) {
							Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(iblockstate, pos, chunkToRender.worldObj, worldrenderer);
						}
					}
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
	}
}
