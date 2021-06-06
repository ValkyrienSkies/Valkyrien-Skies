package org.valkyrienskies.mod.common.collision;

import gnu.trove.list.TIntList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import org.valkyrienskies.mod.common.ships.block_relocation.SpatialDetector;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.datastructures.IBitOctree;
import org.valkyrienskies.mod.common.util.datastructures.ITerrainOctreeProvider;
import valkyrienwarfare.api.TransformType;

import java.util.concurrent.Callable;

public class WaterForcesTask implements Callable<Void> {

    public final static int MAX_TASKS_TO_CHECK = 500;

    // The radius of the sphere that represents each water block in meters.
    private static final double SPHERE_RADIUS = .5;
    // Acceleration in m/s^2
    private static final double GRAVITY_ACCELERATION = 9.8;
    // Density in kg/m^3
    public static double DENSITY_OF_WATER = 1000;
    // Dimensionless constant. Higher values mean more damping force from water.
    private static final double DRAG_COEFFICIENT_OF_WATER = .3;
    // The radius, in meters, of the AABB of water blocks and terrain blocks.
    private static final double AABB_RADIUS = .5;

    private final PhysicsObject parent;
    private final BlockPos colliderCenter;
    private final TIntList waterHitsToCheck;
    private final int minHitIndex, maxHitIndex;
    private final Vector3d addedForce, addedTorque;

    public WaterForcesTask(final PhysicsObject parent, final BlockPos colliderCenter, final TIntList waterHitsToCheck,
                           final int minHitIndex, final int maxHitIndex) {
        this.parent = parent;
        this.colliderCenter = colliderCenter;
        this.waterHitsToCheck = waterHitsToCheck;
        this.minHitIndex = minHitIndex;
        this.maxHitIndex = maxHitIndex;
        this.addedForce = new Vector3d();
        this.addedTorque = new Vector3d();
    }

    /**
     * Adds the computed force and torque to the parent ship
     */
    public void addForcesToShip() {
        final PhysicsCalculations physicsEngine = parent.getPhysicsCalculations();
        physicsEngine.addForceAndTorque(addedForce, addedTorque);
    }

    /**
     * Computes the force and torque resulting from the water collisions handled by this task.
     */
    @Override
    public Void call() {
        final BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
        final ShipTransform physicsTransform = parent.getShipTransformationManager().getCurrentPhysicsTransform();
        final PhysicsCalculations physicsEngine = parent.getPhysicsCalculations();

        // Vector objects reused in this method.
        final Vector3d temp0 = new Vector3d();
        final Vector3d temp1 = new Vector3d();
        final Vector3d temp2 = new Vector3d();
        final Vector3d temp3 = new Vector3d();
        final Vector3d temp4 = new Vector3d();
        final Vector3d temp5 = new Vector3d();
        final Vector3d temp6 = new Vector3d();
        final Vector3d temp7 = new Vector3d();
        final Vector3d temp8 = new Vector3d();
        final Vector3d temp9 = new Vector3d();

        for (int index = minHitIndex; index <= maxHitIndex; index++) {
            final int waterHitPosHash = waterHitsToCheck.get(index);
            SpatialDetector.setPosWithRespectTo(waterHitPosHash, colliderCenter, currentPos);

            final Vector3dc waterPosInShipSpace = physicsTransform.transformPositionNew(JOML.convertDouble(currentPos, temp0).add(.5, .5, .5), TransformType.GLOBAL_TO_SUBSPACE);

            final int minX = (int) Math.floor(waterPosInShipSpace.x() - .5);
            final int minY = (int) Math.floor(waterPosInShipSpace.y() - .5);
            final int minZ = (int) Math.floor(waterPosInShipSpace.z() - .5);

            final int maxX = (int) Math.ceil(waterPosInShipSpace.x() + .5);
            final int maxY = (int) Math.ceil(waterPosInShipSpace.y() + .5);
            final int maxZ = (int) Math.ceil(waterPosInShipSpace.z() + .5);

            final Vector3dc waterPosInWorld = JOML.convertDouble(currentPos, temp1).add(.5, .5, .5);

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    final Chunk chunk = parent.getChunkClaim().containsChunk(x >> 4, z >> 4) ? parent.getChunkAt(x >> 4, z >> 4) : null;
                    if (chunk == null)
                        continue;
                    for (int y = minY; y <= maxY; y++) {
                        final ExtendedBlockStorage blockStorage = chunk.storageArrays[y >> 4];
                        if (blockStorage != null) {
                            final IBitOctree solidBlockOctree = ((ITerrainOctreeProvider) blockStorage.data).getSolidOctree();
                            final IBitOctree airPocketBlockOctree = ((ITerrainOctreeProvider) blockStorage.data).getSolidOctree();
                            // Check if liquid is colliding with a solid block or an air pocket block
                            if (solidBlockOctree.get(x & 15, y & 15, z & 15) || airPocketBlockOctree.get(x & 15, y & 15, z & 15)) {
                                // Assume both the water block and terrain block are spheres, then compute the volume
                                // that overlaps
                                final Vector3dc shipSolidBlockPosInWorld = physicsTransform.transformPositionNew(temp2.set(x + .5, y + .5, z + .5), TransformType.SUBSPACE_TO_GLOBAL);

                                final double volumeDisplaced = calculateAABBOverlap(
                                        waterPosInWorld.x() - shipSolidBlockPosInWorld.x(),
                                        waterPosInWorld.y() - shipSolidBlockPosInWorld.y(),
                                        waterPosInWorld.z() - shipSolidBlockPosInWorld.z()
                                );

                                if (volumeDisplaced <= 0) {
                                    // No intersection
                                    continue;
                                }

                                // Collision position is average of ship solid block pos and water pos
                                final Vector3dc collisionPosInWorld = shipSolidBlockPosInWorld.add(waterPosInWorld, temp3).mul(.5);

                                final Vector3dc buoyancyForce = temp4.set(0, volumeDisplaced * GRAVITY_ACCELERATION * DENSITY_OF_WATER, 0);
                                final Vector3dc collisionPosRelativeToShipCenterInWorld = temp5.set(collisionPosInWorld).sub(physicsTransform.getPosX(), physicsTransform.getPosY(), physicsTransform.getPosZ());

                                addForceAtPoint(collisionPosRelativeToShipCenterInWorld, buoyancyForce, temp7);

                                {
                                    // Compute water damping force
                                    final Vector3dc velocity = physicsEngine.getVelocityAtPoint(collisionPosRelativeToShipCenterInWorld, temp9);

                                    if (!isVectorLengthZero(velocity)) {
                                        // TODO: This is WRONG, but it'll do for now
                                        // The distance between the water block and the solid block its pushing upwards
                                        double distance = waterPosInWorld.distance(shipSolidBlockPosInWorld);
                                        final double area = Math.PI * (SPHERE_RADIUS - (distance * .5)) * (SPHERE_RADIUS - (distance * .5));
                                        final double velocitySquared = velocity.lengthSquared();

                                        // Drag formula from https://en.wikipedia.org/wiki/Drag_(physics)
                                        final double forceMagnitude = (.5) * DENSITY_OF_WATER * velocitySquared * DRAG_COEFFICIENT_OF_WATER * area;

                                        final Vector3dc dragForce = temp6.set(velocity).normalize().mul(-forceMagnitude);

                                        addForceAtPoint(collisionPosRelativeToShipCenterInWorld, dragForce, temp8);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private void addForceAtPoint(Vector3dc posRelToShipCenter,
                                 Vector3dc forceToApply,
                                 Vector3d tempStorage) {
        final Vector3dc torqueFromForce = posRelToShipCenter.cross(forceToApply, tempStorage);
        addedForce.add(forceToApply);
        addedTorque.add(torqueFromForce);
    }

    /**
     * Computes the volume of the overlap of two AABB with radius {@link #AABB_RADIUS}.
     * @param xOffset The offset between the two AABBs in the x direction
     * @param yOffset The offset between the two AABBs in the y direction
     * @param zOffset The offset between the two AABBs in the z direction
     * @return The volume of overlap between both AABBs
     */
    private static double calculateAABBOverlap(double xOffset, double yOffset, double zOffset) {
        xOffset = Math.abs(xOffset);
        yOffset = Math.abs(yOffset);
        zOffset = Math.abs(zOffset);
        if (xOffset >= AABB_RADIUS * 2 || yOffset >= AABB_RADIUS * 2 || zOffset >= AABB_RADIUS * 2) {
            return 0;
        }
        return (AABB_RADIUS * 2 - xOffset) * (AABB_RADIUS * 2 - yOffset) * (AABB_RADIUS * 2 - zOffset);
    }

    private static final double VECTOR_LENGTH_SQUARED_ZERO_THRESHOLD = .01;

    private static boolean isVectorLengthZero(Vector3dc vector) {
        return vector.lengthSquared() < VECTOR_LENGTH_SQUARED_ZERO_THRESHOLD;
    }
}
