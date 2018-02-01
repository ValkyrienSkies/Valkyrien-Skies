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

package valkyrienwarfare.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class FastBlockModelRenderer {

    public static HashMap<IBlockState, BufferBuilder.State> blockstateToVertexData = new HashMap<IBlockState, BufferBuilder.State>();
    public static HashMap<IBlockState, Map<Integer, Integer>> highRamGLList = new HashMap<IBlockState, Map<Integer, Integer>>();

    public static void renderBlockModel(BufferBuilder BufferBuilder, Tessellator tessellator, World world, IBlockState blockstateToRender, int brightness) {
        renderBlockModelHighQualityHighRam(BufferBuilder, tessellator, world, blockstateToRender, brightness);
    }

    private static void renderBlockModelHighQualityHighRam(BufferBuilder BufferBuilder, Tessellator tessellator, World world, IBlockState blockstateToRender, int brightness) {
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
            renderBlockModelHighQuality(BufferBuilder, tessellator, world, blockstateToRender, brightness);
            GL11.glEndList();
            GL11.glPopMatrix();
            glListForBrightness = glList;
            brightnessToGLListMap.put(brightness, glList);
        }

        GL11.glPushMatrix();
        GL11.glCallList(glListForBrightness);
        GL11.glPopMatrix();
    }

    private static void renderBlockModelHighQuality(BufferBuilder BufferBuilder, Tessellator tessellator, World world, IBlockState blockstateToRender, int brightness) {
        BufferBuilder.State vertexData = blockstateToVertexData.get(blockstateToRender);

        double oldX = BufferBuilder.xOffset;
        double oldY = BufferBuilder.yOffset;
        double oldZ = BufferBuilder.zOffset;

//		BufferBuilder.setTranslation(0, 0, 0);

        if (vertexData == null) {
            generateRenderDataFor(BufferBuilder, tessellator, world, blockstateToRender);
            vertexData = blockstateToVertexData.get(blockstateToRender);
        }
        renderVertexState(vertexData, BufferBuilder, tessellator, brightness);

//		BufferBuilder.setTranslation(oldX, oldY, oldZ);
    }

    private static void renderVertexState(BufferBuilder.State data, BufferBuilder BufferBuilder, Tessellator tessellator, int brightness) {
        GL11.glPushMatrix();
        BufferBuilder.begin(7, DefaultVertexFormats.BLOCK);

        BufferBuilder.setVertexState(data);
        int j = BufferBuilder.vertexFormat.getSize() >> 2;
        int cont = BufferBuilder.getVertexCount();
        int offsetUV = BufferBuilder.vertexFormat.getUvOffsetById(1) / 4;
        int bufferNextSize = BufferBuilder.vertexFormat.getIntegerSize();
        for (int contont = 0; contont < cont; contont += 4) {
            try {
                int i = (contont) * bufferNextSize + offsetUV;

                BufferBuilder.rawIntBuffer.put(i, brightness);
                BufferBuilder.rawIntBuffer.put(i + j, brightness);
                BufferBuilder.rawIntBuffer.put(i + j * 2, brightness);
                BufferBuilder.rawIntBuffer.put(i + j * 3, brightness);

                if (contont + 4 < cont) {
                    contont += 4;

                    i = (contont) * bufferNextSize + offsetUV;

                    BufferBuilder.rawIntBuffer.put(i, brightness);
                    BufferBuilder.rawIntBuffer.put(i + j, brightness);
                    BufferBuilder.rawIntBuffer.put(i + j * 2, brightness);
                    BufferBuilder.rawIntBuffer.put(i + j * 3, brightness);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tessellator.draw();

        GL11.glPopMatrix();
    }

    private static void generateRenderDataFor(BufferBuilder BufferBuilder, Tessellator tessellator, World world, IBlockState state) {
        GL11.glPushMatrix();
        BufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, BlockPos.ORIGIN, BufferBuilder, false, 0);
        BufferBuilder.State toReturn = BufferBuilder.getVertexState();
        tessellator.draw();
        GL11.glPopMatrix();
        blockstateToVertexData.put(state, toReturn);
    }

}
