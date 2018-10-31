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

package valkyrienwarfare.mod.client.render;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// TODO: Upon further inspection this class does the exact opposite of what its name implies 
// and takes a stupid slow approach to rendering simple geometries. Remove this and create a 
// vertex buffer based solution soon!
public class FastBlockModelRenderer {

	public static final Map<IBlockState, BufferBuilder.State> blockstateToVertexData = new HashMap<IBlockState, BufferBuilder.State>();
	// Maps IBlockState to a map that maps brightness to VertexBuffer that are already uploaded to gpu memory.
	public static final Map<IBlockState, Map<Integer, VertexBuffer>> blockstateBrightnessToVertexBuffer = new HashMap<IBlockState, Map<Integer, VertexBuffer>>();
	
	protected static final BufferBuilder VERTEX_BUILDER = new BufferBuilder(500000);
	
    public static void renderBlockModel(Tessellator tessellator, World world, IBlockState blockstateToRender, int brightness) {
        renderBlockModelHighQualityHighRam(tessellator, world, blockstateToRender, brightness);
	}

	private static void renderBlockModelHighQualityHighRam(Tessellator tessellator, World world,
			IBlockState blockstateToRender, int brightness) {
		if (!blockstateToVertexData.containsKey(blockstateToRender)) {
			generateRenderDataFor(world, blockstateToRender);
		}

		// We're using the VBO, check if a compiled VertexBuffer already exists. If
		// there isn't one we will create it, then render.
		if (!blockstateBrightnessToVertexBuffer.containsKey(blockstateToRender)) {
			blockstateBrightnessToVertexBuffer.put(blockstateToRender, new HashMap<Integer, VertexBuffer>());
		}
		if (!blockstateBrightnessToVertexBuffer.get(blockstateToRender).containsKey(brightness)) {
			// We have to create the VertexBuffer
			BufferBuilder.State bufferBuilderState = blockstateToVertexData.get(blockstateToRender);

			VERTEX_BUILDER.setTranslation(0, 0, 0);
			VERTEX_BUILDER.begin(7, DefaultVertexFormats.BLOCK);
			VERTEX_BUILDER.setVertexState(bufferBuilderState);

			// This code adjusts the brightness of the model rendered.
			int j = VERTEX_BUILDER.vertexFormat.getNextOffset() >> 2;
			int cont = VERTEX_BUILDER.getVertexCount();
			int offsetUV = VERTEX_BUILDER.vertexFormat.getUvOffsetById(1) / 4;
			int bufferNextSize = VERTEX_BUILDER.vertexFormat.getIntegerSize();

			for (int contont = 0; contont < cont; contont += 4) {
				try {
					int i = (contont) * bufferNextSize + offsetUV;
					VERTEX_BUILDER.rawIntBuffer.put(i, brightness);
					VERTEX_BUILDER.rawIntBuffer.put(i + j, brightness);
					VERTEX_BUILDER.rawIntBuffer.put(i + j * 2, brightness);
					VERTEX_BUILDER.rawIntBuffer.put(i + j * 3, brightness);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			VertexBuffer blockVertexBuffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
			// Now that the VERTEX_BUILDER has been filled with all the render data, we must
			// upload it to the gpu.
			// The VERTEX_UPLOADER copies the state of the VERTEX_BUILDER to
			// blockVertexBuffer, and then uploads it to the gpu.
			VERTEX_BUILDER.finishDrawing();
			VERTEX_BUILDER.reset();
			blockVertexBuffer.bufferData(VERTEX_BUILDER.getByteBuffer());
			// Put the VertexBuffer for that data into the Map for future rendering.
			blockstateBrightnessToVertexBuffer.get(blockstateToRender).put(brightness, blockVertexBuffer);
		}

		// Just to test the look of the State in case I ever need to.
		if (false) {
			BufferBuilder.State bufferBuilderState = blockstateToVertexData.get(blockstateToRender);
			tessellator.getBuffer().begin(7, DefaultVertexFormats.BLOCK);
			tessellator.getBuffer().setVertexState(bufferBuilderState);
			tessellator.getBuffer().finishDrawing();
			tessellator.draw();
		}

		GlStateManager.disableLighting();
		renderVertexBuffer(blockstateBrightnessToVertexBuffer.get(blockstateToRender).get(brightness));		
		GlStateManager.enableLighting();
	}
	
	protected static void renderVertexBuffer(VertexBuffer vertexBuffer) {
		GlStateManager.pushMatrix();
		// Guaranteed not be null.
		GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);

		GlStateManager.pushMatrix();
		vertexBuffer.bindBuffer();

		GlStateManager.glVertexPointer(3, 5126, 28, 0);
		GlStateManager.glColorPointer(4, 5121, 28, 12);
		GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);

		vertexBuffer.drawArrays(7);
		GlStateManager.popMatrix();
		vertexBuffer.unbindBuffer();
		GlStateManager.resetColor();

		for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements()) {
			VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
			int i = vertexformatelement.getIndex();

			switch (vertexformatelement$enumusage) {
			case POSITION:
				GlStateManager.glDisableClientState(32884);
				break;
			case UV:
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i);
				GlStateManager.glDisableClientState(32888);
				OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
				break;
			case COLOR:
				GlStateManager.glDisableClientState(32886);
				GlStateManager.resetColor();
			}
		}
		GlStateManager.popMatrix();
	}

    private static void renderBlockModelHighQuality(Tessellator tessellator, World world, IBlockState blockstateToRender, int brightness) {
        BufferBuilder.State vertexData = blockstateToVertexData.get(blockstateToRender);

        if (vertexData == null) {
            generateRenderDataFor(world, blockstateToRender);
            vertexData = blockstateToVertexData.get(blockstateToRender);
        }
        renderVertexState(vertexData, tessellator, brightness);
    }

    private static void renderVertexState(BufferBuilder.State data, Tessellator tessellator, int brightness) {
        GL11.glPushMatrix();
        tessellator.getBuffer().begin(7, DefaultVertexFormats.BLOCK);

        tessellator.getBuffer().setVertexState(data);
        int j = tessellator.getBuffer().vertexFormat.getNextOffset() >> 2;
        int cont = tessellator.getBuffer().getVertexCount();
        int offsetUV = tessellator.getBuffer().vertexFormat.getUvOffsetById(1) / 4;
        int bufferNextSize = tessellator.getBuffer().vertexFormat.getIntegerSize();
        
        for (int contont = 0; contont < cont; contont += 4) {
            try {
                int i = (contont) * bufferNextSize + offsetUV;

                tessellator.getBuffer().rawIntBuffer.put(i, brightness);
                tessellator.getBuffer().rawIntBuffer.put(i + j, brightness);
                tessellator.getBuffer().rawIntBuffer.put(i + j * 2, brightness);
                tessellator.getBuffer().rawIntBuffer.put(i + j * 3, brightness);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tessellator.draw();

        GL11.glPopMatrix();
    }

    private static void generateRenderDataFor(World world, IBlockState state) {
        VERTEX_BUILDER.begin(7, DefaultVertexFormats.BLOCK);
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBakedModel modelFromState = blockrendererdispatcher.getModelForState(state);
        blockrendererdispatcher.getBlockModelRenderer().renderModel(Minecraft.getMinecraft().world, modelFromState, Blocks.AIR.getDefaultState(), BlockPos.ORIGIN, VERTEX_BUILDER, false, 0);
        BufferBuilder.State toReturn = VERTEX_BUILDER.getVertexState();
        VERTEX_BUILDER.finishDrawing();
        VERTEX_BUILDER.reset();
        blockstateToVertexData.put(state, toReturn);
    }

}
