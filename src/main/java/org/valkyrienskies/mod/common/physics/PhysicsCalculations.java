package org.valkyrienskies.mod.common.physics;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.AxisAngle4d;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.block.torque.IRotationNodeWorld;
import org.valkyrienskies.addon.control.block.torque.IRotationNodeWorldProvider;
import org.valkyrienskies.addon.control.block.torque.ImplRotationNodeWorld;
import org.valkyrienskies.addon.control.nodenetwork.INodeController;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.block.IBlockTorqueProvider;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.collision.WorldPhysicsCollider;
import org.valkyrienskies.mod.common.physics.management.ShipTransformationManager;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.ShipPhysicsData;
import valkyrienwarfare.api.TransformType;

public class PhysicsCalculations implements IRotationNodeWorldProvider {

    public static final double DRAG_CONSTANT = .99D;
    public static final double EPSILON = .00000001;

    @Delegate
    private ShipPhysicsData data;

    private final PhysicsObject parent;
    private final WorldPhysicsCollider worldCollision;
    private final PhysicsParticleManager particleManager;
    // CopyOnWrite to provide concurrency between threads.
    private final Set<BlockPos> activeForcePositions;
    private final IRotationNodeWorld physicsRotationNodeWorld;

    public boolean actAsArchimedes = false;
    private Vector physCenterOfMass;
    private Vector torque;
    private double physTickMass;
    // TODO: Get this in one day
    // private double physMass;
    // The time occurring on each PhysTick
    private double physTickTimeDelta;
    private Matrix3dc physMOITensor;
    private Matrix3dc physInvMOITensor;
    private double physRoll, physPitch, physYaw;
    private double physX, physY, physZ;

    public PhysicsCalculations(PhysicsObject parent) {
        this(parent, parent.getData().getPhysicsData());
    }

    public PhysicsCalculations(PhysicsObject parent, ShipPhysicsData data) {
        this.parent = parent;
        this.worldCollision = new WorldPhysicsCollider(this);
        this.particleManager = new PhysicsParticleManager(this);

        this.data = data;

        this.physMOITensor = null;
        this.physInvMOITensor = null;

        this.physCenterOfMass = new Vector();
        this.torque = new Vector();
        // We need thread safe access to this.
        this.activeForcePositions = ConcurrentHashMap.newKeySet();
        this.physicsRotationNodeWorld = new ImplRotationNodeWorld(this.parent);
    }

    public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos pos) {
        World worldObj = getParent().getWorld();
        if (!newState.equals(oldState)) {
            // TODO: Axe this too.
            if (BlockPhysicsDetails.isBlockProvidingForce(newState, pos, worldObj)) {
                activeForcePositions.add(pos);
            } else {
                activeForcePositions.remove(pos);
            }
        }
    }

    public void generatePhysicsTransform() {
        // Create a new physics transform.
        ShipTransform parentTransform = getParent().getData().getShipTransform();
        physRoll = parentTransform.getRoll();
        physPitch = parentTransform.getPitch();
        physYaw = parentTransform.getYaw();
        physX = parentTransform.getPosX();
        physY = parentTransform.getPosY();
        physZ = parentTransform.getPosZ();
        physCenterOfMass.setValue(parentTransform.getCenterCoord());
        ShipTransform physicsTransform = new ShipTransform(physX, physY, physZ, physPitch,
                physYaw, physRoll,
                physCenterOfMass.toVector3d());
        getParent().getShipTransformationManager()
                .setCurrentPhysicsTransform(physicsTransform);
        // We're doing this afterwards to prevent from prevPhysicsTransform being null.
        getParent().getShipTransformationManager()
                .updatePreviousPhysicsTransform();
    }

    public void rawPhysTickPreCol(double newPhysSpeed) {
        if (getParent().getData().isPhysicsEnabled()) {
            updatePhysSpeedAndIters(newPhysSpeed);
            updateParentCenterOfMass();
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
    }

    public void rawPhysTickPostCol() {
        if (!isPhysicsBroken()) {
            if (getParent().getData().isPhysicsEnabled()) {
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
            getParent().getData().setPhysicsEnabled(false);
            getLinearMomentum().zero();
            getAngularVelocity().zero();
        }

        ShipTransform finalPhysTransform = new ShipTransform(physX, physY, physZ,
                physPitch, physYaw,
                physRoll, physCenterOfMass.toVector3d());

        getParent().getShipTransformationManager().updatePreviousPhysicsTransform();
        getParent().getShipTransformationManager().setCurrentPhysicsTransform(finalPhysTransform);

        updatePhysCenterOfMass();
    }

    // If the ship is moving at these speeds, its likely something in the physics
    // broke. This method helps detect that.
    private boolean isPhysicsBroken() {
        if (getAngularVelocity().lengthSq() > 50000
                || getLinearMomentum().lengthSq() * getInvMass() * getInvMass() > 50000 || getAngularVelocity()
                .isNaN() || getLinearMomentum().isNaN()) {
            System.out.println("Ship tried moving too fast; freezing it and reseting velocities");
            return true;
        }
        return false;
    }

    // The x/y/z variables need to be updated when the centerOfMass location
    // changes.
    @Deprecated
    public void updateParentCenterOfMass() {
        if (!getParent().getCenterCoord().equals(parent.getInertiaData().getGameTickCenterOfMass())) {
            Vector CMDif = parent.getCenterCoord()
                    .getSubtraction(parent.getInertiaData().getGameTickCenterOfMass());

            if (getParent().getShipTransformationManager()
                    .getCurrentPhysicsTransform() != null) {
                getParent().getShipTransformationManager()
                        .getCurrentPhysicsTransform()
                        .rotate(CMDif, TransformType.SUBSPACE_TO_GLOBAL);
            }


            ShipTransform transform = getParent().getTransform();
            ShipTransform newTransform = getParent().getTransform().toBuilder()
                    .posX(transform.getPosX() + CMDif.x)
                    .posY(transform.getPosY() + CMDif.y)
                    .posZ(transform.getPosZ() + CMDif.z)
                    .centerCoord(parent.getInertiaData().getGameTickCenterOfMass().toVector3d())
                .build();

            getParent().updateTransform(newTransform);
            getParent().getShipTransformationManager().updateAllTransforms(newTransform, false, true);

            getParent().getCenterCoord().setValue(parent.getCenterCoord());
        }
    }

    /**
     * Updates the physics center of mass to the game center of mass; does not do any transformation
     * updates on its own.
     */
    @Deprecated
    private void updatePhysCenterOfMass() {
        if (!physCenterOfMass.equals(parent.getCenterCoord())) {
            Vector CMDif = physCenterOfMass
                    .getSubtraction(parent.getCenterCoord());

            getParent().getShipTransformationManager().getCurrentPhysicsTransform()
                    .rotate(CMDif, TransformType.SUBSPACE_TO_GLOBAL);
            physX += CMDif.x;
            physY += CMDif.y;
            physZ += CMDif.z;

            physCenterOfMass.setValue(parent.getCenterCoord());
        }
    }

    /**
     * Generates the rotated moment of inertia tensor with the body; uses the following formula: I'
     * = R * I * R-transpose; where I' is the rotated inertia, I is un-rotated interim, and R is the
     * rotation matrix.
     * Reference: https://en.wikipedia.org/wiki/Moment_of_inertia#Inertia_matrix_in_different_reference_frames
     */
    private void calculateFramedMOITensor() {
        physCenterOfMass = new Vector(parent.getCenterCoord());
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

    protected void calculateForces() {
        applyAirDrag();
        applyGravity();

        // Collections.shuffle(activeForcePositions);

        Vector blockForce = new Vector();
        Vector inBodyWO = new Vector();
        Vector crossVector = new Vector();
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
                            Vector particlePos;
                            if (otherPosition != null) {
                                particlePos = new Vector(
                                        otherPosition);
                            } else {
                                particlePos = new Vector(
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
                    Vector torqueVector = torqueProviderBlock
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
            addForceAtPoint(new Vector(0, 0, 0),
                    VSConfig.gravity().getProduct(physTickMass * getPhysicsTimeDeltaPerPhysTick()));
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

        Vector idealAngularVelocity = new Vector(idealAxisAngle.x, idealAxisAngle.y, idealAxisAngle.z);
        idealAngularVelocity.multiply(idealAngularVelocityMultiple);

        Vector angularVelocityDif = idealAngularVelocity
                .getSubtraction(getAngularVelocity());
        // Larger values converge faster, but sacrifice collision accuracy
        angularVelocityDif.multiply(.01);

        getAngularVelocity().subtract(angularVelocityDif);
    }

    private void applyAirDrag() {
        double drag = getDragForPhysTick();
        getLinearMomentum().multiply(drag);
        getAngularVelocity().multiply(drag);
    }

    public void convertTorqueToVelocity() {
        if (!torque.isZero()) {
            Vector3d torqueTransformed = torque.toVector3d();
            getPhysInvMOITensor().transform(torqueTransformed);
            getAngularVelocity().add(torqueTransformed.x, torqueTransformed.y, torqueTransformed.z);
            torque.zero();
        }
    }

    public void addForceAtPoint(Vector inBodyWO,
                                Vector forceToApply) {
        torque.add(inBodyWO.cross(forceToApply));
        getLinearMomentum().add(forceToApply);
    }

    public void addForceAtPoint(Vector inBodyWO,
                                Vector forceToApply,
                                Vector crossVector) {
        crossVector.setCross(inBodyWO, forceToApply);
        torque.add(crossVector);
        getLinearMomentum().add(forceToApply);
    }

    public void updatePhysSpeedAndIters(double newPhysSpeed) {
        physTickTimeDelta = newPhysSpeed;
    }

    /**
     * This may or may not be correct :/ It seems to work fine but quaternion math is such a headache I'll take whatever works.
     */
    private void integrateAngularVelocity() {
        ShipTransformationManager coordTrans = getParent().getShipTransformationManager();
        Vector angularVelocity = getAngularVelocity();

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

        physX += (getLinearMomentum().x * momentMod);
        physY += (getLinearMomentum().y * momentMod);
        physZ += (getLinearMomentum().z * momentMod);
        physY = Math.min(Math.max(physY, VSConfig.shipLowerLimit), VSConfig.shipUpperLimit);
    }

    public Vector getVelocityAtPoint(
            Vector inBodyWO) {
        Vector speed = getAngularVelocity().cross(inBodyWO);
        double invMass = getInvMass();
        speed.x += (getLinearMomentum().x * invMass);
        speed.y += (getLinearMomentum().y * invMass);
        speed.z += (getLinearMomentum().z * invMass);
        return speed;
    }

    // These getter methods guarantee that only code within this class can modify
    // the mass, preventing outside code from breaking things
    public double getMass() {
        return physTickMass;
    }

    public double getInvMass() {
        return 1D / physTickMass;
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
        Vector3d rotationAxis = new Vector(
            getAngularVelocity()).toVector3d();
        rotationAxis.normalize();
        getPhysMOITensor().transform(rotationAxis);
        return rotationAxis.length();
    }

    @Deprecated
    public Vector getCopyOfPhysCoordinates() {
        return new Vector(physX, physY, physZ);
    }

    public IRotationNodeWorld getPhysicsRotationNodeWorld() {
        return physicsRotationNodeWorld;
    }
}
