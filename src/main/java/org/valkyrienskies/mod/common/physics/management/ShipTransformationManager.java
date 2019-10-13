/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
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
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Quaternion;
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
     * Polls position and rotation data from the parent ship, and creates a new current transform
     * made from this data.
     */
    public void updateCurrentTickTransform() {
        PhysicsWrapperEntity wrapperEntity = parent.wrapperEntity();
        ShipTransform newTickTransform = new ShipTransform(wrapperEntity.posX, wrapperEntity.posY,
            wrapperEntity.posZ, wrapperEntity.getPitch(), wrapperEntity.getYaw(),
            wrapperEntity.getRoll(), parent.centerCoord());
        setCurrentTickTransform(newTickTransform);
    }

    /**
     * Updates all the transformations, only updates the AABB if passed true.
     *
     * @param updateParentAABB
     */
    @Deprecated
    public void updateAllTransforms(boolean updatePhysicsTransform, boolean updateParentAABB,
        boolean updatePassengers) {
        prevTickTransform = currentTickTransform;
        // The client should never be updating the AABB on its own.
        if (parent.world().isRemote) {
            updateParentAABB = false;
        }
        forceShipIntoWorldBorder();
        updateCurrentTickTransform();
        if (prevTickTransform == null) {
            prevTickTransform = currentTickTransform;
        }
        if (updatePhysicsTransform) {
            // This should only be called once when the ship finally loads from nbt.
            parent.physicsProcessor()
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
        WorldBorder border = parent.world().getWorldBorder();
        AxisAlignedBB shipBB = parent.shipBoundingBox();

        if (shipBB.maxX > border.maxX()) {
            parent.wrapperEntity().posX += border.maxX() - shipBB.maxX;
        }
        if (shipBB.minX < border.minX()) {
            parent.wrapperEntity().posX += border.minX() - shipBB.minX;
        }
        if (shipBB.maxZ > border.maxZ()) {
            parent.wrapperEntity().posZ += border.maxZ() - shipBB.maxZ;
        }
        if (shipBB.minZ < border.minZ()) {
            parent.wrapperEntity().posZ += border.minZ() - shipBB.minZ;
        }
    }

    public void updatePassengerPositions() {
        for (Entity entity : parent.wrapperEntity().riddenByEntities) {
            parent.wrapperEntity().updatePassenger(entity);
        }
    }

    public void sendPositionToPlayers(int positionTickID) {
        PhysWrapperPositionMessage posMessage = null;
        if (getCurrentPhysicsTransform() != ZERO_TRANSFORM) {
            posMessage = new PhysWrapperPositionMessage(
                (PhysicsShipTransform) getCurrentPhysicsTransform(),
                parent.wrapperEntity().getEntityId(), positionTickID);
        } else {
            posMessage = new PhysWrapperPositionMessage(parent.wrapperEntity(), positionTickID);
        }

        // Do a standard loop here to avoid a concurrentModificationException. A standard for each loop could cause a crash.
        for (int i = 0; i < parent.watchingPlayers().size(); i++) {
            EntityPlayerMP player = parent.watchingPlayers().get(i);
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
        Vector[] otherNorms = other.shipTransformationManager().normals;
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
        if (parent.blockPositionsGameTick().isEmpty()) {
            return;
        }
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // TODO: This is bad, but its fast. Actually this whole algorithm for calculating an AABB
        //  for a ship is already bad, so we're not really making it much worse.
        final float[] rawMatrix = getCurrentPhysicsTransform()
            .generateFastRawTransformMatrix(TransformType.SUBSPACE_TO_GLOBAL);

        float minX, minY, minZ, maxX, maxY, maxZ;
        minX = minY = minZ = Float.MAX_VALUE;
        maxX = maxY = maxZ = -Float.MAX_VALUE;

        // We loop through this int list instead of a blockpos list because they fit much better in
        // the cache,
        for (int i = parent.blockPositionsGameTick().size() - 1; i >= 0; i--) {
            // Don't bother doing any bounds checking.
            int blockPos = parent.blockPositionsGameTick().getQuick(i);
            parent.setBlockPosFromIntRelToShop(blockPos, pos);

            float x = pos.getX() + .5f;
            float y = pos.getY() + .5f;
            float z = pos.getZ() + .5f;

            float newX = x * rawMatrix[0] + y * rawMatrix[1] + z * rawMatrix[2] + rawMatrix[3];
            float newY = x * rawMatrix[4] + y * rawMatrix[5] + z * rawMatrix[6] + rawMatrix[7];
            float newZ = x * rawMatrix[8] + y * rawMatrix[9] + z * rawMatrix[10] + rawMatrix[11];

            minX = Math.min(newX, minX);
            maxX = Math.max(newX, maxX);
            minY = Math.min(newY, minY);
            maxY = Math.max(newY, maxY);
            minZ = Math.min(newZ, minZ);
            maxZ = Math.max(newZ, maxZ);
        }
        AxisAlignedBB newBB = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).grow(3D);
        // Just a quick sanity check
        if (newBB.getAverageEdgeLength() < 1000000D) {
            parent.shipBoundingBox(newBB);
        } else {
            throw new IllegalStateException("Unexpectedly large ship bounding box!\n" + newBB);
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
        if (!this.parent.world().isRemote || renderTransform == null) {
            return currentTickTransform;
        }
        return renderTransform;
    }

    /**
     * @return the prevTransform
     */
    public ShipTransform getPrevTickTransform() {
        return prevTickTransform;
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

    public void updateRenderTransform(double partialTick) {
        if (partialTick == 0) {
            renderTransform = prevTickTransform;
            return;
        } else if (partialTick == 1) {
            renderTransform = currentTickTransform;
            return;
        }
        ShipTransform prev = prevTickTransform;
        ShipTransform cur = currentTickTransform;
        Vector shipCenter = parent.centerCoord();

        Vector prevPos = new Vector(shipCenter);
        Vector curPos = new Vector(shipCenter);
        prev.transform(prevPos, TransformType.SUBSPACE_TO_GLOBAL);
        cur.transform(curPos, TransformType.SUBSPACE_TO_GLOBAL);
        Vector deltaPos = prevPos.getSubtraction(curPos);
        deltaPos.multiply(partialTick);
        Vector partialPos = new Vector(prevPos);
        partialPos.add(deltaPos); // Now partialPos is complete

        Quaternion prevRot = prev.createRotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        Quaternion curRot = cur.createRotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        Quaternion partialRot = Quaternion.slerpInterpolate(prevRot, curRot, partialTick);
        double[] partialAngles = partialRot
            .toRadians(); // Now partial angles {pitch, yaw, roll} are complete.
        // Put it all together to get the render transform.
        renderTransform = new ShipTransform(partialPos.X, partialPos.Y,
            partialPos.Z, Math.toDegrees(partialAngles[0]), Math.toDegrees(partialAngles[1]),
            Math.toDegrees(partialAngles[2]),
            parent.centerCoord());
    }

}
