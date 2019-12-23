package org.valkyrienskies.mod.client.render;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.proxy.ClientProxy;
import valkyrienwarfare.api.TransformType;

/**
 * Object owned by each physObject responsible for handling all rendering operations
 *
 * @author thebest108
 */
@ParametersAreNonnullByDefault
public class PhysObjectRenderManager {

    // This pos is used to prevent Z-Buffer Errors D:
    // It's actual value is completely irrelevant as long as it's close to the
    // Ship's centerBlockPos
    public final BlockPos offsetPos;
    private final PhysicsObject parent;
    private final PhysRenderChunk[][] renderChunks;

    public PhysObjectRenderManager(PhysicsObject toRender, BlockPos offsetPos) {
        this.parent = toRender;
        this.offsetPos = offsetPos;
        this.renderChunks = new PhysRenderChunk[parent.getOwnedChunks().getChunkLengthX()][parent
                .getOwnedChunks()
                .getChunkLengthZ()];
        for (int xChunk = 0; xChunk < parent.getOwnedChunks().getChunkLengthX(); xChunk++) {
            for (int zChunk = 0; zChunk < parent.getOwnedChunks().getChunkLengthZ(); zChunk++) {
                renderChunks[xChunk][zChunk] = new PhysRenderChunk(parent, parent
                        .getChunkAt(xChunk + parent.getOwnedChunks().minX(),
                                zChunk + parent.getOwnedChunks().minZ()));
            }
        }
    }

    public void renderBlockLayer(BlockRenderLayer layerToRender, double partialTicks, int pass) {
        GL11.glPushMatrix();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        // int i = parent.wrapper.getBrightnessForRender((float) partialTicks);

        // int j = i % 65536;
        // int k = i / 65536;
        // OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)
        // j, (float) k);
        // GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        applyRenderTransform(partialTicks);
        for (PhysRenderChunk[] chunkArray : renderChunks) {
            for (PhysRenderChunk renderChunk : chunkArray) {
                renderChunk.renderBlockLayer(layerToRender, partialTicks, pass);
            }
        }

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GL11.glPopMatrix();
    }

    public void killRenderers() {
        if (renderChunks != null) {
            for (PhysRenderChunk[] chunks : renderChunks) {
                for (PhysRenderChunk chunk : chunks) {
                    chunk.killRenderChunk();
                }
            }
        }
    }

    public void updateRange(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
        boolean updateImmediately) {
        if (renderChunks == null || parent == null || parent.getOwnedChunks() == null) {
            return;
        }

        int size = (maxX + 1 - minX) * (maxZ + 1 - minZ) * (maxY + 1 - minY);

        if (size > 65535) {
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
                // TODO: Fix this render bug
                try {
                    if (chunkX >= parent.getOwnedChunks().minX() && chunkZ >= parent
                        .getOwnedChunks().minZ()
                        && chunkX - parent.getOwnedChunks().minX() < renderChunks.length
                        && chunkZ - parent.getOwnedChunks().minZ() < renderChunks[0].length) {
                        PhysRenderChunk renderChunk = renderChunks[chunkX - parent.getOwnedChunks()
                            .minX()][chunkZ
                            - parent.getOwnedChunks().minZ()];
                        if (renderChunk != null) {
                            renderChunk.updateLayers(minBlockArrayY, maxBlockArrayY);
                        } else {
                            System.err
                                .println("SHIP RENDER CHUNK CAME OUT NULL! THIS IS VERY WRONG!!");
                        }
                    } else {
                        // ValkyrienSkiesMod.VSLogger.info("updateRange Just attempted to update
                        // blocks outside of a Ship's block Range. ANY ERRORS PAST THIS ARE LIKELY
                        // RELATED!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean shouldRender() {
        ICamera camera = ClientProxy.lastCamera;
        return true; // camera == null || camera.isBoundingBoxInFrustum(parent.getShipBoundingBox());
    }

    public void applyRenderTransform(double partialTicks) {
        Vector centerOfRotation = parent.getCenterCoord();

        double p0 = Minecraft.getMinecraft().player.lastTickPosX
            + (Minecraft.getMinecraft().player.posX - Minecraft.getMinecraft().player.lastTickPosX)
            * partialTicks;
        double p1 = Minecraft.getMinecraft().player.lastTickPosY
            + (Minecraft.getMinecraft().player.posY - Minecraft.getMinecraft().player.lastTickPosY)
            * partialTicks;
        double p2 = Minecraft.getMinecraft().player.lastTickPosZ
            + (Minecraft.getMinecraft().player.posZ - Minecraft.getMinecraft().player.lastTickPosZ)
            * partialTicks;

        ShipTransform renderTransform = parent.getShipTransformationManager().getRenderTransform();

        Vector renderPos = new Vector(centerOfRotation);
        renderTransform.transform(renderPos, TransformType.SUBSPACE_TO_GLOBAL);

        double moddedX = renderPos.x;
        double moddedY = renderPos.y;
        double moddedZ = renderPos.z;

        Quaterniondc quaterniondc = renderTransform.rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        Vector3dc angles = quaterniondc.getEulerAnglesXYZ(new Vector3d());

        double moddedPitch = Math.toDegrees(angles.x());
        double moddedYaw = Math.toDegrees(angles.y());
        double moddedRoll = Math.toDegrees(angles.z());
        // Offset pos is used to prevent floating point errors when rendering stuff thats very far away.
        double offsetX = offsetPos.getX() - centerOfRotation.x;
        double offsetY = offsetPos.getY() - centerOfRotation.y;
        double offsetZ = offsetPos.getZ() - centerOfRotation.z;

        GlStateManager.translate(-p0 + moddedX, -p1 + moddedY, -p2 + moddedZ);
        GL11.glRotated(moddedPitch, 1D, 0, 0);
        GL11.glRotated(moddedYaw, 0, 1D, 0);
        GL11.glRotated(moddedRoll, 0, 0, 1D);
        GL11.glTranslated(offsetX, offsetY, offsetZ);
    }

    public void inverseTransform(double partialTicks) {
        Vector centerOfRotation = parent.getCenterCoord();

        double p0 = Minecraft.getMinecraft().player.lastTickPosX
            + (Minecraft.getMinecraft().player.posX - Minecraft.getMinecraft().player.lastTickPosX)
            * partialTicks;
        double p1 = Minecraft.getMinecraft().player.lastTickPosY
            + (Minecraft.getMinecraft().player.posY - Minecraft.getMinecraft().player.lastTickPosY)
            * partialTicks;
        double p2 = Minecraft.getMinecraft().player.lastTickPosZ
            + (Minecraft.getMinecraft().player.posZ - Minecraft.getMinecraft().player.lastTickPosZ)
            * partialTicks;

        ShipTransform renderTransform = parent.getShipTransformationManager().getRenderTransform();

        Vector renderPos = new Vector(centerOfRotation);
        renderTransform.transform(renderPos, TransformType.SUBSPACE_TO_GLOBAL);

        double moddedX = renderPos.x;
        double moddedY = renderPos.y;
        double moddedZ = renderPos.z;

        Quaterniondc quaterniondc = renderTransform.rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        Vector3dc angles = quaterniondc.getEulerAnglesXYZ(new Vector3d());

        double moddedPitch = Math.toDegrees(angles.x());
        double moddedYaw = Math.toDegrees(angles.y());
        double moddedRoll = Math.toDegrees(angles.z());

        double offsetX = offsetPos.getX() - centerOfRotation.x;
        double offsetY = offsetPos.getY() - centerOfRotation.y;
        double offsetZ = offsetPos.getZ() - centerOfRotation.z;

        GL11.glTranslated(-offsetX, -offsetY, -offsetZ);
        GL11.glRotated(-moddedRoll, 0, 0, 1D);
        GL11.glRotated(-moddedYaw, 0, 1D, 0);
        GL11.glRotated(-moddedPitch, 1D, 0, 0);
        GlStateManager.translate(p0 - moddedX, p1 - moddedY, p2 - moddedZ);
    }

    /**
     * @see #renderDebugInfo(double, double, double)
     */
    public void renderDebugInfo(Vector3dc offset) {
        this.renderDebugInfo(offset.x(), offset.y(), offset.z());
    }

    /**
     * Renders bounding boxes for this ship's AABB and center of mass relative to the offset
     * (probably the negative player's position). Used in F3+D
     */
    public void renderDebugInfo(double offsetX, double offsetY, double offsetZ) {
        GlStateManager.pushMatrix();

        AxisAlignedBB shipBB = parent.getShipBB().offset(offsetX, offsetY, offsetZ);
        ShipTransform renderTransform = parent.getShipTransformationManager().getRenderTransform();

        AxisAlignedBB centerOfMassBB = new AxisAlignedBB(renderTransform.getShipPositionVec3d(), renderTransform.getShipPositionVec3d())
            .grow(.1).offset(offsetX, offsetY, offsetZ);

        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();

        // Draw the bounding box for the ship.
        RenderGlobal.drawSelectionBoundingBox(shipBB, 1.0F, 1.0F, 1.0F, 1.0F);

        // Draw the center of mass bounding box.
        GlStateManager.disableDepth();
        RenderGlobal.drawSelectionBoundingBox(centerOfMassBB, 0, 0, 1.0F, 1.0F);
        GlStateManager.enableDepth();

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);

        // Draw a text box that shows the numerical value of the center of mass.
        String centerOfMassStr = "Center of Mass: " + renderTransform.getCenterCoord().toString();
        renderTextBox(centerOfMassStr, renderTransform.getPosX(), renderTransform.getPosY() + .5,
            renderTransform.getPosZ(), offsetX, offsetY, offsetZ);

        String massStr = String.format("Mass: %.2f", parent.getData().getInertiaData().getGameTickMass());

        renderTextBox(massStr, renderTransform.getPosX(), renderTransform.getPosY() + 1,
            renderTransform.getPosZ(), offsetX, offsetY, offsetZ);

        GlStateManager.popMatrix();
    }

    /**
     * Renders a text box in the world at the position xyz, with the render offset provided. (Render offset not included
     * in text box distance check).
     */
    private void renderTextBox(String str, double posX, double posY, double posZ, double offsetX, double offsetY, double offsetZ) {
        Minecraft mc = Minecraft.getMinecraft();
        final double maxDistance = 64;
        double d0 = new Vec3d(posX, posY, posZ)
            .squareDistanceTo(mc.getRenderManager().renderViewEntity.getPositionVector());

        if (d0 <= maxDistance * maxDistance) {
            float playerYaw = mc.getRenderManager().playerViewY;
            float playerPitch = mc.getRenderManager().playerViewX;
            boolean isThirdPersonFrontal = mc.getRenderManager().options.thirdPersonView == 2;
            EntityRenderer.drawNameplate(mc.getRenderManager().getFontRenderer(), str,
                (float) (posX + offsetX), (float) (posY + offsetY), (float) (posZ + offsetZ),
                0, playerYaw, playerPitch, isThirdPersonFrontal, false);
        }
    }

}
