package org.valkyrienskies.mod.common.collision;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import org.valkyrienskies.mod.common.ships.block_relocation.SpatialDetector;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.datastructures.IBitOctree;
import org.valkyrienskies.mod.common.util.datastructures.ITerrainOctreeProvider;
import valkyrienwarfare.api.TransformType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bote
 */
public class WorldWaterCollider {

    // Used to expand the AABB used to check for potential collisions; helps prevent
    // ships ghosting through blocks
    public static final double AABB_EXPANSION = 2D;
    // The range we check for possible collisions with a block.
    public static final double RANGE_CHECK = 1.8D;
    // The minimum depth a collision projection must have, to not use the default
    // collision normal of <0, 1, 0>
    public static final double AXIS_TOLERANCE = .3D;
    // Time in seconds between collision cache updates. A value of .1D means we
    // update the collision cache every 1/10th of a second.
    public static final double CACHE_UPDATE_FREQUENCY = .075D;

    private final PhysicsCalculations calculator;
    private final PhysicsObject parent;
    private final TIntList cachedPotentialHits;
    private final TIntArrayList cachedHitsToRemove;
    // Ensures this always updates the first tick after creation
    private double ticksSinceCacheUpdate;
    private BlockPos centerPotentialHit;

    // The radius of the sphere that represents each water block in meters.
    public static final double SPHERE_RADIUS = .5;
    // Acceleration in m/s^2
    public static final double GRAVITY_ACCELERATION = 9.8;
    // Mass in kg
    public static final double MASS_OF_CUBIC_METER_OF_WATER = 1000;

    public WorldWaterCollider(PhysicsCalculations calculations) {
        this.calculator = calculations;
        this.parent = calculations.getParent();
        this.cachedPotentialHits = new TIntArrayList();
        this.cachedHitsToRemove = new TIntArrayList();
        this.ticksSinceCacheUpdate = 25D;
        this.centerPotentialHit = null;
    }

    public void tickUpdatingTheCollisionCache() {
        // Multiply by 20 to convert seconds (physTickSpeed) into ticks
        ticksSinceCacheUpdate += calculator.getPhysicsTimeDeltaPerPhysTick();
        for (int i = 0; i < cachedHitsToRemove.size(); i++) {
            cachedPotentialHits.remove(cachedHitsToRemove.get(i));
        }
        cachedHitsToRemove.resetQuick();
        if (ticksSinceCacheUpdate > CACHE_UPDATE_FREQUENCY || parent
            .isNeedsCollisionCacheUpdate()) {
            updatePotentialCollisionCache();
        }
    }

    public void addBuoyancyForces() {
        final MutableBlockPos currentPos = new MutableBlockPos();
        final ShipTransform physicsTransform = parent.getShipTransformationManager().getCurrentPhysicsTransform();
        for (int i = 0; i < cachedPotentialHits.size(); i++) {
            final int hash = cachedPotentialHits.get(i);
            SpatialDetector.setPosWithRespectTo(hash, centerPotentialHit, currentPos);

            final Vector3d inLocal = JOML.convertDouble(currentPos).add(.5, .5, .5);
            physicsTransform.transformPosition(inLocal, TransformType.GLOBAL_TO_SUBSPACE);

            final int minX = (int) Math.floor(inLocal.x() - .5);
            final int minY = (int) Math.floor(inLocal.y() - .5);
            final int minZ = (int) Math.floor(inLocal.z() - .5);

            final int maxX = (int) Math.ceil(inLocal.x() + .5);
            final int maxY = (int) Math.ceil(inLocal.y() + .5);
            final int maxZ = (int) Math.ceil(inLocal.z() + .5);

            Vector3d waterPosInWorld = JOML.convertDouble(currentPos).add(.5, .5, .5);

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    final Chunk chunk = parent.getChunkClaim().containsChunk(x >> 4, z >> 4) ? parent.getChunkAt(x >> 4, z >> 4) : null;
                    if (chunk == null)
                        continue;
                    for (int y = minY; y <= maxY; y++) {
                        final ExtendedBlockStorage blockStorage = chunk.storageArrays[y >> 4];
                        if (blockStorage != null) {
                            final IBitOctree terrainOctree = ((ITerrainOctreeProvider) blockStorage.data).getSolidOctree();
                            if (terrainOctree.get(x & 15, y & 15, z & 15)) {
                                // Assume both the water block and terrain block are spheres, then compute the volume
                                // that overlaps
                                Vector3d terrainPos = new Vector3d(x + .5, y + .5, z + .5);
                                physicsTransform.transformPosition(terrainPos, TransformType.SUBSPACE_TO_GLOBAL);

                                final double volumeDisplaced = calculateAABBOverlap(
                                        waterPosInWorld.x() - terrainPos.x(),
                                        waterPosInWorld.y() - terrainPos.y(),
                                        waterPosInWorld.z() - terrainPos.z()
                                );

                                if (volumeDisplaced <= 0) {
                                    // No intersection
                                    continue;
                                }

                                // The distance between the water block and the solid block its pushing upwards
                                double distance = waterPosInWorld.distance(terrainPos);

                                // Collision position is average of terrain pos and water pos
                                Vector3d collisionPos = terrainPos.add(waterPosInWorld, new Vector3d()).mul(.5);

                                Vector3d collisionImpulseForce = new Vector3d(0, GRAVITY_ACCELERATION * MASS_OF_CUBIC_METER_OF_WATER * volumeDisplaced * calculator.getPhysicsTimeDeltaPerPhysTick(), 0);
                                Vector3d inBody = new Vector3d(collisionPos)
                                        .sub(physicsTransform.getPosX(), physicsTransform.getPosY(), physicsTransform.getPosZ());

                                calculator.addForceAtPoint(inBody, collisionImpulseForce);

                                {
                                    // Compute water damping force
                                    final Vector3dc velocity = calculator.getVelocityAtPoint(inBody);

                                    if (velocity.lengthSquared() > .01) {
                                        final double density = 1000;
                                        final double dragCoefficient = .3;
                                        // TODO: This is WRONG, but it'll do for now
                                        final double area = Math.PI * (SPHERE_RADIUS - (distance * .5)) * (SPHERE_RADIUS - (distance * .5));
                                        final double velocitySquared = velocity.lengthSquared();

                                        // Drag formula from https://en.wikipedia.org/wiki/Drag_(physics)
                                        final double forceMagnitude = (.5) * density * velocitySquared * dragCoefficient * area;

                                        final Vector3d dragForce = new Vector3d(velocity).normalize().mul(-forceMagnitude * calculator.getPhysicsTimeDeltaPerPhysTick());

                                        calculator.addForceAtPoint(inBody, dragForce);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Computes the volume of the overlap of two spheres with radius @link{#SPHERE_RADIUS}.
     *
     * Given that both spheres are the same size, then the intersection volume is just the 2 * sphere cap volume.
     *
     * Uses sphere cap equation from https://en.wikipedia.org/wiki/Spherical_cap
     */
    private static double calculateVolumeOfSphereIntersection(final double distance) {
        double sphereCapHeight = (distance / 2) - SPHERE_RADIUS;
        double sphereCapVolume = (Math.PI / 3.0) * (sphereCapHeight * sphereCapHeight) * (3 * SPHERE_RADIUS - sphereCapHeight);

        return 2 * sphereCapVolume;
    }

    private static final double AABB_RADIUS = .5;

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

    private void updatePotentialCollisionCache() {
        ShipTransform currentPhysicsTransform = parent
            .getShipTransformationManager()
            .getCurrentPhysicsTransform();

        AxisAlignedBB shipBB = parent.getShipBB().grow(3);

        // Use the physics tick collision box instead of the game tick collision box.
        // We are using grow(3) on both because for some reason if we don't then ships start
        // jiggling through the ground. God I can't wait for a new physics engine.
        final AxisAlignedBB collisionBB = shipBB
            .grow(AABB_EXPANSION).expand(
                calculator.getLinearVelocity().x * calculator.getPhysicsTimeDeltaPerPhysTick() * 5,
                calculator.getLinearVelocity().y * calculator.getPhysicsTimeDeltaPerPhysTick() * 5,
                calculator.getLinearVelocity().z * calculator.getPhysicsTimeDeltaPerPhysTick() * 5);

        ticksSinceCacheUpdate = 0D;
        // This is being used to occasionally offset the collision cache update, in the
        // hopes this will prevent multiple ships from all updating
        // in the same tick
        if (Math.random() > .5) {
            ticksSinceCacheUpdate -= .05D;
        }
        int oldSize = cachedPotentialHits.size();
        // Resets the potential hits array in O(1) time! Isn't that something.
        // cachedPotentialHits.resetQuick();
        cachedPotentialHits.clear();
        // Ship is outside of world blockSpace, just skip this all togvalkyrium
        if (collisionBB.maxY < 0 || collisionBB.minY > 255) {
            return;
        }

        // Has a -1 on the minY value, I hope this helps with preventing things from
        // falling through the floor
        BlockPos min = new BlockPos(collisionBB.minX, Math.max(collisionBB.minY - 1, 0),
            collisionBB.minZ);
        BlockPos max = new BlockPos(collisionBB.maxX, Math.min(collisionBB.maxY, 255),
            collisionBB.maxZ);
        centerPotentialHit = new BlockPos((min.getX() + max.getX()) / 2D,
            (min.getY() + max.getY()) / 2D,
            (min.getZ() + max.getZ()) / 2D);

        ChunkCache cache = parent.getCachedSurroundingChunks();

        if (cache == null) {
            System.err.println(
                "VS Cached Surrounding Chunks was null! This is going to cause catastophric terrible events!!");
            return;
        }

        int chunkMinX = min.getX() >> 4;
        int chunkMaxX = (max.getX() >> 4) + 1;
        int chunkMinZ = min.getZ() >> 4;
        int chunkMaxZ = (max.getZ() >> 4) + 1;

        int minX = min.getX();
        int minY = min.getY();
        int minZ = min.getZ();
        int maxX = max.getX();
        int maxY = max.getY();
        int maxZ = max.getZ();

        // More multithreading!
        if (VSConfig.MULTITHREADING_SETTINGS.multithreadCollisionCacheUpdate &&
            parent.getBlockPositions().size() > 100) {

            List<Triple<Integer, Integer, TIntList>> tasks = new ArrayList<>();

            for (int chunkX = chunkMinX; chunkX < chunkMaxX; chunkX++) {
                for (int chunkZ = chunkMinZ; chunkZ < chunkMaxZ; chunkZ++) {
                    tasks.add(new ImmutableTriple<>(chunkX, chunkZ, new TIntArrayList()));
                }
            }

            Consumer<Triple<Integer, Integer, TIntList>> consumer = i -> { // i is a Tuple<Integer, Integer>
                // updateCollisionCacheParrallel(cache, cachedPotentialHits, i.getFirst(),
                // i.getSecond(), minX, minY, minZ, maxX, maxY, maxZ);
                updateCollisionCacheSequential(cache, i.getLeft(), i.getMiddle(), minX, minY, minZ,
                    maxX, maxY, maxZ,
                    shipBB, i.getRight());
            };
            ValkyrienSkiesMod.getPhysicsThreadPool().submit(
                () -> tasks.parallelStream().forEach(consumer))
            .join();

            tasks.forEach(task -> cachedPotentialHits.addAll(task.getRight()));
        } else {
            // Cast to double to avoid overflow errors
            double size = ((double) (chunkMaxX - chunkMinX)) * ((double) (chunkMaxZ - chunkMinZ));
            if (size > 300000) {
                // Sanity check; don't execute the rest of the code because we'll just freeze the physics thread.
                return;
            }
            // TODO: VS thread freezes here.
            for (int chunkX = chunkMinX; chunkX < chunkMaxX; chunkX++) {
                for (int chunkZ = chunkMinZ; chunkZ < chunkMaxZ; chunkZ++) {
                    updateCollisionCacheSequential(cache, chunkX, chunkZ, minX, minY, minZ, maxX,
                        maxY, maxZ, shipBB, cachedPotentialHits);
                }
            }
        }
    }

    private void updateCollisionCacheSequential(ChunkCache cache, int chunkX, int chunkZ, int minX,
        int minY, int minZ,
        int maxX, int maxY, int maxZ, AxisAlignedBB shipBB, TIntList output) {
        int arrayChunkX = chunkX - cache.chunkX;
        int arrayChunkZ = chunkZ - cache.chunkZ;

        if (!(arrayChunkX < 0 || arrayChunkZ < 0 || arrayChunkX > cache.chunkArray.length - 1
            || arrayChunkZ > cache.chunkArray[0].length - 1)
            && cache.chunkArray[arrayChunkX][arrayChunkZ] != null) {

            Vector3d temp1 = new Vector3d();
            Vector3d temp2 = new Vector3d();
            Vector3d temp3 = new Vector3d();

            Chunk chunk = cache.chunkArray[arrayChunkX][arrayChunkZ];
            for (int storageY = minY >> 4; storageY <= maxY >> 4; storageY++) {
                ExtendedBlockStorage extendedblockstorage = chunk.storageArrays[storageY];
                if (extendedblockstorage != null) {
                    int minStorageX = chunkX << 4;
                    int minStorageY = storageY << 4;
                    int minStorageZ = chunkZ << 4;

                    int maxStorageX = minStorageX + 16;
                    int maxStorageY = minStorageY + 16;
                    int maxStorageZ = minStorageZ + 16;

                    ITerrainOctreeProvider provider = (ITerrainOctreeProvider) extendedblockstorage.data;
                    IBitOctree octree = provider.getLiquidOctree();
                    for (int x = minStorageX; x < maxStorageX; x++) {
                        for (int y = minStorageY; y < maxStorageY; y++) {
                            for (int z = minStorageZ; z < maxStorageZ; z++) {
                                checkForCollision(x, y, z, extendedblockstorage, octree, temp1,
                                    temp2, temp3,
                                    shipBB, output);
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkForCollision(int x, int y, int z, ExtendedBlockStorage storage,
        IBitOctree octree, Vector3d inLocal,
        Vector3d inBody,
        Vector3d speedInBody, AxisAlignedBB shipBB, TIntList output) {
        if (octree.get(x & 15, y & 15, z & 15)) {
            inLocal.x = x + .5D;
            inLocal.y = y + .5D;
            inLocal.z = z + .5D;
            // TODO: Something
            // parent.coordTransform.fromGlobalToLocal(inLocal);
            if (inLocal.x > shipBB.minX && inLocal.x < shipBB.maxX && inLocal.y > shipBB.minY
                && inLocal.y < shipBB.maxY
                && inLocal.z > shipBB.minZ && inLocal.z < shipBB.maxZ) {
                parent.getShipTransformationManager().getCurrentPhysicsTransform()
                    .transformPosition(inLocal, TransformType.GLOBAL_TO_SUBSPACE);

                inLocal.sub(parent.getCenterCoord(), inBody);
                // parent.physicsProcessor.setVectorToVelocityAtPoint(inBody, speedInBody);
                // speedInBody.multiply(-parent.physicsProcessor.getPhysicsTimeDeltaPerGameTick());

                // TODO: This isnt ideal, but we do gain a lot of performance.
                speedInBody.zero();

                // double RANGE_CHECK = 1;

                int minX, minY, minZ, maxX, maxY, maxZ;
                if (speedInBody.x > 0) {
                    minX = MathHelper.floor(inLocal.x - RANGE_CHECK);
                    maxX = MathHelper.floor(inLocal.x + RANGE_CHECK + speedInBody.x);
                } else {
                    minX = MathHelper.floor(inLocal.x - RANGE_CHECK + speedInBody.x);
                    maxX = MathHelper.floor(inLocal.x + RANGE_CHECK);
                }

                if (speedInBody.y > 0) {
                    minY = MathHelper.floor(inLocal.y - RANGE_CHECK);
                    maxY = MathHelper.floor(inLocal.y + RANGE_CHECK + speedInBody.y);
                } else {
                    minY = MathHelper.floor(inLocal.y - RANGE_CHECK + speedInBody.y);
                    maxY = MathHelper.floor(inLocal.y + RANGE_CHECK);
                }

                if (speedInBody.z > 0) {
                    minZ = MathHelper.floor(inLocal.z - RANGE_CHECK);
                    maxZ = MathHelper.floor(inLocal.z + RANGE_CHECK + speedInBody.z);
                } else {
                    minZ = MathHelper.floor(inLocal.z - RANGE_CHECK + speedInBody.z);
                    maxZ = MathHelper.floor(inLocal.z + RANGE_CHECK);
                }

                minY = Math.min(255, Math.max(minY, 0));
                maxY = Math.min(255, Math.max(maxY, 0));

                // int localX = MathHelper.floor(inLocal.X);
                // int localY = MathHelper.floor(inLocal.Y);
                // int localZ = MathHelper.floor(inLocal.Z);

                // tooTiredToName(localX, localY, localZ, x, y, z);
                // if (false)
                // maxX = Math.min(maxX, minX << 4);
                // maxZ = Math.min(maxZ, minZ << 4);

                Chunk chunkIn00 = parent.getChunkClaim().containsChunk(minX >> 4, minZ >> 4) ? parent.getChunkAt(minX >> 4, minZ >> 4) : null;
                Chunk chunkIn01 = parent.getChunkClaim().containsChunk(minX >> 4, maxZ >> 4) ? parent.getChunkAt(minX >> 4, maxZ >> 4) : null;
                Chunk chunkIn10 = parent.getChunkClaim().containsChunk(maxX >> 4, minZ >> 4) ? parent.getChunkAt(maxX >> 4, minZ >> 4) : null;
                Chunk chunkIn11 = parent.getChunkClaim().containsChunk(maxX >> 4, maxZ >> 4) ? parent.getChunkAt(maxX >> 4, maxZ >> 4) : null;

                breakThisLoop:
                for (int localX = minX; localX < maxX; localX++) {
                    for (int localZ = minZ; localZ < maxZ; localZ++) {
                        Chunk theChunk;
                        if (localX >> 4 == minX >> 4) {
                            if (localZ >> 4 == minZ >> 4) {
                                theChunk = chunkIn00;
                            } else {
                                theChunk = chunkIn01;
                            }
                        } else {
                            if (localZ >> 4 == minZ >> 4) {
                                theChunk = chunkIn10;
                            } else {
                                theChunk = chunkIn11;
                            }
                        }
                        if (theChunk == null) {
                            // No collision here
                            continue;
                        }
                        for (int localY = minY; localY < maxY; localY++) {
                            boolean result = checkForCollisionFast(theChunk, localX, localY,
                                localZ, x, y, z, output);
                            if (result) {
                                break breakThisLoop;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkForCollisionFast(final Chunk chunk, final int localX, final int localY,
        final int localZ,
        final int x, final int y, final int z, final TIntList output) {
        if (chunk.storageArrays[localY >> 4] != null) {
            ITerrainOctreeProvider provider = (ITerrainOctreeProvider) chunk.storageArrays[localY >> 4]
                .getData();
            IBitOctree octreeInLocal = provider.getSolidOctree();
            if (octreeInLocal.get(localX & 15, localY & 15, localZ & 15)) {
                int hash = SpatialDetector.getHashWithRespectTo(x, y, z, centerPotentialHit);
                // Sometimes we end up adding to the hits array in multiple threads at once,
                // crashing the physics.
                output.add(hash);
                return true;
            }
        }
        return false;
    }

}
