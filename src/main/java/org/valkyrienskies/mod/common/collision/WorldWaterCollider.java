package org.valkyrienskies.mod.common.collision;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import net.minecraft.block.state.IBlockState;
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
import org.valkyrienskies.mod.common.util.VSIterationUtils;
import org.valkyrienskies.mod.common.util.datastructures.IBitOctree;
import org.valkyrienskies.mod.common.util.datastructures.ITerrainOctreeProvider;
import valkyrienwarfare.api.TransformType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * This collider adds the water buoyancy and drag forces to a ship.
 */
public class WorldWaterCollider {

    // Used to expand the AABB used to check for potential collisions; helps prevent
    // ships ghosting through blocks
    private static final double AABB_EXPANSION = 2;
    // The radius of the range we check when considering adding a water block to the collision cache
    private static final double RANGE_CHECK = 2;
    // Time in seconds between collision cache updates. A value of .1D means we
    // update the collision cache every 1/10th of a second.
    private static final double CACHE_UPDATE_PERIOD = .5;

    private final PhysicsCalculations calculator;
    private final PhysicsObject parent;
    private final TIntList cachedPotentialHits;
    private double secondsSinceCollisionCacheUpdate;
    private BlockPos centerPotentialHit;



    public WorldWaterCollider(PhysicsCalculations calculations) {
        this.calculator = calculations;
        this.parent = calculations.getParent();
        this.cachedPotentialHits = new TIntArrayList();
        this.secondsSinceCollisionCacheUpdate = 2500; // Any number large than CACHE_UPDATE_PERIOD works
        this.centerPotentialHit = null;
    }

    public void tickUpdatingTheCollisionCache() {
        secondsSinceCollisionCacheUpdate += calculator.getPhysicsTimeDeltaPerPhysTick();
        if (secondsSinceCollisionCacheUpdate > CACHE_UPDATE_PERIOD) {
            updatePotentialCollisionCache();
        }
    }

    /**
     * Adds the water buoyancy and water drag forces to the ship.
     */
    public void addBuoyancyForces() {
        final int minHitIndex = 0;
        final int maxHitIndex = cachedPotentialHits.size() - 1;
        WaterForcesTask waterForcesTask = new WaterForcesTask(parent, centerPotentialHit, cachedPotentialHits, minHitIndex, maxHitIndex);
        waterForcesTask.call();

        final PhysicsCalculations physicsEngine = parent.getPhysicsCalculations();
        physicsEngine.addForceAndTorque(waterForcesTask.getAddForce(), waterForcesTask.getAddedTorque());
    }

    private void updatePotentialCollisionCache() {
        // We are using grow(3) because its good.
        AxisAlignedBB shipBB = parent.getShipBB().grow(3);

        final AxisAlignedBB collisionBB = shipBB
            .grow(AABB_EXPANSION).grow(2 * Math.ceil(RANGE_CHECK));

        secondsSinceCollisionCacheUpdate = 0;
        // This is being used to occasionally offset the collision cache update, in the hopes this will prevent multiple
        // ships from all updating in the same tick
        if (Math.random() > .5) {
            secondsSinceCollisionCacheUpdate -= .01;
        }

        cachedPotentialHits.clear();
        // Ship is outside of world blockSpace, just skip this
        if (collisionBB.maxY < 0 || collisionBB.minY > 255) {
            return;
        }

        // Has a -1 on the minY value, I hope this helps with preventing things from
        // falling through the floor
        final BlockPos min = new BlockPos(collisionBB.minX, Math.max(collisionBB.minY - 1, 0),
            collisionBB.minZ);
        final BlockPos max = new BlockPos(collisionBB.maxX, Math.min(collisionBB.maxY, 255),
            collisionBB.maxZ);
        centerPotentialHit = new BlockPos((min.getX() + max.getX()) / 2.0,
            (min.getY() + max.getY()) / 2.0,
            (min.getZ() + max.getZ()) / 2.0);

        final ChunkCache cache = parent.getCachedSurroundingChunks();

        if (cache == null) {
            System.err.println(
                "VS Cached Surrounding Chunks was null! This is going to cause catastophric terrible events!!");
            return;
        }

        final int chunkMinX = min.getX() >> 4;
        final int chunkMaxX = (max.getX() >> 4) + 1;
        final int chunkMinZ = min.getZ() >> 4;
        final int chunkMaxZ = (max.getZ() >> 4) + 1;

        final int minX = min.getX();
        final int minY = min.getY();
        final int minZ = min.getZ();
        final int maxX = max.getX();
        final int maxY = max.getY();
        final int maxZ = max.getZ();

        // More multithreading!
        if (VSConfig.MULTITHREADING_SETTINGS.multithreadCollisionCacheUpdate &&
            parent.getBlockPositions().size() > 100) {

            final List<Triple<Integer, Integer, TIntList>> tasks = new ArrayList<>();

            for (int chunkX = chunkMinX; chunkX < chunkMaxX; chunkX++) {
                for (int chunkZ = chunkMinZ; chunkZ < chunkMaxZ; chunkZ++) {
                    tasks.add(new ImmutableTriple<>(chunkX, chunkZ, new TIntArrayList()));
                }
            }

            Consumer<Triple<Integer, Integer, TIntList>> consumer = i -> updateCollisionCacheSequential(cache, i.getLeft(), i.getMiddle(), minX, minY, minZ,
                maxX, maxY, maxZ,
                shipBB, i.getRight());
            ValkyrienSkiesMod.getPhysicsThreadPool().submit(
                () -> tasks.parallelStream().forEach(consumer))
            .join();

            tasks.forEach(task -> cachedPotentialHits.addAll(task.getRight()));
        } else {
            // Cast to double to avoid overflow errors
            final double size = ((double) (chunkMaxX - chunkMinX)) * ((double) (chunkMaxZ - chunkMinZ));
            if (size > 300000) {
                // Sanity check; don't execute the rest of the code because we'll just freeze the physics thread.
                return;
            }
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

            final Vector3d temp1 = new Vector3d();
            final Vector3d temp2 = new Vector3d();

            Chunk chunk = cache.chunkArray[arrayChunkX][arrayChunkZ];
            for (int storageY = minY >> 4; storageY <= maxY >> 4; storageY++) {
                ExtendedBlockStorage extendedblockstorage = chunk.storageArrays[storageY];
                if (extendedblockstorage != null) {
                    int minStorageX = chunkX << 4;
                    int minStorageY = storageY << 4;
                    int minStorageZ = chunkZ << 4;

                    int maxStorageX = minStorageX + 15;
                    int maxStorageY = Math.min(maxY, minStorageY + 15);
                    int maxStorageZ = minStorageZ + 15;

                    ITerrainOctreeProvider provider = (ITerrainOctreeProvider) extendedblockstorage.data;
                    IBitOctree octree = provider.getLiquidOctree();
                    for (int x = minStorageX; x <= maxStorageX; x++) {
                        for (int y = minStorageY; y <= maxStorageY; y++) {
                            for (int z = minStorageZ; z <= maxStorageZ; z++) {
                                checkIfCollidesWithinRangeCheckRadius(x, y, z, octree, temp1, temp2, shipBB, output);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * If there is a block within a radius of {@link #RANGE_CHECK}
     */
    private void checkIfCollidesWithinRangeCheckRadius(final int x, final int y, final int z,
                                                       final IBitOctree octree, final Vector3d inLocal, final Vector3d inBody,
                                                       final AxisAlignedBB shipBB, final TIntList output) {
        if (octree.get(x & 15, y & 15, z & 15)) {
            inLocal.x = x + .5;
            inLocal.y = y + .5;
            inLocal.z = z + .5;

            if (inLocal.x > shipBB.minX && inLocal.x < shipBB.maxX && inLocal.y > shipBB.minY
                && inLocal.y < shipBB.maxY
                && inLocal.z > shipBB.minZ && inLocal.z < shipBB.maxZ) {
                parent.getShipTransformationManager().getCurrentPhysicsTransform()
                    .transformPosition(inLocal, TransformType.GLOBAL_TO_SUBSPACE);

                inLocal.sub(parent.getCenterCoord(), inBody);

                int minX, minY, minZ, maxX, maxY, maxZ;

                minX = MathHelper.floor(inLocal.x - RANGE_CHECK);
                maxX = MathHelper.floor(inLocal.x + RANGE_CHECK);

                minY = MathHelper.floor(inLocal.y - RANGE_CHECK);
                maxY = MathHelper.floor(inLocal.y + RANGE_CHECK);

                minZ = MathHelper.floor(inLocal.z - RANGE_CHECK);
                maxZ = MathHelper.floor(inLocal.z + RANGE_CHECK);


                minY = Math.min(255, Math.max(minY, 0));
                maxY = Math.min(255, Math.max(maxY, 0));

                // TODO: This loop is crap. Come up with a better way. Please :(
                final Chunk chunkIn00 = parent.getChunkClaim().containsChunk(minX >> 4, minZ >> 4) ? parent.getChunkAt(minX >> 4, minZ >> 4) : null;
                final Chunk chunkIn01 = parent.getChunkClaim().containsChunk(minX >> 4, maxZ >> 4) ? parent.getChunkAt(minX >> 4, maxZ >> 4) : null;
                final Chunk chunkIn10 = parent.getChunkClaim().containsChunk(maxX >> 4, minZ >> 4) ? parent.getChunkAt(maxX >> 4, minZ >> 4) : null;
                final Chunk chunkIn11 = parent.getChunkClaim().containsChunk(maxX >> 4, maxZ >> 4) ? parent.getChunkAt(maxX >> 4, maxZ >> 4) : null;

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
