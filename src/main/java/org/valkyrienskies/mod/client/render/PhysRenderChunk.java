package org.valkyrienskies.mod.client.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.mod.common.collision.Polygon;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import valkyrienwarfare.api.TransformType;

public class PhysRenderChunk {

    public IVSRenderChunk[] renderChunks = new IVSRenderChunk[16];
    public PhysicsObject toRender;
    public Chunk chunk;

    public PhysRenderChunk(PhysicsObject toRender, Chunk chunk) {
        this.toRender = toRender;
        this.chunk = chunk;
        for (int i = 0; i < 16; i++) {
            ExtendedBlockStorage storage = this.chunk.storageArrays[i];
            if (storage != null) {
                IVSRenderChunk renderChunk;
                // Support old graphics cards that can't use VBOs.
                if (OpenGlHelper.useVbo()) {
                    renderChunk = new RenderLayerVBO(this.chunk, i * 16, i * 16 + 15, this);
                } else {
                    renderChunk = new RenderLayerDisplayList(this.chunk, i * 16, i * 16 + 15, this);
                }
                renderChunks[i] = renderChunk;
            }
        }
    }

    public void renderBlockLayer(BlockRenderLayer layerToRender, double partialTicks, int pass, ICamera iCamera) {
        for (int i = 0; i < 16; i++) {
            IVSRenderChunk renderChunk = renderChunks[i];
            if (renderChunk != null) {
                AxisAlignedBB renderChunkBB = new AxisAlignedBB(chunk.x << 4, renderChunk.minY(), chunk.z << 4, (chunk.x << 4) + 16, renderChunk.minY() + 16, (chunk.z << 4) + 16);
                Polygon polygon = new Polygon(renderChunkBB, toRender.getShipTransformationManager().getRenderTransform(), TransformType.SUBSPACE_TO_GLOBAL);
                AxisAlignedBB inWorldBB = polygon.getEnclosedAABB();

                // Only render chunks that can be shown by the camera.
                if (iCamera.isBoundingBoxInFrustum(inWorldBB)) {
                    renderChunk.renderBlockLayer(layerToRender, partialTicks, pass);
                }
            }
        }
    }

    public void updateLayers(int minLayer, int maxLayer) {
        for (int layerY = minLayer; layerY <= maxLayer; layerY++) {
            IVSRenderChunk renderChunk = renderChunks[layerY];
            if (renderChunk != null) {
                renderChunk.markDirty();
            } else {
                IVSRenderChunk renderLayer;
                if (OpenGlHelper.useVbo()) {
                    renderLayer= new RenderLayerVBO(this.chunk, layerY * 16, layerY * 16 + 15, this);
                } else {
                    renderLayer = new RenderLayerDisplayList(this.chunk, layerY * 16, layerY * 16 + 15, this);
                }
                renderChunks[layerY] = renderLayer;
            }
        }
    }

    void killRenderChunk() {
        for (int i = 0; i < 16; i++) {
            IVSRenderChunk renderChunk = renderChunks[i];
            if (renderChunk != null) {
                renderChunk.deleteRenderChunk();
            }
        }
    }

    private interface IVSRenderChunk {
        void renderBlockLayer(BlockRenderLayer layerToRender, double partialTicks, int pass);

        void markDirty();

        void deleteRenderChunk();

        int minY();

        int maxY();
    }

    private class RenderLayerVBO implements IVSRenderChunk {

        Chunk chunkToRender;
        int yMin, yMax;
        VertexBuffer cutoutBuffer, cutoutMippedBuffer, solidBuffer, translucentBuffer;
        PhysRenderChunk parent;
        boolean needsCutoutUpdate, needsCutoutMippedUpdate, needsSolidUpdate, needsTranslucentUpdate;
        List<TileEntity> renderTiles = new ArrayList<>();

        RenderLayerVBO(Chunk chunk, int yMin, int yMax, PhysRenderChunk parent) {
            chunkToRender = chunk;
            this.yMin = yMin;
            this.yMax = yMax;
            this.parent = parent;
            markDirty();
            cutoutBuffer = null;
            cutoutMippedBuffer = null;
            solidBuffer = null;
            translucentBuffer = null;
        }

        public int minY() {
            return yMin;
        }

        public int maxY() {
            return yMax;
        }

        public void markDirty() {
            needsCutoutUpdate = true;
            needsCutoutMippedUpdate = true;
            needsSolidUpdate = true;
            needsTranslucentUpdate = true;
            updateRenderTileEntities();
        }

        // TODO: There's probably a faster way of doing this.
        public void updateRenderTileEntities() {
            ITileEntitiesToRenderProvider provider = (ITileEntitiesToRenderProvider) chunkToRender;
            List<TileEntity> updatedRenderTiles = provider.getTileEntitiesToRender(yMin >> 4);
            if (updatedRenderTiles != null) {
                Minecraft.getMinecraft().renderGlobal
                    .updateTileEntities(renderTiles, updatedRenderTiles);
                renderTiles = new ArrayList<>(updatedRenderTiles);
            }
        }

        public void deleteRenderChunk() {
            clearRenderLists();
            Minecraft.getMinecraft().renderGlobal.updateTileEntities(renderTiles, new ArrayList<>());
            renderTiles.clear();
        }

        private void clearRenderLists() {
            if (cutoutBuffer != null)
                cutoutBuffer.deleteGlBuffers();
            if (cutoutMippedBuffer != null)
                cutoutMippedBuffer.deleteGlBuffers();
            if (solidBuffer != null)
                solidBuffer.deleteGlBuffers();
            if (translucentBuffer != null)
                translucentBuffer.deleteGlBuffers();
        }

        public void renderBlockLayer(BlockRenderLayer layerToRender, double partialTicks, int pass) {
            switch (layerToRender) {
                case CUTOUT:
                    if (needsCutoutUpdate) {
                        updateList(layerToRender);
                    }
                    FastBlockModelRenderer.renderVertexBuffer(cutoutBuffer);
                    break;
                case CUTOUT_MIPPED:
                    if (needsCutoutMippedUpdate) {
                        updateList(layerToRender);
                    }
                    FastBlockModelRenderer.renderVertexBuffer(cutoutMippedBuffer);
                    break;
                case SOLID:
                    if (needsSolidUpdate) {
                        updateList(layerToRender);
                    }
                    FastBlockModelRenderer.renderVertexBuffer(solidBuffer);
                    break;
                case TRANSLUCENT:
                    if (needsTranslucentUpdate) {
                        updateList(layerToRender);
                    }
                    FastBlockModelRenderer.renderVertexBuffer(translucentBuffer);
                    break;
                default:
                    break;
            }
        }

        private void updateList(BlockRenderLayer layerToUpdate) {
            if (parent.toRender.getShipRenderer() == null) {
                return;
            }
            BlockPos offsetPos = parent.toRender.getShipRenderer().offsetPos;
            if (offsetPos == null) {
                return;
            }
            // Tessellator tessellator = Tessellator.getInstance();
            // BufferBuilder worldrenderer = tessellator.getBuffer();
            BufferBuilder vsChunkBuilder = FastBlockModelRenderer.VERTEX_BUILDER;
            vsChunkBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            vsChunkBuilder.setTranslation(-offsetPos.getX(), -offsetPos.getY(), -offsetPos.getZ());

            // The vertex buffer we're going to render this 16x16x16 chunk into
            VertexBuffer renderBuffer;

            switch (layerToUpdate) {
                case CUTOUT:
                    if (cutoutBuffer != null) {
                        cutoutBuffer.deleteGlBuffers();
                    }
                    cutoutBuffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
                    renderBuffer = cutoutBuffer;
                    break;
                case CUTOUT_MIPPED:
                    if (cutoutMippedBuffer != null) {
                        cutoutMippedBuffer.deleteGlBuffers();
                    }
                    cutoutMippedBuffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
                    renderBuffer = cutoutMippedBuffer;
                    break;
                case SOLID:
                    if (solidBuffer != null) {
                        solidBuffer.deleteGlBuffers();
                    }
                    solidBuffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
                    renderBuffer = solidBuffer;
                    break;
                case TRANSLUCENT:
                    if (translucentBuffer != null) {
                        translucentBuffer.deleteGlBuffers();
                    }
                    translucentBuffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
                    renderBuffer = translucentBuffer;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + layerToUpdate);
            }

            // Be careful with render layers
            BlockRenderLayer oldLayer = MinecraftForgeClient.getRenderLayer();
            ForgeHooksClient.setRenderLayer(layerToUpdate);
            MutableBlockPos pos = new MutableBlockPos();
            for (int x = chunkToRender.x * 16; x < chunkToRender.x * 16 + 16; x++) {
                for (int z = chunkToRender.z * 16; z < chunkToRender.z * 16 + 16; z++) {
                    for (int y = yMin; y <= yMax; y++) {
                        pos.setPos(x, y, z);
                        IBlockState iblockstate = chunkToRender.getBlockState(pos);
                        try {
                            if (iblockstate.getBlock()
                                .canRenderInLayer(iblockstate, layerToUpdate)) {
                                Minecraft.getMinecraft().getBlockRendererDispatcher()
                                    .renderBlock(iblockstate, pos, chunkToRender.world, vsChunkBuilder);
                            }
                        } catch (NullPointerException e) {
                            System.out.println("Something was null!");
                        }
                    }
                }
            }

            vsChunkBuilder.finishDrawing();
            renderBuffer.bufferData(vsChunkBuilder.getByteBuffer());
            vsChunkBuilder.reset();

            // Fix the old render layer
            ForgeHooksClient.setRenderLayer(oldLayer);

            vsChunkBuilder.setTranslation(0, 0, 0);

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

    public static class RenderLayerDisplayList implements IVSRenderChunk {

        Chunk chunkToRender;
        int yMin, yMax;
        int glCallListCutout, glCallListCutoutMipped, glCallListSolid, glCallListTranslucent;
        PhysRenderChunk parent;
        boolean needsCutoutUpdate, needsCutoutMippedUpdate, needsSolidUpdate, needsTranslucentUpdate;
        List<TileEntity> renderTiles = new ArrayList<>();

        RenderLayerDisplayList(Chunk chunk, int yMin, int yMax, PhysRenderChunk parent) {
            chunkToRender = chunk;
            this.yMin = yMin;
            this.yMax = yMax;
            this.parent = parent;
            markDirty();
            glCallListCutout = GLAllocation.generateDisplayLists(4);
            glCallListCutoutMipped = glCallListCutout + 1;
            glCallListSolid = glCallListCutout + 2;
            glCallListTranslucent = glCallListCutout + 3;
        }

        public int minY() {
            return yMin;
        }

        public int maxY() {
            return yMax;
        }

        public void markDirty() {
            needsCutoutUpdate = true;
            needsCutoutMippedUpdate = true;
            needsSolidUpdate = true;
            needsTranslucentUpdate = true;
            updateRenderTileEntities();
        }

        // TODO: There's probably a faster way of doing this.
        public void updateRenderTileEntities() {
            ITileEntitiesToRenderProvider provider = (ITileEntitiesToRenderProvider) chunkToRender;
            List<TileEntity> updatedRenderTiles = provider.getTileEntitiesToRender(yMin >> 4);
            if (updatedRenderTiles != null) {
                Minecraft.getMinecraft().renderGlobal
                        .updateTileEntities(renderTiles, updatedRenderTiles);
                renderTiles = new ArrayList<>(updatedRenderTiles);
            }
        }

        public void deleteRenderChunk() {
            clearRenderLists();
            Minecraft.getMinecraft().renderGlobal.updateTileEntities(renderTiles, new ArrayList<>());
            renderTiles.clear();
        }

        private void clearRenderLists() {
            GLAllocation.deleteDisplayLists(glCallListCutout);
            GLAllocation.deleteDisplayLists(glCallListCutoutMipped);
            GLAllocation.deleteDisplayLists(glCallListSolid);
            GLAllocation.deleteDisplayLists(glCallListTranslucent);
        }

        public void renderBlockLayer(BlockRenderLayer layerToRender, double partialTicks,
                                     int pass) {
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

        private void updateList(BlockRenderLayer layerToUpdate) {
            if (parent.toRender.getShipRenderer() == null) {
                return;
            }
            BlockPos offsetPos = parent.toRender.getShipRenderer().offsetPos;
            if (offsetPos == null) {
                return;
            }
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder worldrenderer = tessellator.getBuffer();
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

            BlockRenderLayer oldLayer = MinecraftForgeClient.getRenderLayer();
            ForgeHooksClient.setRenderLayer(layerToUpdate);
            MutableBlockPos pos = new MutableBlockPos();
            for (int x = chunkToRender.x * 16; x < chunkToRender.x * 16 + 16; x++) {
                for (int z = chunkToRender.z * 16; z < chunkToRender.z * 16 + 16; z++) {
                    for (int y = yMin; y <= yMax; y++) {
                        pos.setPos(x, y, z);
                        iblockstate = chunkToRender.getBlockState(pos);
                        try {
                            if (iblockstate.getBlock()
                                    .canRenderInLayer(iblockstate, layerToUpdate)) {
                                Minecraft.getMinecraft().getBlockRendererDispatcher()
                                        .renderBlock(iblockstate, pos, chunkToRender.world,
                                                worldrenderer);
                            }
                        } catch (NullPointerException e) {
                            System.out.println(
                                    "Something was null! LValkyrienSkiesBase/render/PhysRenderChunk#updateList");
                        }
                    }
                }
            }
            tessellator.draw();
            // worldrenderer.finishDrawing();
            ForgeHooksClient.setRenderLayer(oldLayer);
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
