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

package org.valkyrienskies.mod.common.physics.management;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.math.RotationMatrices;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.multithreaded.PhysicsShipTransform;
import org.valkyrienskies.mod.common.network.PhysWrapperPositionMessage;
import valkyrienwarfare.api.TransformType;

/**
 * Stores various coordinates and transforms for the ship.
 *
 * @author thebest108
 */
public class ShipTransformationManager {

    // A transformation that does no rotation, and does no translation.
    public static final ShipTransform ZERO_TRANSFORM = new ShipTransform();
    // A buffer to hold ship transform data sent from server to the client.
    public final ShipTransformationBuffer serverBuffer;
    private final PhysicsObject parent;
    public Vector[] normals;
    private ShipTransform currentTickTransform;
    private ShipTransform renderTransform;
    private ShipTransform prevTickTransform;
    // Used exclusively by the physics engine; should never even be used by the
    // client.
    private ShipTransform currentPhysicsTransform;
    private ShipTransform prevPhysicsTransform;

    public ShipTransformationManager(PhysicsObject parent) {
        this.parent = parent;
        this.currentTickTransform = null;
        this.renderTransform = null;
        this.prevTickTransform = null;
        this.currentPhysicsTransform = null;
        this.prevPhysicsTransform = null;
        this.normals = null;
        this.serverBuffer = new ShipTransformationBuffer();
    }

    /**
     * Polls position and rotation data from the parent ship, and creates a new
     * current transform made from this data.
     */
    public void updateCurrentTickTransform() {
        double[] lToWTransform = RotationMatrices.getTranslationMatrix(parent.getWrapperEntity().posX, parent.getWrapperEntity().posY,
                parent.getWrapperEntity().posZ);
        lToWTransform = RotationMatrices.rotateAndTranslate(lToWTransform, parent.getWrapperEntity().getPitch(),
                parent.getWrapperEntity().getYaw(), parent.getWrapperEntity().getRoll(), parent.getCenterCoord());
        setCurrentTickTransform(new ShipTransform(lToWTransform));
    }

    public void updateRenderTransform(double x, double y, double z, double pitch, double yaw, double roll) {
        double[] RlToWTransform = RotationMatrices.getTranslationMatrix(x, y, z);
        RlToWTransform = RotationMatrices.rotateAndTranslate(RlToWTransform, pitch, yaw, roll, parent.getCenterCoord());
        setRenderTransform(new ShipTransform(RlToWTransform));
    }

    /**
     * Sets the previous transform to the current transform.
     */
    public void updatePrevTickTransform() {
        // Transformation objects are immutable, so this is 100% safe!
        setPrevTickTransform(getCurrentTickTransform());
    }

    /**
     * Updates all the transformations, only updates the AABB if passed true.
     *
     * @param updateParentAABB
     */
    @Deprecated
    public void updateAllTransforms(boolean updatePhysicsTransform, boolean updateParentAABB, boolean updatePassengers) {
        // The client should never be updating the AABB on its own.
        if (parent.getWorld().isRemote) {
            updateParentAABB = false;
        }
        forceShipIntoWorldBorder();
        updateCurrentTickTransform();
        if (prevTickTransform == null) {
            prevTickTransform = currentTickTransform;
        }
        if (updatePhysicsTransform) {
            // This should only be called once when the ship finally loads from nbt.
            parent.getPhysicsProcessor()
                    .generatePhysicsTransform();
            prevPhysicsTransform = currentPhysicsTransform;
        }
        if (updateParentAABB) {
            updateParentAABB();
        }
        updateParentNormals();
        if (updatePassengers) {
            updatePassengerPositions();
        }
    }

    /**
     * Keeps the Ship in the world border
     */
    private void forceShipIntoWorldBorder() {
        WorldBorder border = parent.getWorld().getWorldBorder();
        AxisAlignedBB shipBB = parent.getShipBoundingBox();

        if (shipBB.maxX > border.maxX()) {
            parent.getWrapperEntity().posX += border.maxX() - shipBB.maxX;
        }
        if (shipBB.minX < border.minX()) {
            parent.getWrapperEntity().posX += border.minX() - shipBB.minX;
        }
        if (shipBB.maxZ > border.maxZ()) {
            parent.getWrapperEntity().posZ += border.maxZ() - shipBB.maxZ;
        }
        if (shipBB.minZ < border.minZ()) {
            parent.getWrapperEntity().posZ += border.minZ() - shipBB.minZ;
        }
    }

    public void updatePassengerPositions() {
        for (Entity entity : parent.getWrapperEntity().riddenByEntities) {
            parent.getWrapperEntity().updatePassenger(entity);
        }
    }

    public void sendPositionToPlayers(int positionTickID) {
        PhysWrapperPositionMessage posMessage = null;
        if (getCurrentPhysicsTransform() != ZERO_TRANSFORM) {
            posMessage = new PhysWrapperPositionMessage((PhysicsShipTransform) getCurrentPhysicsTransform(),
                    parent.getWrapperEntity().getEntityId(), positionTickID);
        } else {
            posMessage = new PhysWrapperPositionMessage(parent.getWrapperEntity(), positionTickID);
        }

        // Do a standard loop here to avoid a concurrentModificationException. A standard for each loop could cause a crash.
        for (int i = 0; i < parent.getWatchingPlayers().size(); i++) {
            EntityPlayerMP player = parent.getWatchingPlayers().get(i);
            if (player != null) {
                ValkyrienSkiesMod.physWrapperNetwork.sendTo(posMessage, player);
            }
        }
    }

    public void updateParentNormals() {
        normals = new Vector[15];
        // Used to generate Normals for the Axis Aligned World
        Vector[] alignedNorms = Vector.generateAxisAlignedNorms();
        Vector[] rotatedNorms = generateRotationNormals();
        for (int i = 0; i < 6; i++) {
            Vector currentNorm;
            if (i < 3) {
                currentNorm = alignedNorms[i];
            } else {
                currentNorm = rotatedNorms[i - 3];
            }
            normals[i] = currentNorm;
        }
        int cont = 6;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Vector norm = normals[i].crossAndUnit(normals[j + 3]);
                normals[cont] = norm;
                cont++;
            }
        }
        for (int i = 0; i < normals.length; i++) {
            if (normals[i].isZero()) {
                normals[i] = new Vector(0.0D, 1.0D, 0.0D);
            }
        }
        normals[0] = new Vector(1.0D, 0.0D, 0.0D);
        normals[1] = new Vector(0.0D, 1.0D, 0.0D);
        normals[2] = new Vector(0.0D, 0.0D, 1.0D);
    }

    public Vector[] generateRotationNormals() {
        Vector[] norms = Vector.generateAxisAlignedNorms();
        for (int i = 0; i < 3; i++) {
            getCurrentTickTransform().rotate(norms[i], TransformType.SUBSPACE_TO_GLOBAL);
        }
        return norms;
    }

    public Vector[] getSeperatingAxisWithShip(PhysicsObject other) {
        // Note: This Vector array still contains potential 0 vectors, those are removed
        // later
        Vector[] normals = new Vector[15];
        Vector[] otherNorms = other.getShipTransformationManager().normals;
        Vector[] rotatedNorms = normals;
        for (int i = 0; i < 6; i++) {
            if (i < 3) {
                normals[i] = otherNorms[i];
            } else {
                normals[i] = rotatedNorms[i - 3];
            }
        }
        int cont = 6;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Vector norm = normals[i].crossAndUnit(normals[j + 3]);
                if (!norm.isZero()) {
                    normals[cont] = norm;
                } else {
                    normals[cont] = normals[1];
                }
                cont++;
            }
        }
        return normals;
    }

    // TODO: Use Octrees to optimize this, or more preferably QuickHull3D.
    public void updateParentAABB() {
        // Don't run otherwise make the game freeze
        if (parent.getBlockPositionsGameTick().isEmpty()) {
            return;
        }
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final double[] MDouble = getCurrentPhysicsTransform().getInternalMatrix(TransformType.SUBSPACE_TO_GLOBAL);
        final float[] M = new float[MDouble.length];
        for (int i = 0; i < MDouble.length; i++) {
            M[i] = (float) MDouble[i];
        }

        float minX, minY, minZ, maxX, maxY, maxZ;
        minX = minY = minZ = Float.MAX_VALUE;
        maxX = maxY = maxZ = -Float.MAX_VALUE;

        // We loop through this int list instead of a blockpos list because they fit much better in the cache,
        for (int i = parent.getBlockPositionsGameTick().size() - 1; i >= 0; i--) {
            int blockPos = parent.getBlockPositionsGameTick().get(i);
            parent.setBlockPosFromIntRelToShop(blockPos, pos);

            float x = pos.getX() + .5f;
            float y = pos.getY() + .5f;
            float z = pos.getZ() + .5f;

            float newX = x * M[0] + y * M[1] + z * M[2] + M[3];
            float newY = x * M[4] + y * M[5] + z * M[6] + M[7];
            float newZ = x * M[8] + y * M[9] + z * M[10] + M[11];

            minX = Math.min(newX - .5f, minX);
            maxX = Math.max(newX + .5f, maxX);
            minY = Math.min(newY - .5f, minY);
            maxY = Math.max(newY + .5f, maxY);
            minZ = Math.min(newZ - .5f, minZ);
            maxZ = Math.max(newZ + .5f, maxZ);
        }
        AxisAlignedBB newBB = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).grow(3D);
        // Just a quick sanity check
        if (newBB.getAverageEdgeLength() < 1000000D) {
            parent.setShipBoundingBox(newBB);
        } else {
            // throw new IllegalStateException("Unexpectedly large shipBB!!!");

        }
    }

    /**
     * Transforms a vector from global coordinates to local coordinates, using the
     * getCurrentTickTransform()
     *
     * @param inGlobal
     */
    public void fromGlobalToLocal(Vector inGlobal) {
        getCurrentTickTransform().transform(inGlobal, TransformType.GLOBAL_TO_SUBSPACE);
    }

    /**
     * Transforms a vector from local coordinates to global coordinates, using the
     * getCurrentTickTransform()
     *
     * @param inLocal
     */
    public void fromLocalToGlobal(Vector inLocal) {
        getCurrentTickTransform().transform(inLocal, TransformType.SUBSPACE_TO_GLOBAL);
    }

    /**
     * @return the current transformation being used this tick.
     */
    public ShipTransform getCurrentTickTransform() {
        return currentTickTransform;
    }

    /**
     * @param currentTransform the currentTransform to set
     */
    @Deprecated
    private void setCurrentTickTransform(ShipTransform currentTransform) {
        this.currentTickTransform = currentTransform;
    }

    /**
     * @return the renderTransform
     */
    public ShipTransform getRenderTransform() {
        if (!this.parent.getWorld().isRemote || renderTransform == null) {
            return currentTickTransform;
        }
        return renderTransform;
    }

    /**
     * @param renderTransform the renderTransform to set
     */
    @Deprecated
    private void setRenderTransform(ShipTransform renderTransform) {
        this.renderTransform = renderTransform;
    }

    /**
     * @return the prevTransform
     */
    public ShipTransform getPrevTickTransform() {
        return prevTickTransform;
    }

    /**
     * @param prevTransform the prevTransform to set
     */
    private void setPrevTickTransform(ShipTransform prevTransform) {
        this.prevTickTransform = prevTransform;
    }

    /**
     * Returns the transformation data used for physics processing.
     *
     * @return the physics transform
     */
    public ShipTransform getCurrentPhysicsTransform() {
        return currentPhysicsTransform;
    }

    /**
     * Sets the physics transform to the given input.
     *
     * @param
     */
    public void setCurrentPhysicsTransform(ShipTransform currentPhysicsTransform) {
        this.currentPhysicsTransform = currentPhysicsTransform;
    }

    public ShipTransform getPrevPhysicsTransform() {
        return prevPhysicsTransform;
    }

    public void updatePreviousPhysicsTransform() {
        this.prevPhysicsTransform = currentPhysicsTransform;
    }

}
