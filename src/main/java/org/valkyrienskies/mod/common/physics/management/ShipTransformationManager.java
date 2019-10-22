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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.border.WorldBorder;
import org.joml.Matrix4dc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.multithreaded.PhysicsShipTransform;
import org.valkyrienskies.mod.common.network.WrapperPositionMessage;
import org.valkyrienskies.mod.common.physics.collision.meshing.IVoxelFieldAABBMaker;
import org.valkyrienskies.mod.common.physics.collision.polygons.Polygon;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import valkyrienwarfare.api.TransformType;

/**
 * Stores various coordinates and transforms for the ship.
 *
 * @author thebest108
 */
public class ShipTransformationManager {

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
        PhysicsWrapperEntity wrapperEntity = parent.getWrapperEntity();
        ShipTransform newTickTransform = new ShipTransform(wrapperEntity.posX, wrapperEntity.posY,
            wrapperEntity.posZ, wrapperEntity.getPitch(), wrapperEntity.getYaw(),
            wrapperEntity.getRoll(), parent.getCenterCoord());
        setCurrentTickTransform(newTickTransform);
    }

    /**
     * Updates all the transformations, only updates the AABB if passed true.
     *
     * @param updateParentAABB
     */
    @Deprecated
    public void updateAllTransforms(boolean updatePhysicsTransform, boolean updateParentAABB) {
        prevTickTransform = currentTickTransform;
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
            parent.getPhysicsCalculations()
                .generatePhysicsTransform();
            prevPhysicsTransform = currentPhysicsTransform;
        }
        if (updateParentAABB) {
            updateParentAABB();
        }
        updateParentNormals();
    }

    /**
     * Keeps the Ship in the world border
     */
    private void forceShipIntoWorldBorder() {
        WorldBorder border = parent.getWorld().getWorldBorder();
        AxisAlignedBB shipBB = parent.getShipBoundingBox();

        ShipTransform transform = parent.getTransform();
        ShipTransform.ShipTransformBuilder builder = transform.toBuilder();

        if (shipBB.maxX > border.maxX()) {
            builder.posX(transform.getPosX() + border.maxX() - shipBB.maxX);
        }
        if (shipBB.minX < border.minX()) {
            builder.posX(transform.getPosX() + border.minX() - shipBB.minX);
        }
        if (shipBB.maxZ > border.maxZ()) {
            builder.posZ(transform.getPosZ() + border.maxZ() - shipBB.maxZ);
        }
        if (shipBB.minZ < border.minZ()) {
            builder.posZ(transform.getPosZ() + border.minZ() - shipBB.minZ);
        }

        ShipTransform newTransform = builder.build();
        parent.updateTransform(newTransform);
    }

    public void sendPositionToPlayers(int positionTickID) {
        WrapperPositionMessage posMessage = null;
        Matrix4dc gts = getCurrentPhysicsTransform().getGlobalToSubspace();
        Matrix4dc stg = getCurrentPhysicsTransform().getSubspaceToGlobal();
        // If it is the identity transform
        if ((gts.properties() & Matrix4dc.PROPERTY_IDENTITY) != 0 &&
            (stg.properties() & Matrix4dc.PROPERTY_IDENTITY) != 0) {
            posMessage = new WrapperPositionMessage(
                (PhysicsShipTransform) getCurrentPhysicsTransform(),
                parent.getWrapperEntity().getEntityId(), positionTickID);
        } else {
            posMessage = new WrapperPositionMessage(parent.getWrapperEntity(), positionTickID);
        }

        // Do a standard loop here to avoid a concurrentModificationException. A standard for each loop could cause a crash.
        for (int i = 0; i < parent.getWatchingPlayers().size(); i++) {
            EntityPlayerMP player = parent.getWatchingPlayers().get(i);
            if (player != null) {
                ValkyrienSkiesMod.physWrapperNetwork.sendTo(posMessage, player);
            }
        }
    }

    private void updateParentNormals() {
        // We edit a local array instead of normals to avoid data races.
        final Vector[] newNormals = new Vector[15];
        // Used to generate Normals for the Axis Aligned World
        final Vector[] alignedNorms = Vector.generateAxisAlignedNorms();
        final Vector[] rotatedNorms = generateRotationNormals();
        for (int i = 0; i < 6; i++) {
            Vector currentNorm;
            if (i < 3) {
                currentNorm = alignedNorms[i];
            } else {
                currentNorm = rotatedNorms[i - 3];
            }
            newNormals[i] = currentNorm;
        }
        int cont = 6;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Vector norm = newNormals[i].crossAndUnit(newNormals[j + 3]);
                newNormals[cont] = norm;
                cont++;
            }
        }
        for (int i = 0; i < newNormals.length; i++) {
            if (newNormals[i].isZero()) {
                newNormals[i] = new Vector(0.0D, 1.0D, 0.0D);
            }
        }
        newNormals[0] = new Vector(1.0D, 0.0D, 0.0D);
        newNormals[1] = new Vector(0.0D, 1.0D, 0.0D);
        newNormals[2] = new Vector(0.0D, 0.0D, 1.0D);

        this.normals = newNormals;
    }

    private Vector[] generateRotationNormals() {
        Vector[] norms = Vector.generateAxisAlignedNorms();
        for (int i = 0; i < 3; i++) {
            getCurrentTickTransform().rotate(norms[i], TransformType.SUBSPACE_TO_GLOBAL);
        }
        return norms;
    }

    // TODO: Use Octrees to optimize this, or more preferably QuickHull3D.
    private void updateParentAABB() {
        IVoxelFieldAABBMaker aabbMaker = parent.getVoxelFieldAABBMaker();
        AxisAlignedBB subspaceBB = aabbMaker.makeVoxelFieldAABB();
        if (subspaceBB == null) {
            // The aabbMaker didn't know what the aabb was, just don't update the aabb for now.
            return;
        }
        // Expand subspaceBB by 1 to fit the block grid.
        subspaceBB = subspaceBB.expand(1, 1, 1);
        // Now transform the subspaceBB to world coordinates
        Polygon largerPoly = new Polygon(subspaceBB, getCurrentTickTransform(),
            TransformType.SUBSPACE_TO_GLOBAL);
        // Set the ship AABB to that of the polygon.
        AxisAlignedBB worldBB = largerPoly.getEnclosedAABB();
        parent.setShipBoundingBox(worldBB);
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
        Vector3d shipCenter = parent.getCenterCoord().toVector3d();

        Vector3d prevPos = new Vector3d(shipCenter);
        Vector3d curPos = new Vector3d(shipCenter);
        prev.transformPosition(prevPos, TransformType.SUBSPACE_TO_GLOBAL);
        cur.transformPosition(curPos, TransformType.SUBSPACE_TO_GLOBAL);
        Vector3d deltaPos = curPos.sub(prevPos, new Vector3d());
        deltaPos.mul(partialTick);

        Vector3d partialPos = new Vector3d(prevPos);
        partialPos.add(deltaPos); // Now partialPos is complete

        Quaterniondc prevRot = prev.rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        Quaterniondc curRot = cur.rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        Quaterniondc partialRot = prevRot.slerp(curRot, partialTick, new Quaterniond());

        Vector3dc angles = partialRot.getEulerAnglesXYZ(new Vector3d());

        // Put it all together to get the render transform.
        renderTransform = new ShipTransform(partialPos.x, partialPos.y,
            partialPos.z, Math.toDegrees(angles.x()), Math.toDegrees(angles.y()),
            Math.toDegrees(angles.z()),
            parent.getCenterCoord());
    }

}
