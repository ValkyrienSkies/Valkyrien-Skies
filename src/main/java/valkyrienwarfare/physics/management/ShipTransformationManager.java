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

package valkyrienwarfare.physics.management;

import java.util.function.Consumer;
import java.util.stream.Stream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.network.PhysWrapperPositionMessage;
import valkyrienwarfare.physics.data.ShipTransform;
import valkyrienwarfare.physics.data.TransformType;

/**
 * Stores various coordinates and transforms for the ship.
 *
 * @author thebest108
 */
public class ShipTransformationManager {

    // A transformation that does no rotation, and does no translation.
    public static final ShipTransform ZERO_TRANSFORM = new ShipTransform();
    private final PhysicsObject parent;
    private ShipTransform currentTickTransform;
    private ShipTransform renderTransform;
    private ShipTransform prevTickTransform;
    // Used exclusively by the physics engine; should never even be used by the
    // client.
    private ShipTransform currentPhysicsTransform;
    private ShipTransform prevPhysicsTransform;
    public Vector[] normals;
    // A buffer to hold ship transform data sent from server to the client.
    public final ShipTransformationBuffer serverBuffer;

    public ShipTransformationManager(PhysicsObject parent) {
        this.parent = parent;
        this.currentTickTransform = ZERO_TRANSFORM;
        this.renderTransform = ZERO_TRANSFORM;
        this.prevTickTransform = ZERO_TRANSFORM;
        this.currentPhysicsTransform = ZERO_TRANSFORM;
        this.prevPhysicsTransform = ZERO_TRANSFORM;
        this.updateAllTransforms(true, true);
        this.normals = Vector.generateAxisAlignedNorms();
        this.serverBuffer = new ShipTransformationBuffer();
    }

    /**
     * Polls position and rotation data from the parent ship, and creates a new
     * current transform made from this data.
     */
    public void updateCurrentTickTransform() {
        double[] lToWTransform = RotationMatrices.getTranslationMatrix(parent.wrapper.posX, parent.wrapper.posY,
                parent.wrapper.posZ);
        lToWTransform = RotationMatrices.rotateAndTranslate(lToWTransform, parent.wrapper.getPitch(),
                parent.wrapper.getYaw(), parent.wrapper.getRoll(), parent.centerCoord);
        setCurrentTickTransform(new ShipTransform(lToWTransform));
    }

    public void updateRenderTransform(double x, double y, double z, double pitch, double yaw, double roll) {
        double[] RlToWTransform = RotationMatrices.getTranslationMatrix(x, y, z);
        RlToWTransform = RotationMatrices.rotateAndTranslate(RlToWTransform, pitch, yaw, roll, parent.centerCoord);
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
    public void updateAllTransforms(boolean updateParentAABB, boolean updatePassengers) {
        forceShipIntoWorldBorder();
        updateCurrentTickTransform();
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
        WorldBorder border = parent.getWorldObj().getWorldBorder();
        AxisAlignedBB shipBB = parent.getCollisionBoundingBox();

        if (shipBB.maxX > border.maxX()) {
            parent.wrapper.posX += border.maxX() - shipBB.maxX;
        }
        if (shipBB.minX < border.minX()) {
            parent.wrapper.posX += border.minX() - shipBB.minX;
        }
        if (shipBB.maxZ > border.maxZ()) {
            parent.wrapper.posZ += border.maxZ() - shipBB.maxZ;
        }
        if (shipBB.minZ < border.minZ()) {
            parent.wrapper.posZ += border.minZ() - shipBB.minZ;
        }
    }

    public void updatePassengerPositions() {
        for (Entity entity : parent.wrapper.riddenByEntities) {
            parent.wrapper.updatePassenger(entity);
        }
    }

    public void sendPositionToPlayers(int positionTickID) {
        PhysWrapperPositionMessage posMessage = new PhysWrapperPositionMessage(parent.wrapper, positionTickID);

        /*
         * List<Entity> entityList = new ArrayList<Entity>(); for (Entity entity :
         * parent.worldObj.loadedEntityList) { if (entity instanceof IDraggable) {
         * IDraggable draggable = (IDraggable) entity; if (draggable.getWorldBelowFeet()
         * == parent.wrapper) { entityList.add(entity); } } }
         * 
         * EntityRelativePositionMessage otherPositionMessage = new
         * EntityRelativePositionMessage(parent.wrapper, entityList);
         */

        for (EntityPlayerMP player : parent.watchingPlayers) {
            ValkyrienWarfareMod.physWrapperNetwork.sendTo(posMessage, player);
            // ValkyrienWarfareMod.physWrapperNetwork.sendTo(otherPositionMessage, player);
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
            getCurrentTickTransform().rotate(norms[i], TransformType.LOCAL_TO_GLOBAL);
        }
        return norms;
    }

    public Vector[] getSeperatingAxisWithShip(PhysicsObject other) {
        // Note: This Vector array still contains potential 0 vectors, those are removed
        // later
        Vector[] normals = new Vector[15];
        Vector[] otherNorms = other.coordTransform.normals;
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
        CollisionBBConsumer convexHullConsumer = new CollisionBBConsumer();
        Stream<BlockPos> parentPositionsStream = null;
        if (parent.blockPositions.size() < 300) {
            // If its a small ship use a sequential stream.
            parentPositionsStream = parent.blockPositions.stream();
        } else {
            // If its a big ship then we destroy the cpu consumption and go fully
            // multithreaded!
            parentPositionsStream = parent.blockPositions.parallelStream();
        }
        parentPositionsStream.forEach(convexHullConsumer);
        parent.setCollisionBoundingBox(convexHullConsumer.createWrappingAABB());
    }

    /**
     * Transforms a vector from global coordinates to local coordinates, using the
     * getCurrentTickTransform()
     * 
     * @param inGlobal
     */
    public void fromGlobalToLocal(Vector inGlobal) {
        getCurrentTickTransform().transform(inGlobal, TransformType.GLOBAL_TO_LOCAL);
    }

    /**
     * Transforms a vector from local coordinates to global coordinates, using the
     * getCurrentTickTransform()
     * 
     * @param inLocal
     */
    public void fromLocalToGlobal(Vector inLocal) {
        getCurrentTickTransform().transform(inLocal, TransformType.LOCAL_TO_GLOBAL);
    }

    /**
     * @return the current transformation being used this tick.
     */
    public ShipTransform getCurrentTickTransform() {
        return currentTickTransform;
    }

    /**
     * @param currentTransform
     *            the currentTransform to set
     */
    @Deprecated
    private void setCurrentTickTransform(ShipTransform currentTransform) {
        this.currentTickTransform = currentTransform;
    }

    /**
     * @return the renderTransform
     */
    public ShipTransform getRenderTransform() {
        return renderTransform;
    }

    /**
     * @param renderTransform
     *            the renderTransform to set
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
     * @param prevTransform
     *            the prevTransform to set
     */
    private void setPrevTickTransform(ShipTransform prevTransform) {
        this.prevTickTransform = prevTransform;
    }

    /**
     * Returns the transformation data used for physics processing. Added @SideOnly
     * as a check to crash the game if the client ever calls this.
     * 
     * @return the physics transform
     */
    public ShipTransform getCurrentPhysicsTransform() {
        return currentPhysicsTransform;
    }

    /**
     * Sets the physics transform to the given input.
     * 
     * @param physicsTransform
     */
    public void setCurrentPhysicsTransform(ShipTransform currentPhysicsTransform) {
        this.currentPhysicsTransform = currentPhysicsTransform;
    }

    public ShipTransform getPreviousPhysicsTransform() {
        return prevPhysicsTransform;
    }

    public void updatePreviousPhysicsTransform() {
        this.prevPhysicsTransform = currentPhysicsTransform;
    }

    private class CollisionBBConsumer implements Consumer<BlockPos> {
        private static final double AABB_EXPANSION = 1.6D;
        private final double[] M = getCurrentTickTransform().getInternalMatrix(TransformType.LOCAL_TO_GLOBAL);
        double minX, minY, minZ, maxX, maxY, maxZ;

        CollisionBBConsumer() {
            minX = parent.wrapper.posX;
            minY = parent.wrapper.posY;
            minZ = parent.wrapper.posZ;
            maxX = parent.wrapper.posX;
            maxY = parent.wrapper.posY;
            maxZ = parent.wrapper.posZ;
        }

        @Override
        public void accept(BlockPos pos) {
            double x = pos.getX() + .5D;
            double y = pos.getY() + .5D;
            double z = pos.getZ() + .5D;

            double newX = x * M[0] + y * M[1] + z * M[2] + M[3];
            double newY = x * M[4] + y * M[5] + z * M[6] + M[7];
            double newZ = x * M[8] + y * M[9] + z * M[10] + M[11];

            minX = Math.min(newX, minX);
            maxX = Math.max(newX, maxX);
            minY = Math.min(newY, minY);
            maxY = Math.max(newY, maxY);
            minZ = Math.min(newZ, minZ);
            maxZ = Math.max(newZ, maxZ);
        }

        AxisAlignedBB createWrappingAABB() {
            return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).grow(AABB_EXPANSION);
        }

    }
}
