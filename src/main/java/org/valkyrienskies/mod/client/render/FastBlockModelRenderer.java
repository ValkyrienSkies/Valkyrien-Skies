package org.valkyrienskies.mod.client.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

// TODO: Upon further inspection this class does the exact opposite of what its name implies 
// and takes a stupid slow approach to rendering simple geometries. Remove this and create a 
// vertex buffer based solution soon!
public class FastBlockModelRenderer {

    public static final Map<IBlockState, BufferBuilder.State> blockstateToVertexData = new HashMap<IBlockState, BufferBuilder.State>();
    // Maps IBlockState to a map that maps brightness to VertexBuffer that are already uploaded to gpu memory.
    public static final Map<IBlockState, Map<Integer, VertexBuffer>> blockstateBrightnessToVertexBuffer = new HashMap<IBlockState, Map<Integer, VertexBuffer>>();

    protected static final BufferBuilder VERTEX_BUILDER = new BufferBuilder(500000);
    // Used to make sure that when we simulate rendering models they're not affected by light from other blocks.
    private static final BlockPos offsetPos = new BlockPos(0, 512, 0);

    public static void renderBlockModel(Tessellator tessellator, World world,
        IBlockState blockstateToRender, int brightness) {
        GL11.glPushMatrix();
        GL11.glTranslated(-offsetPos.getX(), -offsetPos.getY(), -offsetPos.getZ());
        renderBlockModelHighQualityHighRam(tessellator, world, blockstateToRender, brightness);
        GL11.glPopMatrix();
    }

    private static void renderBlockModelHighQualityHighRam(Tessellator tessellator, World world,
        IBlockState blockstateToRender, int brightness) {
        if (!blockstateToVertexData.containsKey(blockstateToRender)) {
            generateRenderDataFor(world, blockstateToRender);
        }

        // We're using the VBO, check if a compiled VertexBuffer already exists. If
        // there isn't one we will create it, then render.
        if (!blockstateBrightnessToVertexBuffer.containsKey(blockstateToRender)) {
            blockstateBrightnessToVertexBuffer
                .put(blockstateToRender, new HashMap<Integer, VertexBuffer>());
        }
        if (!blockstateBrightnessToVertexBuffer.get(blockstateToRender).containsKey(brightness)) {
            // We have to create the VertexBuffer
            BufferBuilder.State bufferBuilderState = blockstateToVertexData.get(blockstateToRender);

            VERTEX_BUILDER.setTranslation(0, 0, 0);
            VERTEX_BUILDER.begin(7, DefaultVertexFormats.BLOCK);
            VERTEX_BUILDER.setVertexState(bufferBuilderState);

            // This code adjusts the brightness of the model rendered.
            int j = VERTEX_BUILDER.vertexFormat.getSize() >> 2;
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
            blockstateBrightnessToVertexBuffer.get(blockstateToRender)
                .put(brightness, blockVertexBuffer);
        }

        // Just to test the look of the State in case I ever need to.
        if (false) {
            BufferBuilder.State bufferBuilderState = blockstateToVertexData.get(blockstateToRender);
            tessellator.getBuffer().begin(7, DefaultVertexFormats.BLOCK);
            tessellator.getBuffer().setVertexState(bufferBuilderState);
            tessellator.getBuffer().finishDrawing();
            tessellator.draw();
        }

        renderVertexBuffer(
            blockstateBrightnessToVertexBuffer.get(blockstateToRender).get(brightness));
    }

    protected static void renderVertexBuffer(VertexBuffer vertexBuffer) {
        // Check if optifine shaders are currently loaded.
        final boolean areOptifineShadersEnabled = GibsModelRegistry.isOptifineShadersEnabled();

        GlStateManager.pushMatrix();
        GlStateManager.resetColor();

        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);

        // Extra OpenGL states that must be enabled when shaders are enabled.
        if (areOptifineShadersEnabled) {
            GL11.glEnableClientState(32885);
            GL20.glEnableVertexAttribArray(11);
            GL20.glEnableVertexAttribArray(12);
            GL20.glEnableVertexAttribArray(10);
        }

        GlStateManager.pushMatrix();
        vertexBuffer.bindBuffer();

        // Even more OpenGL states that must be enabled when shaders are enabled.
        if (areOptifineShadersEnabled) {
            int vertexSizeI = 14;
            GL11.glVertexPointer(3, 5126, 56, 0L);
            GL11.glColorPointer(4, 5121, 56, 12L);
            GL11.glTexCoordPointer(2, 5126, 56, 16L);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glTexCoordPointer(2, 5122, 56, 24L);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glNormalPointer(5120, 56, 28L);
            GL20.glVertexAttribPointer(11, 2, 5126, false, 56, 32L);
            GL20.glVertexAttribPointer(12, 4, 5122, false, 56, 40L);
            GL20.glVertexAttribPointer(10, 3, 5122, false, 56, 48L);
        } else {

            GlStateManager.glVertexPointer(3, 5126, 28, 0);
            GlStateManager.glColorPointer(4, 5121, 28, 12);
            GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        }

        vertexBuffer.drawArrays(7);
        GlStateManager.popMatrix();
        vertexBuffer.unbindBuffer();
        GlStateManager.resetColor();

        for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements()) {
            VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement
                .getUsage();
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

        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);

        // Finally disable some of those extra OpenGL states that were be enabled due to shaders.
        if (areOptifineShadersEnabled) {
            GL11.glDisableClientState(32885);
            GL20.glDisableVertexAttribArray(11);
            GL20.glDisableVertexAttribArray(12);
            GL20.glDisableVertexAttribArray(10);
        }

        GlStateManager.resetColor();
        GlStateManager.popMatrix();
    }

    private static void renderBlockModelHighQuality(Tessellator tessellator, World world,
        IBlockState blockstateToRender, int brightness) {
        BufferBuilder.State vertexData = blockstateToVertexData.get(blockstateToRender);

        if (vertexData == null) {
            generateRenderDataFor(world, blockstateToRender);
            vertexData = blockstateToVertexData.get(blockstateToRender);
        }
        renderVertexState(vertexData, tessellator, brightness);
    }

    private static void renderVertexState(BufferBuilder.State data, Tessellator tessellator,
        int brightness) {
        GL11.glPushMatrix();
        tessellator.getBuffer().begin(7, DefaultVertexFormats.BLOCK);

        tessellator.getBuffer().setVertexState(data);
        int j = tessellator.getBuffer().vertexFormat.getSize() >> 2;
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
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft()
            .getBlockRendererDispatcher();
        IBakedModel modelFromState = blockrendererdispatcher.getModelForState(state);
        blockrendererdispatcher.getBlockModelRenderer()
            .renderModel(Minecraft.getMinecraft().world, modelFromState,
                Blocks.AIR.getDefaultState(), offsetPos, VERTEX_BUILDER, false, 0);
        BufferBuilder.State toReturn = VERTEX_BUILDER.getVertexState();
        VERTEX_BUILDER.finishDrawing();
        VERTEX_BUILDER.reset();
        blockstateToVertexData.put(state, toReturn);
    }

}
