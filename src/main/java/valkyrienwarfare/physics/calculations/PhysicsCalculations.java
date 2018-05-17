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

package valkyrienwarfare.physics.calculations;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.vecmath.Matrix3d;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.nodenetwork.INodePhysicsProcessor;
import valkyrienwarfare.addon.control.nodenetwork.Node;
import valkyrienwarfare.api.IBlockForceProvider;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.math.Quaternion;
import valkyrienwarfare.math.VWMath;
import valkyrienwarfare.mod.multithreaded.PhysicsShipTransform;
import valkyrienwarfare.physics.collision.WorldPhysicsCollider;
import valkyrienwarfare.physics.data.BlockForce;
import valkyrienwarfare.physics.data.BlockMass;
import valkyrienwarfare.physics.data.ShipTransform;
import valkyrienwarfare.physics.data.TransformType;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.ShipTransformationManager;
import valkyrienwarfare.util.NBTUtils;
import valkyrienwarfare.util.PhysicsSettings;

public class PhysicsCalculations {

    // Without this the physics feels too slow
    public static final double PHYSICS_SPEEDUP_FACTOR = 1.8D;
    public static final double DRAG_CONSTANT = .99D;
    public static final double INERTIA_OFFSET = .4D;
    public static final double EPSILON = 0xE - 8;

    public final PhysicsObject parent;
    public final WorldPhysicsCollider worldCollision;

    public Vector gameTickCenterOfMass;
    private Vector physCenterOfMass;
    public Vector linearMomentum;
    public Vector angularVelocity;
    private Vector torque;
    private double mass;
    // The time occurring on each PhysTick
    private double physRawSpeed;
    // Number of iterations the solver runs on each game tick
    private int iterations;

    // CopyOnWrite to provide concurrency between threads.
    private final Set<BlockPos> activeForcePositions;
    private final SortedSet<INodePhysicsProcessor> physicsTasks;
    public double[] MoITensor, invMoITensor;
    public double[] framedMOI, invFramedMOI;
    public boolean actAsArchimedes = false;

    private double physRoll, physPitch, physYaw;
    private double physX, physY, physZ;

    public PhysicsCalculations(PhysicsObject toProcess) {
        parent = toProcess;
        worldCollision = new WorldPhysicsCollider(this);

        MoITensor = RotationMatrices.getZeroMatrix(3);
        invMoITensor = RotationMatrices.getZeroMatrix(3);
        framedMOI = RotationMatrices.getZeroMatrix(3);
        invFramedMOI = RotationMatrices.getZeroMatrix(3);

        gameTickCenterOfMass = new Vector(toProcess.centerCoord);
        linearMomentum = new Vector();
        physCenterOfMass = new Vector();
        angularVelocity = new Vector();
        torque = new Vector();
        iterations = 5;
        // We need thread safe access to this.
        activeForcePositions = ConcurrentHashMap.newKeySet();
        physicsTasks = new TreeSet<INodePhysicsProcessor>();
    }

    public PhysicsCalculations(PhysicsCalculations toCopy) {
        parent = toCopy.parent;
        worldCollision = toCopy.worldCollision;
        gameTickCenterOfMass = toCopy.gameTickCenterOfMass;
        linearMomentum = toCopy.linearMomentum;
        angularVelocity = toCopy.angularVelocity;
        torque = toCopy.torque;
        mass = toCopy.mass;
        physRawSpeed = toCopy.physRawSpeed;
        iterations = toCopy.iterations;
        activeForcePositions = toCopy.activeForcePositions;
        MoITensor = toCopy.MoITensor;
        invMoITensor = toCopy.invMoITensor;
        framedMOI = toCopy.framedMOI;
        invFramedMOI = toCopy.invFramedMOI;
        actAsArchimedes = toCopy.actAsArchimedes;
        physicsTasks = toCopy.physicsTasks;
    }

    public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos pos) {
        World worldObj = parent.getWorldObj();
        if (!newState.equals(oldState)) {
            if (oldState.getBlock() == Blocks.AIR) {
                if (BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)) {
                    activeForcePositions.add(pos);
                }
            } else {
                if (activeForcePositions.contains(pos)) {
                    if (!BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)) {
                        activeForcePositions.remove(pos);
                    }
                } else {
                    if (BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)) {
                        activeForcePositions.add(pos);
                    }
                }
            }
            if (newState.getBlock() == Blocks.AIR) {
                activeForcePositions.remove(pos);
            }

            double oldMass = BlockMass.basicMass.getMassFromState(oldState, pos, worldObj);
            double newMass = BlockMass.basicMass.getMassFromState(newState, pos, worldObj);
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
        Vector prevCenterOfMass = new Vector(gameTickCenterOfMass);
        if (mass > .0001D) {
            gameTickCenterOfMass.multiply(mass);
            gameTickCenterOfMass.add(new Vector(x, y, z).getProduct(addedMass));
            gameTickCenterOfMass.multiply(1.0D / (mass + addedMass));
        } else {
            gameTickCenterOfMass = new Vector(x, y, z);
            MoITensor = RotationMatrices.getZeroMatrix(3);
        }
        double cmShiftX = prevCenterOfMass.X - gameTickCenterOfMass.X;
        double cmShiftY = prevCenterOfMass.Y - gameTickCenterOfMass.Y;
        double cmShiftZ = prevCenterOfMass.Z - gameTickCenterOfMass.Z;
        double rx = x - gameTickCenterOfMass.X;
        double ry = y - gameTickCenterOfMass.Y;
        double rz = z - gameTickCenterOfMass.Z;

        MoITensor[0] = MoITensor[0] + (cmShiftY * cmShiftY + cmShiftZ * cmShiftZ) * mass
                + (ry * ry + rz * rz) * addedMass;
        MoITensor[1] = MoITensor[1] - cmShiftX * cmShiftY * mass - rx * ry * addedMass;
        MoITensor[2] = MoITensor[2] - cmShiftX * cmShiftZ * mass - rx * rz * addedMass;
        MoITensor[3] = MoITensor[1];
        MoITensor[4] = MoITensor[4] + (cmShiftX * cmShiftX + cmShiftZ * cmShiftZ) * mass
                + (rx * rx + rz * rz) * addedMass;
        MoITensor[5] = MoITensor[5] - cmShiftY * cmShiftZ * mass - ry * rz * addedMass;
        MoITensor[6] = MoITensor[2];
        MoITensor[7] = MoITensor[5];
        MoITensor[8] = MoITensor[8] + (cmShiftX * cmShiftX + cmShiftY * cmShiftY) * mass
                + (rx * rx + ry * ry) * addedMass;

        mass += addedMass;
        invMoITensor = RotationMatrices.inverse3by3(MoITensor);
    }

    public void rawPhysTickPreCol(double newPhysSpeed, int iters) {
        if (parent.coordTransform.getCurrentPhysicsTransform() == ShipTransformationManager.ZERO_TRANSFORM) {
            // Create a new physics transform.
            parent.coordTransform.setCurrentPhysicsTransform(
                    new PhysicsShipTransform(parent.coordTransform.getCurrentTickTransform()));
            parent.coordTransform.updatePreviousPhysicsTransform();
            physRoll = parent.wrapper.getRoll();
            physPitch = parent.wrapper.getPitch();
            physYaw = parent.wrapper.getYaw();
            physX = parent.wrapper.posX;
            physY = parent.wrapper.posY;
            physZ = parent.wrapper.posZ;
            physCenterOfMass.setValue(gameTickCenterOfMass);
        }
        if (parent.doPhysics) {
            updatePhysSpeedAndIters(newPhysSpeed, iters);
            updateParentCenterOfMass();
            calculateFramedMOITensor();
            if (!actAsArchimedes) {
                calculateForces();
            } else {
                calculateForcesArchimedes();
            }
        }
    }

    public void processWorldCollision() {
        if (parent.doPhysics) {
            worldCollision.runPhysCollision();
        }
    }

    public void rawPhysTickPostCol() {
        if (parent.doPhysics) {
            if (!isPhysicsBroken()) {
                if (PhysicsSettings.doAirshipRotation) {
                    applyAngularVelocity();
                }
                if (PhysicsSettings.doAirshipMovement) {
                    applyLinearVelocity();
                }
            } else {
                parent.doPhysics = false;
                linearMomentum.zero();
                angularVelocity.zero();
            }
            ShipTransform finalPhysTransform = new ShipTransform(physX, physY, physZ, physPitch, physYaw, physRoll,
                    physCenterOfMass);

            parent.coordTransform.setCurrentPhysicsTransform(finalPhysTransform);

            updatePhysCenterOfMass();
            updateParentToPhysCoords();

            parent.coordTransform.updateAllTransforms(true, true);
        }
    }

    private void updateParentToPhysCoords() {
        parent.wrapper.setPitch(physPitch);
        parent.wrapper.setYaw(physYaw);
        parent.wrapper.setRoll(physRoll);

        parent.wrapper.posX = physX;
        parent.wrapper.posY = physY;
        parent.wrapper.posZ = physZ;
    }

    // If the ship is moving at these speeds, its likely something in the physics
    // broke. This method helps detect that.
    private boolean isPhysicsBroken() {
        if (angularVelocity.lengthSq() > 50000 || linearMomentum.lengthSq() * getInvMass() * getInvMass() > 50000) {
            System.out.println("Ship tried moving too fast; freezing it and reseting velocities");
            return true;
        }
        return false;
    }

    // The x/y/z variables need to be updated when the centerOfMass location
    // changes.
    public void updateParentCenterOfMass() {
        Vector parentCM = parent.centerCoord;
        if (!parent.centerCoord.equals(gameTickCenterOfMass)) {
            Vector CMDif = gameTickCenterOfMass.getSubtraction(parentCM);
            // RotationMatrices.doRotationOnly(parent.coordTransform.lToWTransform, CMDif);

            parent.coordTransform.getCurrentTickTransform().rotate(CMDif, TransformType.LOCAL_TO_GLOBAL);
            parent.wrapper.posX -= CMDif.X;
            parent.wrapper.posY -= CMDif.Y;
            parent.wrapper.posZ -= CMDif.Z;

            parent.centerCoord.setValue(gameTickCenterOfMass);
            // parent.coordTransform.updateAllTransforms(false, false);
        }
    }

    /**
     * Updates the physics center of mass to the game center of mass; does not do
     * any transformation updates on its own.
     */
    private void updatePhysCenterOfMass() {
        if (!physCenterOfMass.equals(gameTickCenterOfMass)) {
            Vector CMDif = physCenterOfMass.getSubtraction(gameTickCenterOfMass);
            // RotationMatrices.doRotationOnly(parent.coordTransform.lToWTransform, CMDif);

            parent.coordTransform.getCurrentPhysicsTransform().rotate(CMDif, TransformType.LOCAL_TO_GLOBAL);
            physX += CMDif.X;
            physY += CMDif.Y;
            physZ += CMDif.Z;

            physCenterOfMass.setValue(gameTickCenterOfMass);
        }
    }

    /**
     * Generates the rotated moment of inertia tensor with the body; uses the
     * following formula: I' = R * I * R-transpose; where I' is the rotated inertia,
     * I is unrotated interim, and R is the rotation matrix.
     */
    private void calculateFramedMOITensor() {
        framedMOI = RotationMatrices.getZeroMatrix(3);

        double[] internalRotationMatrix = parent.coordTransform.getCurrentPhysicsTransform()
                .getInternalMatrix(TransformType.LOCAL_TO_GLOBAL);

        // Copy the rotation matrix, ignore the translation and scaling parts.
        Matrix3d rotationMatrix = new Matrix3d(internalRotationMatrix[0], internalRotationMatrix[1],
                internalRotationMatrix[2], internalRotationMatrix[4], internalRotationMatrix[5],
                internalRotationMatrix[6], internalRotationMatrix[8], internalRotationMatrix[9],
                internalRotationMatrix[10]);

        Matrix3d inertiaBodyFrame = new Matrix3d(MoITensor);
        // The product of the overall rotation matrix with the inertia tensor.
        inertiaBodyFrame.mul(rotationMatrix);
        rotationMatrix.transpose();
        // The product of the inertia tensor multiplied with the transpose of the
        // rotation transpose.
        inertiaBodyFrame.mul(rotationMatrix);
        framedMOI[0] = inertiaBodyFrame.m00;
        framedMOI[1] = inertiaBodyFrame.m01;
        framedMOI[2] = inertiaBodyFrame.m02;
        framedMOI[3] = inertiaBodyFrame.m10;
        framedMOI[4] = inertiaBodyFrame.m11;
        framedMOI[5] = inertiaBodyFrame.m12;
        framedMOI[6] = inertiaBodyFrame.m20;
        framedMOI[7] = inertiaBodyFrame.m21;
        framedMOI[8] = inertiaBodyFrame.m22;
        invFramedMOI = RotationMatrices.inverse3by3(framedMOI);
    }

    protected void calculateForces() {
        applyAirDrag();
        applyGravity();

        // Collections.shuffle(activeForcePositions);

        Vector blockForce = new Vector();
        Vector inBodyWO = new Vector();
        Vector crossVector = new Vector();
        World worldObj = parent.getWorldObj();

        if (PhysicsSettings.doPhysicsBlocks) {

            physicsTasks.clear();
            for (Node node : parent.nodesWithinShip) {
                TileEntity nodeTile = node.getParentTile();
                if (nodeTile instanceof INodePhysicsProcessor) {
                    // Iterate through them in sorted order
                    physicsTasks.add((INodePhysicsProcessor) nodeTile);
                }
            }

            // This iterates over a SortedSet to retain sorted order, allowing some tasks to
            // be given greater priority than others.
            for (INodePhysicsProcessor physicsProcessorNode : physicsTasks) {
                physicsProcessorNode.onPhysicsTick(parent, this, physRawSpeed);
            }

            for (BlockPos pos : activeForcePositions) {
                IBlockState state = parent.VKChunkCache.getBlockState(pos);
                Block blockAt = state.getBlock();
                VWMath.getBodyPosWithOrientation(pos, physCenterOfMass, parent.coordTransform
                        .getCurrentPhysicsTransform().getInternalMatrix(TransformType.LOCAL_TO_GLOBAL), inBodyWO);

                BlockForce.basicForces.getForceFromState(state, pos, worldObj, getPhysicsTimeDeltaPerPhysTick(), parent,
                        blockForce);

                if (blockForce != null) {
                    if (blockAt instanceof IBlockForceProvider) {
                        Vector otherPosition = ((IBlockForceProvider) blockAt).getCustomBlockForcePosition(worldObj,
                                pos, state, parent.wrapper, getPhysicsTimeDeltaPerPhysTick());
                        if (otherPosition != null) {
                            VWMath.getBodyPosWithOrientation(otherPosition, gameTickCenterOfMass, parent.coordTransform
                                    .getCurrentPhysicsTransform().getInternalMatrix(TransformType.LOCAL_TO_GLOBAL),
                                    inBodyWO);
                        }
                    }
                    addForceAtPoint(inBodyWO, blockForce, crossVector);
                }
            }
        }

        convertTorqueToVelocity();
    }

    public void applyGravity() {
        if (PhysicsSettings.doGravity) {
            addForceAtPoint(new Vector(0, 0, 0),
                    ValkyrienWarfareMod.gravity.getProduct(mass * getPhysicsTimeDeltaPerPhysTick()));
        }
    }

    public void calculateForcesArchimedes() {
        applyAirDrag();
    }

    protected void applyAirDrag() {
        double drag = getDragForPhysTick();
        linearMomentum.multiply(drag);
        angularVelocity.multiply(drag);
    }

    public void convertTorqueToVelocity() {
        if (!torque.isZero()) {
            angularVelocity.add(RotationMatrices.get3by3TransformedVec(invFramedMOI, torque));
            torque.zero();
        }
    }

    public void addForceAtPoint(Vector inBodyWO, Vector forceToApply) {
        forceToApply.multiply(PHYSICS_SPEEDUP_FACTOR);
        torque.add(inBodyWO.cross(forceToApply));
        linearMomentum.add(forceToApply);
    }

    public void addForceAtPoint(Vector inBodyWO, Vector forceToApply, Vector crossVector) {
        forceToApply.multiply(PHYSICS_SPEEDUP_FACTOR);
        crossVector.setCross(inBodyWO, forceToApply);
        torque.add(crossVector);
        linearMomentum.add(forceToApply);
    }

    public void updatePhysSpeedAndIters(double newPhysSpeed, int iters) {
        physRawSpeed = newPhysSpeed;
        iterations = iters;
    }

    public void applyAngularVelocity() {
        ShipTransformationManager coordTrans = parent.coordTransform;

        double[] rotationChange = RotationMatrices.getRotationMatrix(angularVelocity.X, angularVelocity.Y,
                angularVelocity.Z, angularVelocity.length() * getPhysicsTimeDeltaPerPhysTick());
        Quaternion finalTransform = Quaternion.QuaternionFromMatrix(RotationMatrices.getMatrixProduct(rotationChange,
                coordTrans.getCurrentPhysicsTransform().getInternalMatrix(TransformType.LOCAL_TO_GLOBAL)));

        double[] radians = finalTransform.toRadians();

        physPitch = Double.isNaN(radians[0]) ? 0.0f : (float) Math.toDegrees(radians[0]);
        physYaw = Double.isNaN(radians[1]) ? 0.0f : (float) Math.toDegrees(radians[1]);
        physRoll = Double.isNaN(radians[2]) ? 0.0f : (float) Math.toDegrees(radians[2]);

        // parent.wrapper.setPitch(Double.isNaN(radians[0]) ? 0.0f : (float)
        // Math.toDegrees(radians[0]));
        // parent.wrapper.setYaw(Double.isNaN(radians[1]) ? 0.0f : (float)
        // Math.toDegrees(radians[1]));
        // parent.wrapper.setRoll(Double.isNaN(radians[2]) ? 0.0f : (float)
        // Math.toDegrees(radians[2]));
    }

    public void applyLinearVelocity() {
        double momentMod = getPhysicsTimeDeltaPerPhysTick() * getInvMass();

        physX += (linearMomentum.X * momentMod);
        physY += (linearMomentum.Y * momentMod);
        physZ += (linearMomentum.Z * momentMod);
        physY = Math.min(Math.max(physY, ValkyrienWarfareMod.shipLowerLimit), ValkyrienWarfareMod.shipUpperLimit);

        // parent.wrapper.posX += (linearMomentum.X * momentMod);
        // parent.wrapper.posY += (linearMomentum.Y * momentMod);
        // parent.wrapper.posZ += (linearMomentum.Z * momentMod);
        // parent.wrapper.posY = Math.min(Math.max(parent.wrapper.posY,
        // ValkyrienWarfareMod.shipLowerLimit),
        // ValkyrienWarfareMod.shipUpperLimit);
    }

    public Vector getVelocityAtPoint(Vector inBodyWO) {
        Vector speed = angularVelocity.cross(inBodyWO);
        double invMass = getInvMass();
        speed.X += (linearMomentum.X * invMass);
        speed.Y += (linearMomentum.Y * invMass);
        speed.Z += (linearMomentum.Z * invMass);
        return speed;
    }

    public void setVectorToVelocityAtPoint(Vector inBodyWO, Vector toSet) {
        toSet.setCross(angularVelocity, inBodyWO);
        double invMass = getInvMass();
        toSet.X += (linearMomentum.X * invMass);
        toSet.Y += (linearMomentum.Y * invMass);
        toSet.Z += (linearMomentum.Z * invMass);
    }

    public void writeToNBTTag(NBTTagCompound compound) {
        compound.setDouble("mass", mass);

        NBTUtils.writeVectorToNBT("linear", linearMomentum, compound);
        NBTUtils.writeVectorToNBT("angularVelocity", angularVelocity, compound);
        NBTUtils.writeVectorToNBT("CM", gameTickCenterOfMass, compound);

        NBTUtils.write3x3MatrixToNBT("MOI", MoITensor, compound);
    }

    public void readFromNBTTag(NBTTagCompound compound) {
        mass = compound.getDouble("mass");

        linearMomentum = NBTUtils.readVectorFromNBT("linear", compound);
        angularVelocity = NBTUtils.readVectorFromNBT("angularVelocity", compound);
        gameTickCenterOfMass = NBTUtils.readVectorFromNBT("CM", compound);

        MoITensor = NBTUtils.read3x3MatrixFromNBT("MOI", compound);

        invMoITensor = RotationMatrices.inverse3by3(MoITensor);
    }

    // Called upon a Ship being created from the World, and generates the physics
    // data for it
    public void processInitialPhysicsData() {
        IBlockState air = Blocks.AIR.getDefaultState();
        for (BlockPos pos : parent.blockPositions) {
            onSetBlockState(air, parent.VKChunkCache.getBlockState(pos), pos);
        }
    }

    // These getter methods guarantee that only code within this class can modify
    // the mass,
    // preventing outside code from breaking things
    public double getMass() {
        return mass;
    }

    public double getInvMass() {
        return 1D / mass;
    }

    public double getPhysicsTimeDeltaPerPhysTick() {
        return getPhysicsTimeDeltaPerGameTick() / getPhysicsTicksPerGameTick();
    }

    public double getPhysicsTimeDeltaPerGameTick() {
        return physRawSpeed;
    }

    public int getPhysicsTicksPerGameTick() {
        return iterations;
    }

    public double getDragForPhysTick() {
        return Math.pow(DRAG_CONSTANT, getPhysicsTimeDeltaPerPhysTick() * 20D);
    }

    public void addPotentialActiveForcePos(BlockPos pos) {
        this.activeForcePositions.add(pos);
    }

}
