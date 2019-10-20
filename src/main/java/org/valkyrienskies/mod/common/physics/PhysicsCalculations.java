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

package org.valkyrienskies.mod.common.physics;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.*;
import org.valkyrienskies.addon.control.block.torque.IRotationNodeWorld;
import org.valkyrienskies.addon.control.block.torque.IRotationNodeWorldProvider;
import org.valkyrienskies.addon.control.block.torque.ImplRotationNodeWorld;
import org.valkyrienskies.addon.control.nodenetwork.INodeController;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.block.IBlockTorqueProvider;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.math.RotationMatrices;
import org.valkyrienskies.mod.common.math.VSMath;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.multithreaded.PhysicsShipTransform;
import org.valkyrienskies.mod.common.physics.collision.WorldPhysicsCollider;
import org.valkyrienskies.mod.common.physics.management.ShipTransformationManager;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienNBTUtils;
import valkyrienwarfare.api.TransformType;

import java.lang.Math;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicsCalculations implements IRotationNodeWorldProvider {

    public static final double DRAG_CONSTANT = .99D;
    public static final double INERTIA_OFFSET = .4D;
    public static final double EPSILON = .00000001;

    private final PhysicsObject parent;
    private final WorldPhysicsCollider worldCollision;
    private final PhysicsParticleManager particleManager;
    // CopyOnWrite to provide concurrency between threads.
    private final Set<BlockPos> activeForcePositions;
    private final IRotationNodeWorld physicsRotationNodeWorld;
    public org.valkyrienskies.mod.common.math.Vector gameTickCenterOfMass;
    public org.valkyrienskies.mod.common.math.Vector linearMomentum;
    public org.valkyrienskies.mod.common.math.Vector angularVelocity;
    public boolean actAsArchimedes;
    private org.valkyrienskies.mod.common.math.Vector physCenterOfMass;
    private org.valkyrienskies.mod.common.math.Vector torque;
    private double gameTickMass;
    // TODO: Get this in one day
    // private double physMass;
    // The time occurring on each PhysTick
    private double physTickTimeDelta;
    private Matrix3dc gameMoITensor;
    private Matrix3dc physMOITensor;
    private Matrix3dc physInvMOITensor;
    private double physRoll, physPitch, physYaw;
    private double physX, physY, physZ;

    public PhysicsCalculations(PhysicsObject toProcess) {
        parent = toProcess;
        worldCollision = new WorldPhysicsCollider(this);
        particleManager = new PhysicsParticleManager(this);

        gameMoITensor = new Matrix3d();
        physMOITensor = new Matrix3d();
        physInvMOITensor = new Matrix3d();

        gameTickCenterOfMass = new org.valkyrienskies.mod.common.math.Vector(
                toProcess.getCenterCoord());
        linearMomentum = new org.valkyrienskies.mod.common.math.Vector();
        physCenterOfMass = new org.valkyrienskies.mod.common.math.Vector();
        angularVelocity = new org.valkyrienskies.mod.common.math.Vector();
        torque = new org.valkyrienskies.mod.common.math.Vector();
        actAsArchimedes = false;
        // We need thread safe access to this.
        activeForcePositions = ConcurrentHashMap.newKeySet();
        this.physicsRotationNodeWorld = new ImplRotationNodeWorld(parent);
    }

    public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos pos) {
        World worldObj = getParent().getWorld();
        if (!newState.equals(oldState)) {
            if (BlockPhysicsDetails.isBlockProvidingForce(newState, pos, worldObj)) {
                activeForcePositions.add(pos);
            } else {
                activeForcePositions.remove(pos);
            }

            double oldMass = BlockPhysicsDetails.getMassFromState(oldState, pos, worldObj);
            double newMass = BlockPhysicsDetails.getMassFromState(newState, pos, worldObj);
            double deltaMass = newMass - oldMass;
            // Don't change anything if the mass is the same
            if (Math.abs(deltaMass) > EPSILON) {
                double x = pos.getX() + .5D;
                double y = pos.getY() + .5D;
                double z = pos.getZ() + .5D;

                deltaMass /= 9D;
                addMassAt(x, y, z, deltaMass);
                addMassAt(x + INERTIA_OFFSET, y + INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(x + INERTIA_OFFSET, y + INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(x + INERTIA_OFFSET, y - INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(x + INERTIA_OFFSET, y - INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(x - INERTIA_OFFSET, y + INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(x - INERTIA_OFFSET, y + INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(x - INERTIA_OFFSET, y - INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(x - INERTIA_OFFSET, y - INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
            }
        }
    }

    private void addMassAt(double x, double y, double z, double addedMass) {
        org.valkyrienskies.mod.common.math.Vector prevCenterOfMass = new org.valkyrienskies.mod.common.math.Vector(
                gameTickCenterOfMass);
        if (gameTickMass > .0001D) {
            gameTickCenterOfMass.multiply(gameTickMass);
            gameTickCenterOfMass
                    .add(new org.valkyrienskies.mod.common.math.Vector(x, y, z).getProduct(addedMass));
            gameTickCenterOfMass.multiply(1.0D / (gameTickMass + addedMass));
        } else {
            gameTickCenterOfMass = new org.valkyrienskies.mod.common.math.Vector(x, y, z);
            gameMoITensor = new Matrix3d();
        }
        double cmShiftX = prevCenterOfMass.x - gameTickCenterOfMass.x;
        double cmShiftY = prevCenterOfMass.y - gameTickCenterOfMass.y;
        double cmShiftZ = prevCenterOfMass.z - gameTickCenterOfMass.z;
        double rx = x - gameTickCenterOfMass.x;
        double ry = y - gameTickCenterOfMass.y;
        double rz = z - gameTickCenterOfMass.z;


        Matrix3d copy = new Matrix3d(gameMoITensor);


        copy.m00 =
                gameMoITensor.m00() + (cmShiftY * cmShiftY + cmShiftZ * cmShiftZ) * gameTickMass
                        + (ry * ry + rz * rz) * addedMass;
        copy.m10 =
                gameMoITensor.m10() - cmShiftX * cmShiftY * gameTickMass - rx * ry * addedMass;
        copy.m20 =
                gameMoITensor.m20() - cmShiftX * cmShiftZ * gameTickMass - rx * rz * addedMass;
        copy.m01 = gameMoITensor.m10();
        copy.m11 =
                gameMoITensor.m11() + (cmShiftX * cmShiftX + cmShiftZ * cmShiftZ) * gameTickMass
                        + (rx * rx + rz * rz) * addedMass;
        copy.m21 =
                gameMoITensor.m21() - cmShiftY * cmShiftZ * gameTickMass - ry * rz * addedMass;
        copy.m02 = gameMoITensor.m20();
        copy.m12 = gameMoITensor.m21();
        copy.m22 =
                gameMoITensor.m22() + (cmShiftX * cmShiftX + cmShiftY * cmShiftY) * gameTickMass
                        + (rx * rx + ry * ry) * addedMass;


        gameMoITensor = copy;

        // Do this to avoid a mass of zero, which runs the risk of dividing by zero and
        // crashing the program.
        if (gameTickMass + addedMass < .0001D) {
            gameTickMass = .0001D;
            getParent().setPhysicsEnabled(false);
        } else {
            gameTickMass += addedMass;
        }
    }

    public void generatePhysicsTransform() {
        // Create a new physics transform.
        physRoll = getParent().getWrapperEntity()
                .getRoll();
        physPitch = getParent().getWrapperEntity()
                .getPitch();
        physYaw = getParent().getWrapperEntity()
                .getYaw();
        physX = getParent().getWrapperEntity().posX;
        physY = getParent().getWrapperEntity().posY;
        physZ = getParent().getWrapperEntity().posZ;
        physCenterOfMass.setValue(gameTickCenterOfMass);
        ShipTransform physicsTransform = new PhysicsShipTransform(physX, physY, physZ, physPitch,
                physYaw, physRoll,
                physCenterOfMass, getParent().getShipBoundingBox(),
                getParent().getShipTransformationManager()
                        .getCurrentTickTransform());
        getParent().getShipTransformationManager()
                .setCurrentPhysicsTransform(physicsTransform);
        // We're doing this afterwards to prevent from prevPhysicsTransform being null.
        getParent().getShipTransformationManager()
                .updatePreviousPhysicsTransform();
    }

    public void rawPhysTickPreCol(double newPhysSpeed) {
        if (getParent().isPhysicsEnabled()) {
            updatePhysSpeedAndIters(newPhysSpeed);
            updateParentCenterOfMass();
            calculateFramedMOITensor();
            if (!parent.getShipAligningToGrid()) {
                // We are not marked for deconstruction, act normal.
                if (!actAsArchimedes) {
                    calculateForces();
                } else {
                    calculateForcesArchimedes();
                }
            } else {
                // We are trying to deconstruct, try to rotate the ship to grid to align with the grid.
                calculateForcesDeconstruction();
            }
        }
    }

    public void rawPhysTickPostCol() {
        if (!isPhysicsBroken()) {
            if (getParent().isPhysicsEnabled()) {
                // This wasn't implemented very well at all! Maybe in the future I'll try again.
                // enforceStaticFriction();
                if (VSConfig.doAirshipRotation) {
                    integrateAngularVelocity();
                }
                if (VSConfig.doAirshipMovement) {
                    integrateLinearVelocity();
                }
            }
        } else {
            getParent().setPhysicsEnabled(false);
            linearMomentum.zero();
            angularVelocity.zero();
        }

        PhysicsShipTransform finalPhysTransform = new PhysicsShipTransform(physX, physY, physZ,
                physPitch, physYaw,
                physRoll, physCenterOfMass, getParent().getShipBoundingBox(),
                getParent().getShipTransformationManager().getCurrentTickTransform());

        getParent().getShipTransformationManager().updatePreviousPhysicsTransform();
        getParent().getShipTransformationManager().setCurrentPhysicsTransform(finalPhysTransform);

        updatePhysCenterOfMass();
    }

    // If the ship is moving at these speeds, its likely something in the physics
    // broke. This method helps detect that.
    private boolean isPhysicsBroken() {
        if (angularVelocity.lengthSq() > 50000
                || linearMomentum.lengthSq() * getInvMass() * getInvMass() > 50000 || angularVelocity
                .isNaN() || linearMomentum.isNaN()) {
            System.out.println("Ship tried moving too fast; freezing it and reseting velocities");
            return true;
        }
        return false;
    }

    // The x/y/z variables need to be updated when the centerOfMass location
    // changes.
    public void updateParentCenterOfMass() {
        org.valkyrienskies.mod.common.math.Vector parentCM = getParent().getCenterCoord();
        if (!getParent().getCenterCoord().equals(gameTickCenterOfMass)) {
            org.valkyrienskies.mod.common.math.Vector CMDif = gameTickCenterOfMass
                    .getSubtraction(parentCM);

            if (getParent().getShipTransformationManager()
                    .getCurrentPhysicsTransform() != null) {
                getParent().getShipTransformationManager()
                        .getCurrentPhysicsTransform()
                        .rotate(CMDif, TransformType.SUBSPACE_TO_GLOBAL);
            }
            getParent().getWrapperEntity().posX -= CMDif.x;
            getParent().getWrapperEntity().posY -= CMDif.y;
            getParent().getWrapperEntity().posZ -= CMDif.z;

            getParent().getCenterCoord().setValue(gameTickCenterOfMass);
        }
    }

    /**
     * Updates the physics center of mass to the game center of mass; does not do any transformation
     * updates on its own.
     */
    private void updatePhysCenterOfMass() {
        if (!physCenterOfMass.equals(gameTickCenterOfMass)) {
            org.valkyrienskies.mod.common.math.Vector CMDif = physCenterOfMass
                    .getSubtraction(gameTickCenterOfMass);

            getParent().getShipTransformationManager().getCurrentPhysicsTransform()
                    .rotate(CMDif, TransformType.SUBSPACE_TO_GLOBAL);
            physX += CMDif.x;
            physY += CMDif.y;
            physZ += CMDif.z;

            physCenterOfMass.setValue(gameTickCenterOfMass);
        }
    }

    /**
     * Generates the rotated moment of inertia tensor with the body; uses the following formula: I'
     * = R * I * R-transpose; where I' is the rotated inertia, I is un-rotated interim, and R is the
     * rotation matrix.
     * Reference: https://en.wikipedia.org/wiki/Moment_of_inertia#Inertia_matrix_in_different_reference_frames
     */
    private void calculateFramedMOITensor() {
        // Copy the rotation matrix, ignore the translation and scaling parts.
        Matrix3dc rotationMatrix = getParent().getShipTransformationManager()
                .getCurrentPhysicsTransform().createRotationMatrix(TransformType.SUBSPACE_TO_GLOBAL);

        Matrix3dc inertiaBodyFrame = gameMoITensor;

        Matrix3d rotationMatrixTranspose = new Matrix3d();
        rotationMatrix.transpose(rotationMatrixTranspose);

        Matrix3d finalInertia = new Matrix3d(rotationMatrix);
        finalInertia.mul(inertiaBodyFrame);
        finalInertia.mul(rotationMatrixTranspose);

        physMOITensor = finalInertia;
        setPhysInvMOITensor(finalInertia.invert(new Matrix3d()));
    }

    protected void calculateForces() {
        applyAirDrag();
        applyGravity();

        // Collections.shuffle(activeForcePositions);

        org.valkyrienskies.mod.common.math.Vector blockForce = new org.valkyrienskies.mod.common.math.Vector();
        org.valkyrienskies.mod.common.math.Vector inBodyWO = new org.valkyrienskies.mod.common.math.Vector();
        org.valkyrienskies.mod.common.math.Vector crossVector = new org.valkyrienskies.mod.common.math.Vector();
        World worldObj = getParent().getWorld();

        if (VSConfig.doPhysicsBlocks) {
            // We want to loop through all the physics nodes in a sorted order. Priority Queue handles that.
            Queue<INodeController> nodesPriorityQueue = new PriorityQueue<>(
                    parent.getPhysicsControllersInShip());

            while (nodesPriorityQueue.size() > 0) {
                INodeController controller = nodesPriorityQueue.poll();
                controller.onPhysicsTick(parent, this, this.getPhysicsTimeDeltaPerPhysTick());
            }

            this.physicsRotationNodeWorld.processTorquePhysics(getPhysicsTimeDeltaPerPhysTick());

            SortedMap<IBlockTorqueProvider, List<BlockPos>> torqueProviders = new TreeMap<IBlockTorqueProvider, List<BlockPos>>();
            for (BlockPos pos : activeForcePositions) {
                IBlockState state = getParent().getChunkAt(pos.getX() >> 4, pos.getZ() >> 4)
                        .getBlockState(pos);
                Block blockAt = state.getBlock();

                if (blockAt instanceof IBlockForceProvider) {
                    try {
                        BlockPhysicsDetails.getForceFromState(state, pos, worldObj,
                                getPhysicsTimeDeltaPerPhysTick(),
                                getParent(), blockForce);

                        Vector otherPosition = ((IBlockForceProvider) blockAt)
                                .getCustomBlockForcePosition(worldObj,
                                        pos, state, getParent(), getPhysicsTimeDeltaPerPhysTick());

                        if (otherPosition != null) {
                            inBodyWO.setValue(otherPosition);
                            inBodyWO.subtract(physCenterOfMass);
                            getParent().getShipTransformationManager().getCurrentPhysicsTransform()
                                    .rotate(inBodyWO, TransformType.SUBSPACE_TO_GLOBAL);
                        } else {
                            inBodyWO.setValue(pos.getX() + .5,
                                    pos.getY() + .5, pos.getZ() + .5);
                            inBodyWO.subtract(physCenterOfMass);
                            getParent().getShipTransformationManager().getCurrentPhysicsTransform()
                                    .rotate(inBodyWO, TransformType.SUBSPACE_TO_GLOBAL);
                        }

                        addForceAtPoint(inBodyWO, blockForce, crossVector);
                        // Add particles here.
                        if (((IBlockForceProvider) blockAt).doesForceSpawnParticles()) {
                            org.valkyrienskies.mod.common.math.Vector particlePos;
                            if (otherPosition != null) {
                                particlePos = new org.valkyrienskies.mod.common.math.Vector(
                                        otherPosition);
                            } else {
                                particlePos = new org.valkyrienskies.mod.common.math.Vector(
                                        pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
                            }
                            parent.getShipTransformationManager().getCurrentPhysicsTransform()
                                    .transform(particlePos, TransformType.SUBSPACE_TO_GLOBAL);
                            // System.out.println(particlePos);
                            float posX = (float) particlePos.x;
                            float posY = (float) particlePos.y;
                            float posZ = (float) particlePos.z;
                            float particleMass = 5f;
                            float velX = (float) -(blockForce.x / particleMass);
                            float velY = (float) -(blockForce.y / particleMass);
                            float velZ = (float) -(blockForce.z / particleMass);
                            // Half a second
                            float particleLife = .5f;
                            // System.out.println(blockForce);
                            // System.out.println(posX + ":" + posY + ":" + posZ);
                            // System.out.println(velX + ":" + velY + ":" + velZ);

                            this.particleManager
                                    .spawnPhysicsParticle(posX, posY, posZ, velX, velY, velZ,
                                            particleMass, particleLife);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (blockAt instanceof IBlockTorqueProvider) {
                    // Add it to the torque sorted map; we do this so the torque dampeners can run
                    // after the gyroscope stabilizers.
                    IBlockTorqueProvider torqueProviderBlock = (IBlockTorqueProvider) blockAt;
                    if (!torqueProviders.containsKey(torqueProviderBlock)) {
                        torqueProviders.put(torqueProviderBlock, new LinkedList<BlockPos>());
                    }
                    torqueProviders.get(torqueProviderBlock).add(pos);
                }
            }

            // Now add the torque from the torque providers, in a sorted order!
            for (IBlockTorqueProvider torqueProviderBlock : torqueProviders.keySet()) {
                List<BlockPos> blockPositions = torqueProviders.get(torqueProviderBlock);
                for (BlockPos pos : blockPositions) {
                    this.convertTorqueToVelocity();
                    org.valkyrienskies.mod.common.math.Vector torqueVector = torqueProviderBlock
                            .getTorqueInGlobal(this, pos);
                    if (torqueVector != null) {
                        torque.add(torqueVector);
                    }
                }
            }
        }
        particleManager.physicsTickAfterAllPreForces((float) getPhysicsTimeDeltaPerPhysTick());

        convertTorqueToVelocity();
    }

    private void applyGravity() {
        if (VSConfig.doGravity) {
            addForceAtPoint(new org.valkyrienskies.mod.common.math.Vector(0, 0, 0),
                    VSConfig.gravity().getProduct(gameTickMass * getPhysicsTimeDeltaPerPhysTick()));
        }
    }

    private void calculateForcesArchimedes() {
        applyAirDrag();
    }

    private void calculateForcesDeconstruction() {
        applyAirDrag();

        Quaterniondc inverseCurrentRotation = parent.getShipTransformationManager()
                .getCurrentPhysicsTransform()
                .rotationQuaternion(TransformType.GLOBAL_TO_SUBSPACE);

        Quaterniondc r = inverseCurrentRotation;
        AxisAngle4d idealAxisAngle = new AxisAngle4d(r);

        if (idealAxisAngle.angle < EPSILON) {
            // We already have the perfect angular velocity, nothing left to do.
            return;
        }

        // Normalizes the axis, not the angle.
        idealAxisAngle.normalize();

        // Number of seconds we'd expect this angular velocity to convert us onto the grid orientation.
        double timeStep = 1D;
        double idealAngularVelocityMultiple = idealAxisAngle.angle / timeStep;

        Vector idealAngularVelocity = new Vector(idealAxisAngle.x, idealAxisAngle.y, idealAxisAngle.z);
        idealAngularVelocity.multiply(idealAngularVelocityMultiple);

        org.valkyrienskies.mod.common.math.Vector angularVelocityDif = idealAngularVelocity
                .getSubtraction(angularVelocity);
        // Larger values converge faster, but sacrifice collision accuracy
        angularVelocityDif.multiply(.01);

        angularVelocity.subtract(angularVelocityDif);
    }

    private void applyAirDrag() {
        double drag = getDragForPhysTick();
        linearMomentum.multiply(drag);
        angularVelocity.multiply(drag);
    }

    public void convertTorqueToVelocity() {
        if (!torque.isZero()) {
            Vector3d torqueTransformed = torque.toVector3d();
            getPhysInvMOITensor().transform(torqueTransformed);
            angularVelocity.add(torqueTransformed.x, torqueTransformed.y, torqueTransformed.z);
            torque.zero();
        }
    }

    public void addForceAtPoint(org.valkyrienskies.mod.common.math.Vector inBodyWO,
                                org.valkyrienskies.mod.common.math.Vector forceToApply) {
        torque.add(inBodyWO.cross(forceToApply));
        linearMomentum.add(forceToApply);
    }

    public void addForceAtPoint(org.valkyrienskies.mod.common.math.Vector inBodyWO,
                                org.valkyrienskies.mod.common.math.Vector forceToApply,
                                org.valkyrienskies.mod.common.math.Vector crossVector) {
        crossVector.setCross(inBodyWO, forceToApply);
        torque.add(crossVector);
        linearMomentum.add(forceToApply);
    }

    public void updatePhysSpeedAndIters(double newPhysSpeed) {
        physTickTimeDelta = newPhysSpeed;
    }

    /**
     * This may or may not be correct :/ It seems to work fine but quaternion math is such a headache I'll take whatever works.
     */
    private void integrateAngularVelocity() {
        ShipTransformationManager coordTrans = getParent().getShipTransformationManager();

        AxisAngle4d axisAngle4d = new AxisAngle4d(angularVelocity.length() * getPhysicsTimeDeltaPerPhysTick(), angularVelocity.x, angularVelocity.y, angularVelocity.z);
        axisAngle4d.normalize();

        Matrix3dc rotationChange = new Matrix3d().set(axisAngle4d);

        // Take the product of the current rotation with the change in rotation that results from
        // the angular velocity. Then change our pitch/yaw/roll based on the result.
        Quaterniondc rotationChangeQuat = rotationChange.getNormalizedRotation(new Quaterniond());
        Quaterniondc initialRotation = coordTrans.getCurrentPhysicsTransform()
                .rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        Quaterniondc finalRotation = rotationChangeQuat.mul(initialRotation, new Quaterniond());

        Vector3dc angles = finalRotation.getEulerAnglesXYZ(new Vector3d());

        physPitch = Double.isNaN(angles.x()) ? 0.0f : (float) Math.toDegrees(angles.x());
        physYaw = Double.isNaN(angles.y()) ? 0.0f : (float) Math.toDegrees(angles.y());
        physRoll = Double.isNaN(angles.z()) ? 0.0f : (float) Math.toDegrees(angles.z());
    }

    /**
     * Only run ONCE per phys tick!
     */
    private void integrateLinearVelocity() {
        double momentMod = getPhysicsTimeDeltaPerPhysTick() * getInvMass();

        physX += (linearMomentum.x * momentMod);
        physY += (linearMomentum.y * momentMod);
        physZ += (linearMomentum.z * momentMod);
        physY = Math.min(Math.max(physY, VSConfig.shipLowerLimit), VSConfig.shipUpperLimit);
    }

    public org.valkyrienskies.mod.common.math.Vector getVelocityAtPoint(
            org.valkyrienskies.mod.common.math.Vector inBodyWO) {
        org.valkyrienskies.mod.common.math.Vector speed = angularVelocity.cross(inBodyWO);
        double invMass = getInvMass();
        speed.x += (linearMomentum.x * invMass);
        speed.y += (linearMomentum.y * invMass);
        speed.z += (linearMomentum.z * invMass);
        return speed;
    }

    public void writeToNBTTag(NBTTagCompound compound) {
        compound.setDouble("mass", gameTickMass);

        ValkyrienNBTUtils.writeVectorToNBT("linear", linearMomentum, compound);
        ValkyrienNBTUtils.writeVectorToNBT("angularVelocity", angularVelocity, compound);
        ValkyrienNBTUtils.writeVectorToNBT("CM", gameTickCenterOfMass, compound);

        ValkyrienNBTUtils.write3x3MatrixToNBT("MOI", gameMoITensor, compound);

        physicsRotationNodeWorld.writeToNBTTag(compound);
        compound.setString("block_mass_ver", BlockPhysicsDetails.BLOCK_MASS_VERSION);
    }

    public void readFromNBTTag(NBTTagCompound compound) {
        linearMomentum = ValkyrienNBTUtils.readVectorFromNBT("linear", compound);
        angularVelocity = ValkyrienNBTUtils.readVectorFromNBT("angularVelocity", compound);
        gameTickCenterOfMass = ValkyrienNBTUtils.readVectorFromNBT("CM", compound);
        gameTickMass = compound.getDouble("mass");
        gameMoITensor = ValkyrienNBTUtils.read3x3MatrixFromNBT("MOI", compound);
        physicsRotationNodeWorld.readFromNBTTag(compound);

        if (!BlockPhysicsDetails.BLOCK_MASS_VERSION.equals(compound.getString("block_mass_ver"))) {
            this.recalculateShipInertia();
        }
    }

    // Called upon a Ship being created from the World, and generates the physics
    // data for it
    public void recalculateShipInertia() {
        linearMomentum.zero();
        angularVelocity.zero();
        gameTickCenterOfMass.zero();
        gameTickMass = 0;
        gameMoITensor = new Matrix3d();
        IBlockState air = Blocks.AIR.getDefaultState();
        for (BlockPos pos : getParent().getBlockPositions()) {
            onSetBlockState(air, getParent().getChunkAt(pos.getX() >> 4, pos.getZ() >> 4)
                    .getBlockState(pos), pos);
        }
    }

    // These getter methods guarantee that only code within this class can modify
    // the mass, preventing outside code from breaking things
    public double getMass() {
        return gameTickMass;
    }

    public double getInvMass() {
        return 1D / gameTickMass;
    }

    public double getPhysicsTimeDeltaPerPhysTick() {
        return physTickTimeDelta;
    }

    public double getDragForPhysTick() {
        return Math.pow(DRAG_CONSTANT, getPhysicsTimeDeltaPerPhysTick() * 20D);
    }

    public void addPotentialActiveForcePos(BlockPos pos) {
        this.activeForcePositions.add(pos);
    }

    /**
     * @return The inverse moment of inertia tensor with local translation (0 vector is at the
     * center of mass), but rotated into world coordinates.
     */
    public Matrix3dc getPhysInvMOITensor() {
        return physInvMOITensor;
    }

    /**
     * @param physInvMOITensor the physInvMOITensor to set
     */
    private void setPhysInvMOITensor(Matrix3dc physInvMOITensor) {
        this.physInvMOITensor = physInvMOITensor;
    }

    /**
     * @return The moment of inertia tensor with local translation (0 vector is at the center of
     * mass), but rotated into world coordinates.
     */
    public Matrix3dc getPhysMOITensor() {
        return this.physMOITensor;
    }

    /**
     * @return the parent
     */
    public PhysicsObject getParent() {
        return parent;
    }

    /**
     * @return the worldCollision
     */
    public WorldPhysicsCollider getWorldCollision() {
        return worldCollision;
    }

    public double getInertiaAlongRotationAxis() {
        Vector3d rotationAxis = new org.valkyrienskies.mod.common.math.Vector(
                angularVelocity).toVector3d();
        rotationAxis.normalize();
        getPhysInvMOITensor().transform(rotationAxis);
        return rotationAxis.length();
    }

    @Deprecated
    public org.valkyrienskies.mod.common.math.Vector getCopyOfPhysCoordinates() {
        return new Vector(physX, physY, physZ);
    }

    public IRotationNodeWorld getPhysicsRotationNodeWorld() {
        return physicsRotationNodeWorld;
    }
}
