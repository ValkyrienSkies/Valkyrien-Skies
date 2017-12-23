/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class FastBlockModelRenderer {
	
	public static HashMap<IBlockState, VertexBuffer.State> blockstateToVertexData = new HashMap<IBlockState, VertexBuffer.State>();
	public static HashMap<IBlockState, Map<Integer, Integer>> highRamGLList = new HashMap<IBlockState, Map<Integer, Integer>>();
	
	public static void renderBlockModel(VertexBuffer vertexbuffer, Tessellator tessellator, World world, IBlockState blockstateToRender, int brightness) {
		renderBlockModelHighQualityHighRam(vertexbuffer, tessellator, world, blockstateToRender, brightness);
	}
	
	private static void renderBlockModelHighQualityHighRam(VertexBuffer vertexbuffer, Tessellator tessellator, World world, IBlockState blockstateToRender, int brightness) {
		Map<Integer, Integer> brightnessToGLListMap = highRamGLList.get(blockstateToRender);
		
		if (brightnessToGLListMap == null) {
			highRamGLList.put(blockstateToRender, new HashMap<Integer, Integer>());
			brightnessToGLListMap = highRamGLList.get(blockstateToRender);
		}
		
		Integer glListForBrightness = brightnessToGLListMap.get(brightness);
		if (glListForBrightness == null) {
			GL11.glPushMatrix();
			int glList = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(glList, GL11.GL_COMPILE);
			renderBlockModelHighQuality(vertexbuffer, tessellator, world, blockstateToRender, brightness);
			GL11.glEndList();
			GL11.glPopMatrix();
			glListForBrightness = glList;
			brightnessToGLListMap.put(brightness, glList);
		}
		
		GL11.glPushMatrix();
		GL11.glCallList(glListForBrightness);
		GL11.glPopMatrix();
	}
	
	private static void renderBlockModelHighQuality(VertexBuffer vertexbuffer, Tessellator tessellator, World world, IBlockState blockstateToRender, int brightness) {
		VertexBuffer.State vertexData = blockstateToVertexData.get(blockstateToRender);
		
		double oldX = vertexbuffer.xOffset;
		double oldY = vertexbuffer.yOffset;
		double oldZ = vertexbuffer.zOffset;

//		vertexbuffer.setTranslation(0, 0, 0);
		
		if (vertexData == null) {
			generateRenderDataFor(vertexbuffer, tessellator, world, blockstateToRender);
			vertexData = blockstateToVertexData.get(blockstateToRender);
		}
		renderVertexState(vertexData, vertexbuffer, tessellator, brightness);

//		vertexbuffer.setTranslation(oldX, oldY, oldZ);
	}
	
	private static void renderVertexState(VertexBuffer.State data, VertexBuffer vertexbuffer, Tessellator tessellator, int brightness) {
		GL11.glPushMatrix();
		vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
		
		vertexbuffer.setVertexState(data);
		int j = vertexbuffer.vertexFormat.getNextOffset() >> 2;
		int cont = vertexbuffer.getVertexCount();
		int offsetUV = vertexbuffer.vertexFormat.getUvOffsetById(1) / 4;
		int bufferNextSize = vertexbuffer.vertexFormat.getIntegerSize();
		for (int contont = 0; contont < cont; contont += 4) {
			try {
				int i = (contont) * bufferNextSize + offsetUV;
				
				vertexbuffer.rawIntBuffer.put(i, brightness);
				vertexbuffer.rawIntBuffer.put(i + j, brightness);
				vertexbuffer.rawIntBuffer.put(i + j * 2, brightness);
				vertexbuffer.rawIntBuffer.put(i + j * 3, brightness);
				
				if (contont + 4 < cont) {
					contont += 4;
					
					i = (contont) * bufferNextSize + offsetUV;
					
					vertexbuffer.rawIntBuffer.put(i, brightness);
					vertexbuffer.rawIntBuffer.put(i + j, brightness);
					vertexbuffer.rawIntBuffer.put(i + j * 2, brightness);
					vertexbuffer.rawIntBuffer.put(i + j * 3, brightness);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		tessellator.draw();
		
		GL11.glPopMatrix();
	}
	
	private static void generateRenderDataFor(VertexBuffer vertexbuffer, Tessellator tessellator, World world, IBlockState state) {
		GL11.glPushMatrix();
		vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
		BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, BlockPos.ORIGIN, vertexbuffer, false, 0);
		VertexBuffer.State toReturn = vertexbuffer.getVertexState();
		tessellator.draw();
		GL11.glPopMatrix();
		blockstateToVertexData.put(state, toReturn);
	}
	
}
