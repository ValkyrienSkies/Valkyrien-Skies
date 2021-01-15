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
import org.valkyrienskies.mod.common.util.datastructures.IBitOctree;
import org.valkyrienskies.mod.common.util.datastructures.ITerrainOctreeProvider;
import valkyrienwarfare.api.TransformType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Handles the task of finding and processing collisions between a PhysicsObject and the game
 * world.
 *
 * @author thebest108
 */
public class WorldPhysicsCollider {

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
    public static final double CACHE_UPDATE_PERIOD = .01;
    // Determines how 'bouncy' collisions are
    public static final double COEFFICIENT_OF_RESTITUTION = .52D;

    // Greater coefficients result in more friction
    public static final double KINETIC_FRICTION_COEFFICIENT = .15D;
    // Use ThreadLocalRandom because its much faster than Random.
    private final ThreadLocalRandom rand;
    private final Collection<ShipCollisionTask> tasks;
    private final PhysicsCalculations calculator;
    private final PhysicsObject parent;
    private final TIntList cachedPotentialHits;
    private final TIntArrayList cachedHitsToRemove;
    // Ensures this always updates the first tick after creation
    private double ticksSinceCacheUpdate;
    private boolean updateCollisionTasksCache;
    private BlockPos centerPotentialHit;

    public WorldPhysicsCollider(PhysicsCalculations calculations) {
        this.calculator = calculations;
        this.parent = calculations.getParent();
        this.cachedPotentialHits = new TIntArrayList();
        this.cachedHitsToRemove = new TIntArrayList();
        this.rand = ThreadLocalRandom.current();
        this.tasks = new ArrayList<>();
        this.ticksSinceCacheUpdate = 25D;
        this.updateCollisionTasksCache = true;
        this.centerPotentialHit = null;
    }

    public void tickUpdatingTheCollisionCache() {
        // Multiply by 20 to convert seconds (physTickSpeed) into ticks
        ticksSinceCacheUpdate += calculator.getPhysicsTimeDeltaPerPhysTick();
        for (int i = 0; i < cachedHitsToRemove.size(); i++) {
            cachedPotentialHits.remove(cachedHitsToRemove.get(i));
        }
        cachedHitsToRemove.resetQuick();
        if (ticksSinceCacheUpdate > CACHE_UPDATE_PERIOD) {
            updatePotentialCollisionCache();
            updateCollisionTasksCache = true;
        }
    }

    public void splitIntoCollisionTasks(List<ShipCollisionTask> toAdd) {
        if (updateCollisionTasksCache) {
            tasks.clear();
            int index = 0;
            int size = cachedPotentialHits.size();
            while (index < size) {
                ShipCollisionTask task = new ShipCollisionTask(this, index);
                index += ShipCollisionTask.MAX_TASKS_TO_CHECK;
                tasks.add(task);
            }
            updateCollisionTasksCache = false;
        }
        cachedPotentialHits.shuffle(rand);
        toAdd.addAll(tasks);
    }

    public void processCollisionTask(ShipCollisionTask task) {
        MutableBlockPos inWorldPos = new MutableBlockPos();
        MutableBlockPos inLocalPos = new MutableBlockPos();

        Iterator<CollisionInformationHolder> collisionIterator = task
            .getCollisionInformationIterator();

        while (collisionIterator.hasNext()) {
            CollisionInformationHolder info = collisionIterator.next();
            inWorldPos.setPos(info.inWorldX, info.inWorldY, info.inWorldZ);
            inLocalPos.setPos(info.inLocalX, info.inLocalY, info.inLocalZ);
            handleActualCollision(inWorldPos, inLocalPos, info.inWorldState,
                info.inLocalState);
        }

        /*
         * for (CollisionInformationHolder info :
         * task.getCollisionInformationGenerated()) { inWorldPos.setPos(info.inWorldX,
         * info.inWorldY, info.inWorldZ); inLocalPos.setPos(info.inLocalX,
         * info.inLocalY, info.inLocalZ); handleActualCollision(info.collider,
         * inWorldPos, inLocalPos, info.inWorldState, info.inLocalState); }
         */

        task.getCollisionInformationGenerated().clear();
    }

    private static final byte[] combinationsOfOnes = new byte[] {
            1, 1, 1,
            1, 1, -1,
            1, -1, 1,
            1, -1, -1,
            -1, 1, 1,
            -1, 1, -1,
            -1, -1, 1,
            -1, -1, -1
    };

    // Temp variables
    private final Vector3d temp0 = new Vector3d();
    private final Vector3d temp1 = new Vector3d();
    private final Vector3d temp2 = new Vector3d();
    private final Vector3d temp3 = new Vector3d();

    private static final double SPHERE_RADIUS = .25;

    // Takes the collision data along all axes generated prior, and creates the
    // ideal value that is to be followed
    private boolean handleActualCollision(BlockPos inWorldPos,
        BlockPos inLocalPos,
        IBlockState inWorldState, IBlockState inLocalState) {

        final ShipTransform parentTransform = parent.getShipTransformationManager().getCurrentPhysicsTransform();

        final boolean isXUpSolid = isBlockInWorldSolidFast(inWorldPos.getX() + 1, inWorldPos.getY(), inWorldPos.getZ());
        final boolean isXDownSolid = isBlockInWorldSolidFast(inWorldPos.getX() - 1, inWorldPos.getY(), inWorldPos.getZ());
        final boolean isYUpSolid = isBlockInWorldSolidFast(inWorldPos.getX(), inWorldPos.getY() + 1, inWorldPos.getZ());
        final boolean isYDownSolid = isBlockInWorldSolidFast(inWorldPos.getX(), inWorldPos.getY() - 1, inWorldPos.getZ());
        final boolean isZUpSolid = isBlockInWorldSolidFast(inWorldPos.getX(), inWorldPos.getY(), inWorldPos.getZ() + 1);
        final boolean isZDownSolid = isBlockInWorldSolidFast(inWorldPos.getX(), inWorldPos.getY(), inWorldPos.getZ() - 1);

        if (isXUpSolid && isXDownSolid && isYUpSolid && isYDownSolid && isZUpSolid && isZDownSolid) {
            // Interior block, skip
            return false;
        }

        for (int i = 0; i < 7; i++) {
            final int xAxis = combinationsOfOnes[i * 3];
            final int yAxis = combinationsOfOnes[i * 3 + 1];
            final int zAxis = combinationsOfOnes[i * 3 + 2];

            // Position of the World's block in global
            final Vector3dc shipBlockInLocal = temp0.set(inLocalPos.getX() + .5 + xAxis * SPHERE_RADIUS, inLocalPos.getY() + .5 + yAxis * SPHERE_RADIUS, inLocalPos.getZ() + .5 + zAxis * SPHERE_RADIUS);
            // Position of the World's block in local
            final Vector3dc shipBlockInGlobal = parentTransform.transformPositionNew(temp1.set(shipBlockInLocal), TransformType.SUBSPACE_TO_GLOBAL);

            double xUpOffset = .5 - (shipBlockInGlobal.x() - (inWorldPos.getX() + .5)) + SPHERE_RADIUS;
            double xDownOffset = .5 - ((inWorldPos.getX() + .5) - shipBlockInGlobal.x()) + SPHERE_RADIUS;

            double yUpOffset = .5 - (shipBlockInGlobal.y() - (inWorldPos.getY() + .5)) + SPHERE_RADIUS;
            double yDownOffset = .5 - ((inWorldPos.getY() + .5) - shipBlockInGlobal.y()) + SPHERE_RADIUS;

            double zUpOffset = .5 - (shipBlockInGlobal.z() - (inWorldPos.getZ() + .5)) + SPHERE_RADIUS;
            double zDownOffset = .5 - ((inWorldPos.getZ() + .5) - shipBlockInGlobal.z()) + SPHERE_RADIUS;


            if (isXUpSolid) {
                xUpOffset = 1;
            }
            if (isXDownSolid) {
                xDownOffset = 1;
            }
            if (isYUpSolid) {
                yUpOffset = 1;
            }
            if (isYDownSolid) {
                yDownOffset = 1;
            }
            if (isZUpSolid) {
                zUpOffset = 1;
            }
            if (isZDownSolid) {
                zDownOffset = 1;
            }

            if (xUpOffset > 0 && xDownOffset > 0 && yUpOffset > 0 && yDownOffset > 0 && zUpOffset > 0 && zDownOffset > 0) {
                // Intersection
                final Vector3dc axis;
                final Vector3dc response;

                // If X and Z collision have less magnitude than this, then we only consider the Y axis.
                final double THRESHOLD = .1;

                if (Math.abs(xUpOffset) < THRESHOLD && Math.abs(xDownOffset) < THRESHOLD && Math.abs(zUpOffset) < THRESHOLD && Math.abs(zDownOffset) < THRESHOLD && (!isYUpSolid || !isYDownSolid)) {
                    // Ignore other axes just use Y
                    if (yUpOffset < yDownOffset) {
                        // yUpOffset is minimum
                        axis = temp2.set(0, -1, 0);
                        response = axis.mul(yUpOffset, temp3);
                    } else {
                        // yDownOffset is minimum
                        axis = temp2.set(0, 1, 0);
                        response = axis.mul(yDownOffset, temp3);
                    }
                } else {
                    // Use the minimum overlapping axis
                    if (xUpOffset < xDownOffset && xUpOffset < yUpOffset && xUpOffset < yDownOffset && xUpOffset < zUpOffset && xUpOffset < zDownOffset) {
                        // xUpOffset is minimum
                        axis = temp2.set(-1, 0, 0);
                        response = axis.mul(xUpOffset, temp3);
                    } else if (xDownOffset < yUpOffset && xDownOffset < yDownOffset && xDownOffset < zUpOffset && xDownOffset < zDownOffset) {
                        // xDownOffset is minimum
                        axis = temp2.set(1, 0, 0);
                        response = axis.mul(xDownOffset, temp3);
                    } else if (yUpOffset < yDownOffset && yUpOffset < zUpOffset && yUpOffset < zDownOffset) {
                        // yUpOffset is minimum
                        axis = temp2.set(0, -1, 0);
                        response = axis.mul(yUpOffset, temp3);
                    } else if (yDownOffset < zUpOffset && yDownOffset < zDownOffset) {
                        // yDownOffset is minimum
                        axis = temp2.set(0, 1, 0);
                        response = axis.mul(yDownOffset, temp3);
                    } else if (zUpOffset < zDownOffset) {
                        // zUpOffset is minimum
                        axis = temp2.set(0, 0, -1);
                        response = axis.mul(zUpOffset, temp3);
                    } else {
                        // zDownOffset is minimum
                        axis = temp2.set(0, 0, 1);
                        response = axis.mul(zDownOffset, temp3);
                    }
                }
                handleCollision(shipBlockInGlobal, axis, response, 1);
            }
        }

        return false;
    }

    private boolean isBlockInWorldSolidFast(final int posX, final int posY, final int posZ) {
        if (posY < 0 || posY >= 256) {
            return false;
        }
        final ChunkCache surroundingChunks = parent.getCachedSurroundingChunks();
        final int relativeChunkX = (posX >> 4) - surroundingChunks.chunkX;
        final int relativeChunkZ = (posZ >> 4) - surroundingChunks.chunkZ;
        if (relativeChunkX < 0 || relativeChunkX >= surroundingChunks.chunkArray.length || relativeChunkZ < 0 || relativeChunkZ >= surroundingChunks.chunkArray[relativeChunkX].length) {
            return false;
        }
        final Chunk chunk = surroundingChunks.chunkArray[relativeChunkX][relativeChunkZ];
        if (chunk == null) {
            return false;
        }
        final ExtendedBlockStorage blockStorage = chunk.storageArrays[posY >> 4];
        if (blockStorage == null) {
            return false;
        }
        final IBitOctree octree = ITerrainOctreeProvider.class.cast(blockStorage.data).getSolidOctree();
        return octree.get(posX & 15, posY & 15, posZ & 15);
    }

    private void handleCollision(final Vector3dc collisionPos, final Vector3dc axis, final Vector3dc offsetVector, final double impulseApplied) {
        Vector3d inBody = new Vector3d(
                collisionPos.x() - parent.getShipTransform().getPosX(),
                collisionPos.y() - parent.getShipTransform().getPosY(),
                collisionPos.z() - parent.getShipTransform().getPosZ());
        Vector3d momentumAtPoint = calculator
                .getVelocityAtPoint(inBody);
        calculateCollisionImpulseForce(inBody, momentumAtPoint, axis, offsetVector, false,
                false, impulseApplied);
    }

    // Finally, the end of all this spaghetti code! This step takes all of the math
    // generated before, and it directly adds the result to Ship velocities
    private void calculateCollisionImpulseForce(Vector3dc inBody,
                                                Vector3d velocityAtPointOfCollision,
                                                Vector3dc axis,
                                                Vector3dc offsetVector,
                                                boolean didBlockBreakInShip,
                                                boolean didBlockBreakInWorld,
                                                double impulseApplied) {
        // Our ideal velocity is to negate the velocity at the point, and the collision offset.
        velocityAtPointOfCollision.sub(offsetVector);



        Vector3d firstCross = inBody.cross(axis, new Vector3d());

        calculator.getPhysInvMOITensor().transform(firstCross);

        Vector3d secondCross = firstCross.cross(inBody);

        double impulseMagnitude = -velocityAtPointOfCollision.dot(axis)
            / (calculator.getInvMass() + secondCross.dot(axis));

        // Below this speed our collision coefficient of restitution is zero.
        final double slopR = .5D;
        double collisionSpeed = Math.abs(velocityAtPointOfCollision.dot(axis));
        if (collisionSpeed > slopR) {
            impulseMagnitude *= (1 + COEFFICIENT_OF_RESTITUTION);
        } else {
            // TODO: Need to reduce this value by some factor
            // impulseMagnitude *= .5D;
        }

        Vector3d collisionImpulseForce = axis.mul(impulseMagnitude, new Vector3d());

        // This is just an optimized way to add this force as quickly as possible.
        // Added collisionImpulseForce.dot(inBody) > 0 to force all collision to move in
        // the direction towards the in body vector.
        if (collisionImpulseForce.dot(offsetVector) < 0 && collisionImpulseForce.dot(inBody) < 0) {
            // collisionImpulseForce.multiply(1.8D);
            double collisionVelocity = velocityAtPointOfCollision.dot(axis);

            addFrictionToNormalForce(velocityAtPointOfCollision, collisionImpulseForce, inBody);
            calculator.getLinearVelocity().add(collisionImpulseForce.mul(calculator.getInvMass(), new Vector3d()));
            Vector3d thirdCross = inBody.cross(collisionImpulseForce, new Vector3d());

            calculator.getPhysInvMOITensor().transform(thirdCross);

            calculator.getAngularVelocity().add(thirdCross, calculator.getAngularVelocity());
        }
    }

    // Applies the friction force generated by the collision.
    // The magnitude of this vector must be adjusted to minimize energy
    private void addFrictionToNormalForce(Vector3dc momentumAtPoint, Vector3d impulseVector, Vector3dc inBody) {
        Vector3d contactNormal = new Vector3d(impulseVector);
        contactNormal.normalize();

        Vector3d frictionVector = new Vector3d(momentumAtPoint);
        frictionVector.normalize();
        frictionVector.mul(impulseVector.length() * KINETIC_FRICTION_COEFFICIENT);

        if (frictionVector.dot(momentumAtPoint) > 0) {
            frictionVector.mul(-1D);
        }

        // Remove all friction components along the impulse vector
        double frictionImpulseDot = frictionVector.dot(contactNormal);
        Vector3d toRemove = contactNormal.mul(frictionImpulseDot, new Vector3d());
        frictionVector.sub(toRemove);

        double inertiaScalarAlongAxis = parent.getPhysicsCalculations()
            .getInertiaAlongRotationAxis();
        // The change in velocity vector
        Vector3dc initialVelocity = parent.getPhysicsCalculations().getLinearVelocity();
        // Don't forget to multiply by delta t
        Vector3d deltaVelocity = new Vector3d(frictionVector);
        deltaVelocity.mul(parent.getPhysicsCalculations().getInvMass() * parent.getPhysicsCalculations()
                .getDragForPhysTick());

        double A = initialVelocity.lengthSquared();
        double B = 2 * initialVelocity.dot(deltaVelocity);
        double C = deltaVelocity.lengthSquared();

        Vector3d initialAngularVelocity = new Vector3d(parent.getPhysicsCalculations().getAngularVelocity());
        Vector3d deltaAngularVelocity = inBody.cross(frictionVector, new Vector3d());
        // This might need to be 1 / inertiaScalarAlongAxis
        deltaAngularVelocity.mul(parent.getPhysicsCalculations().getDragForPhysTick() / inertiaScalarAlongAxis);

        double D = initialAngularVelocity.lengthSquared();
        double E = 2 * deltaAngularVelocity.dot(initialAngularVelocity);
        double F = deltaAngularVelocity.lengthSquared();

        // This is tied to PhysicsCalculations line 430
        if (initialAngularVelocity.lengthSquared() < .05 && initialVelocity.lengthSquared() < .05) {
            // Remove rotational friction if we are rotating slow enough
            D = E = F = 0;
        }

        // The coefficients of energy as a function of energyScaleFactor in the form (A
        // + B * k + c * k^2)
        double firstCoefficient =
            A * parent.getPhysicsCalculations().getMass() + D * inertiaScalarAlongAxis;
        double secondCoefficient =
            B * parent.getPhysicsCalculations().getMass() + E * inertiaScalarAlongAxis;
        double thirdCoefficient =
            C * parent.getPhysicsCalculations().getMass() + F * inertiaScalarAlongAxis;

        double scaleFactor = -secondCoefficient / (thirdCoefficient * 2);

        if (new Double(scaleFactor).isNaN()) {
            scaleFactor = 0;
        } else {
            scaleFactor = Math.max(0, Math.min(scaleFactor, 1));
            frictionVector.mul(scaleFactor);
        }

        // System.out.println(scaleFactor);
        // ===== Friction Scaling Code End =====

        impulseVector.add(frictionVector);
    }

    // TODO: The greatest physics lag starts here.
    private void updatePotentialCollisionCache() {
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

        AxisAlignedBB shipBBOriginal = parent.getPhysicsTransformAABB();

        if (shipBBOriginal == null) {
            return;
        }

        final AxisAlignedBB shipBB = shipBBOriginal.grow(3);

        // Use the physics tick collision box instead of the game tick collision box.
        // We are using grow(3) on both because for some reason if we don't then ships start
        // jiggling through the ground. God I can't wait for a new physics engine.
        final AxisAlignedBB collisionBB = shipBB
            .grow(AABB_EXPANSION).expand(
                calculator.getLinearVelocity().x * .2,
                calculator.getLinearVelocity().y * .2,
                calculator.getLinearVelocity().z * .2);


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
        // long startTime = System.nanoTime();

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

                    final ITerrainOctreeProvider provider = (ITerrainOctreeProvider) extendedblockstorage.data;
                    final IBitOctree octree = provider.getSolidOctree();

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

                            /*
                             * if (false) // TODO: This code isn't thread safe. try { boolean result =
                             * tooTiredToName(localX, localY, localZ, x, y, z); if (result) { break
                             * breakThisLoop; } } catch (Exception e) { e.printStackTrace(); }
                             */
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
                // break outermostloop;
            }
            // }
        }
        return false;
    }

    public BlockPos getCenterPotentialHit() {
        return centerPotentialHit;
    }

    public int getCachedPotentialHit(int offset) {
        return cachedPotentialHits.get(offset);
    }

    public int getCachedPotentialHitSize() {
        return cachedPotentialHits.size();
    }

    public PhysicsObject getParent() {
        return parent;
    }

}
