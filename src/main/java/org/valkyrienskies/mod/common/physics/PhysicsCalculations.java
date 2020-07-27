package org.valkyrienskies.mod.common.physics;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.*;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.block.IBlockTorqueProvider;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.collision.WorldPhysicsCollider;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import valkyrienwarfare.api.TransformType;

import java.lang.Math;
import java.util.*;

public class PhysicsCalculations {

    public static final double DRAG_CONSTANT = .99D;
    public static final double EPSILON = .00000001;

    private final PhysicsObject parent;
    private final WorldPhysicsCollider worldCollision;

    public boolean actAsArchimedes = false;
    private Vector3dc physCenterOfMass;
    private Vector3d torque;
    private double physTickMass;
    // TODO: Get this in one day
    // private double physMass;
    // The time occurring on each PhysTick
    private double physTickTimeDelta;
    private Matrix3dc physMOITensor;
    private Matrix3dc physInvMOITensor;
    private Quaterniondc physRotation;
    private double physX, physY, physZ;

    @Getter
    private final Vector3d linearVelocity;
    @Getter
    private final Vector3d angularVelocity;

    public PhysicsCalculations(PhysicsObject parent) {
        this.parent = parent;
        this.worldCollision = new WorldPhysicsCollider(this);

        this.physMOITensor = null;
        this.physInvMOITensor = null;

        this.linearVelocity = new Vector3d(parent.getPhysicsData().getLinearVelocity());
        this.angularVelocity = new Vector3d(parent.getPhysicsData().getAngularVelocity());

        this.physCenterOfMass = new Vector3d();
        this.torque = new Vector3d();

        generatePhysicsTransform();
    }

    public void generatePhysicsTransform() {
        // Create a new physics transform.
        ShipTransform parentTransform = getParent().getShipData().getShipTransform();
        physRotation = parentTransform.getSubspaceToGlobal().getNormalizedRotation(new Quaterniond());
        physX = parentTransform.getPosX();
        physY = parentTransform.getPosY();
        physZ = parentTransform.getPosZ();
        physCenterOfMass = parentTransform.getCenterCoord();
        ShipTransform physicsTransform = new ShipTransform(physX, physY, physZ, physRotation, physCenterOfMass);
        getParent().getShipTransformationManager()
                .setCurrentPhysicsTransform(physicsTransform);
        // We're doing this afterwards to prevent from prevPhysicsTransform being null.
        getParent().getShipTransformationManager()
                .updatePreviousPhysicsTransform();
    }

    public void rawPhysTickPreCol(double newPhysSpeed) {
        updatePhysSpeedAndIters(newPhysSpeed);
        updatePhysCenterOfMass();
        calculateFramedMOITensor();
        if (!parent.isShipAligningToGrid()) {
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

    public void rawPhysTickPostCol() {
        if (!isPhysicsBroken()) {
            // This wasn't implemented very well at all! Maybe in the future I'll try again.
            // enforceStaticFriction();
            if (VSConfig.doAirshipRotation) {
                integrateAngularVelocity();
            }
            if (VSConfig.doAirshipMovement) {
                integrateLinearVelocity();
            }
        } else {
            getParent().getShipData().setPhysicsEnabled(false);
            getLinearVelocity().zero();
            getAngularVelocity().zero();
        }

        ShipTransform finalPhysTransform = new ShipTransform(physX, physY, physZ, physRotation, physCenterOfMass);

        getParent().getShipTransformationManager().updatePreviousPhysicsTransform();
        getParent().getShipTransformationManager().setCurrentPhysicsTransform(finalPhysTransform);
        // Save a copy of linear and angular velocity in parent's ShipData
        getParent().getShipData().getPhysicsData().setAngularVelocity(new Vector3d(angularVelocity));
        getParent().getShipData().getPhysicsData().setLinearVelocity(new Vector3d(linearVelocity));
    }

    // If the ship is moving at these speeds, its likely something in the physics
    // broke. This method helps detect that.
    private boolean isPhysicsBroken() {
        if (getAngularVelocity().lengthSquared() > 50000
                || getLinearVelocity().lengthSquared() > 50000 || !getAngularVelocity()
                .isFinite() || !getLinearVelocity().isFinite()) {
            System.out.println("Ship tried moving too fast; freezing it and reseting velocities");
            return true;
        }
        return false;
    }

    /**
     * Updates the physics center of mass to the game center of mass; does not do any transformation
     * updates on its own.
     */
    private void updatePhysCenterOfMass() {
        Vector3dc gameTickCM = parent.getInertiaData().getGameTickCenterOfMass();
        if (!physCenterOfMass.equals(gameTickCM)) {
            Vector3d CMDif = gameTickCM.sub(physCenterOfMass, new Vector3d());

            getParent().getShipTransformationManager().getCurrentPhysicsTransform()
                    .transformDirection(CMDif, TransformType.SUBSPACE_TO_GLOBAL);

            physX += CMDif.x;
            physY += CMDif.y;
            physZ += CMDif.z;
            physCenterOfMass = gameTickCM;
        }
    }

    /**
     * Generates the rotated moment of inertia tensor with the body; uses the following formula: I'
     * = R * I * R-transpose; where I' is the rotated inertia, I is un-rotated interim, and R is the
     * rotation matrix.
     * Reference: https://en.wikipedia.org/wiki/Moment_of_inertia#Inertia_matrix_in_different_reference_frames
     */
    private void calculateFramedMOITensor() {
        // physCenterOfMass = new Vector(parent.getCenterCoord());
        physTickMass = parent.getInertiaData().getGameTickMass();

        // Copy the rotation matrix, ignore the translation and scaling parts.
        Matrix3dc rotationMatrix = getParent().getShipTransformationManager()
                .getCurrentPhysicsTransform().createRotationMatrix(TransformType.SUBSPACE_TO_GLOBAL);

        Matrix3dc inertiaBodyFrame = parent.getInertiaData().getGameMoITensor();


        Matrix3d rotationMatrixTranspose = rotationMatrix.transpose(new Matrix3d());


        Matrix3d finalInertia = new Matrix3d(rotationMatrix);
        finalInertia.mul(inertiaBodyFrame);
        finalInertia.mul(rotationMatrixTranspose);


        physMOITensor = finalInertia;
        physInvMOITensor = physMOITensor.invert(new Matrix3d());
    }

    private void calculateForces() {
        applyAirDrag();
        applyGravity();

        Vector3d blockForce = new Vector3d();
        Vector3d inBodyWO = new Vector3d();
        Vector3d crossVector = new Vector3d();
        World worldObj = getParent().getWorld();

        if (VSConfig.doPhysicsBlocks) {
            // We want to loop through all the physics nodes in a sorted order. Priority Queue handles that.
            Queue<IPhysicsBlockController> nodesPriorityQueue = new PriorityQueue<>(
                    parent.getPhysicsControllersInShip());

            while (nodesPriorityQueue.size() > 0) {
                IPhysicsBlockController controller = nodesPriorityQueue.poll();
                controller.onPhysicsTick(parent, this, this.getPhysicsTimeDeltaPerPhysTick());
            }

            SortedMap<IBlockTorqueProvider, List<BlockPos>> torqueProviders = new TreeMap<>();

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            // Note that iterating over "activeForcePositions" is not thread safe, so this could lead to problems.
            // However we're going to completely replace this code soon anyways, so its not a big deal.
            parent.getShipData().activeForcePositions.forEach((x, y, z) -> {
                mutablePos.setPos(x, y, z);
                IBlockState state = getParent().getChunkAt(mutablePos.getX() >> 4, mutablePos.getZ() >> 4).getBlockState(mutablePos);
                Block blockAt = state.getBlock();

                if (blockAt instanceof IBlockForceProvider) {
                    try {
                        BlockPhysicsDetails.getForceFromState(state, mutablePos, worldObj,
                                getPhysicsTimeDeltaPerPhysTick(),
                                getParent(), blockForce);

                        Vector3dc otherPosition = ((IBlockForceProvider) blockAt)
                                .getCustomBlockForcePosition(worldObj,
                                        mutablePos, state, getParent(), getPhysicsTimeDeltaPerPhysTick());

                        if (otherPosition != null) {
                            inBodyWO.set(otherPosition);
                            inBodyWO.sub(physCenterOfMass);
                            getParent().getShipTransformationManager().getCurrentPhysicsTransform()
                                    .transformDirection(inBodyWO, TransformType.SUBSPACE_TO_GLOBAL);
                        } else {
                            inBodyWO.set(mutablePos.getX() + .5, mutablePos.getY() + .5,
                                    mutablePos.getZ() + .5);
                            inBodyWO.sub(physCenterOfMass);
                            getParent().getShipTransformationManager().getCurrentPhysicsTransform()
                                    .transformDirection(inBodyWO, TransformType.SUBSPACE_TO_GLOBAL);
                        }

                        addForceAtPoint(inBodyWO, blockForce, crossVector);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (blockAt instanceof IBlockTorqueProvider) {
                    // Add it to the torque sorted map; we do this so the torque dampeners can run
                    // after the gyroscope stabilizers.
                    IBlockTorqueProvider torqueProviderBlock = (IBlockTorqueProvider) blockAt;
                    if (!torqueProviders.containsKey(torqueProviderBlock)) {
                        torqueProviders.put(torqueProviderBlock, new LinkedList<>());
                    }
                    torqueProviders.get(torqueProviderBlock).add(new BlockPos(x, y, z));
                }
            });

            // Now add the torque from the torque providers, in a sorted order!
            for (IBlockTorqueProvider torqueProviderBlock : torqueProviders.keySet()) {
                List<BlockPos> blockPositions = torqueProviders.get(torqueProviderBlock);
                for (BlockPos pos : blockPositions) {
                    this.convertTorqueToVelocity();
                    Vector3dc torqueVector = torqueProviderBlock
                            .getTorqueInGlobal(this, pos);
                    if (torqueVector != null) {
                        torque.add(torqueVector);
                    }
                }
            }
        }

        convertTorqueToVelocity();
    }

    private void applyGravity() {
        if (VSConfig.doGravity) {
            addForceAtPoint(new Vector3d(),
                    VSConfig.gravity().mul(physTickMass * getPhysicsTimeDeltaPerPhysTick(), new Vector3d()));
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
        AxisAngle4d idealAxisAngle = new AxisAngle4d(inverseCurrentRotation);

        if (idealAxisAngle.angle < EPSILON) {
            // We already have the perfect angular velocity, nothing left to do.
            return;
        }

        // Normalizes the axis, not the angle.
        idealAxisAngle.normalize();

        // Number of seconds we'd expect this angular velocity to convert us onto the grid orientation.
        double timeStep = 1D;
        double idealAngularVelocityMultiple = idealAxisAngle.angle / timeStep;

        Vector3d idealAngularVelocity = new Vector3d(idealAxisAngle.x, idealAxisAngle.y, idealAxisAngle.z);
        idealAngularVelocity.mul(idealAngularVelocityMultiple);

        Vector3d angularVelocityDif = idealAngularVelocity.sub(getAngularVelocity(), new Vector3d());
        // Larger values converge faster, but sacrifice collision accuracy
        angularVelocityDif.mul(.01);

        getAngularVelocity().add(angularVelocityDif);
    }

    private void applyAirDrag() {
        double drag = getDragForPhysTick();
        getLinearVelocity().mul(drag);
        getAngularVelocity().mul(drag);
    }

    private void convertTorqueToVelocity() {
        Vector3d torqueTransformed = new Vector3d(torque);
        getPhysInvMOITensor().transform(torqueTransformed);
        getAngularVelocity().add(torqueTransformed.x, torqueTransformed.y, torqueTransformed.z);
        torque.zero();
    }

    public void addForceAtPoint(Vector3dc inBodyWO,
                                Vector3dc forceToApply) {
        addForceAtPoint(inBodyWO, forceToApply, new Vector3d());
    }

    private void addForceAtPoint(Vector3dc inBodyWO,
                                Vector3dc forceToApply,
                                Vector3d crossVector) {
        inBodyWO.cross(forceToApply, crossVector);
        torque.add(crossVector);
        getLinearVelocity().add(forceToApply.x() * getInvMass(), forceToApply.y() * getInvMass(), forceToApply.z() * getInvMass());
    }

    private void updatePhysSpeedAndIters(double newPhysSpeed) {
        physTickTimeDelta = newPhysSpeed;
    }

    /**
     * Implementation is based on https://gafferongames.com/post/physics_in_3d/
     */
    private void integrateAngularVelocity() {
        // The body angular velocity vector, in World coordinates
        Vector3d angularVelocity = getAngularVelocity();
        if (angularVelocity.lengthSquared() < .001) {
            // Angular velocity is zero, so the rotation hasn't changed.
            return;
        }

        Vector3dc angularVelInBody = new Vector3d(angularVelocity);

        AxisAngle4d axisAngle4d = new AxisAngle4d(angularVelInBody.length() * getPhysicsTimeDeltaPerPhysTick(), angularVelInBody.x(), angularVelInBody.y(), angularVelInBody.z());
        axisAngle4d.normalize();

        // Take the product of the current rotation with the change in rotation that results from
        // the angular velocity. Then change our pitch/yaw/roll based on the result.
        Quaterniondc rotationQuat = new Quaterniond(axisAngle4d);

        physRotation = physRotation.premul(rotationQuat, new Quaterniond()).normalize();
    }

    /**
     * Only run ONCE per phys tick!
     */
    private void integrateLinearVelocity() {
        physX += getLinearVelocity().x() * getPhysicsTimeDeltaPerPhysTick();
        physY += getLinearVelocity().y() * getPhysicsTimeDeltaPerPhysTick();
        physZ += getLinearVelocity().z() * getPhysicsTimeDeltaPerPhysTick();
        physY = Math.min(Math.max(physY, VSConfig.shipLowerLimit), VSConfig.shipUpperLimit);
    }

    public Vector3d getVelocityAtPoint(
            Vector3dc inBodyWO) {
        Vector3d speed = getAngularVelocity().cross(inBodyWO, new Vector3d());
        speed.x += getLinearVelocity().x();
        speed.y += getLinearVelocity().y();
        speed.z += getLinearVelocity().z();
        return speed;
    }

    // These getter methods guarantee that only code within this class can modify
    // the mass, preventing outside code from breaking things
    public double getMass() {
        return physTickMass;
    }

    public double getInvMass() {
        return 1.0 / physTickMass;
    }

    public double getPhysicsTimeDeltaPerPhysTick() {
        return physTickTimeDelta;
    }

    public double getDragForPhysTick() {
        return Math.pow(DRAG_CONSTANT, getPhysicsTimeDeltaPerPhysTick() * 20D);
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
        Vector3d rotationAxis = new Vector3d(getAngularVelocity());
        rotationAxis.normalize();
        getPhysMOITensor().transform(rotationAxis);
        return rotationAxis.length();
    }

}
