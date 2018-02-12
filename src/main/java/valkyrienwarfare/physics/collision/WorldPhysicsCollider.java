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

package valkyrienwarfare.physics.collision;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jackredcreeper.cannon.world.NewExp2;

import gnu.trove.list.array.TIntArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.physmanagement.relocation.SpatialDetector;
import valkyrienwarfare.physics.calculations.PhysicsCalculations;
import valkyrienwarfare.physics.collision.optimization.IBitOctree;
import valkyrienwarfare.physics.collision.optimization.IBitOctreeProvider;
import valkyrienwarfare.physics.collision.optimization.ShipCollisionTask;
import valkyrienwarfare.physics.management.PhysicsObject;

// A manager used to process collisions between ships and the game world
public class WorldPhysicsCollider {

    // Used to expand the AABB used to check for potential collisions; helps prevent
    // ships ghosting through blocks
    public static final double AABB_EXPANSION = 2D;
    public static final double RANGE_CHECK = 1.8D;
    // The minimum depth a collision projection must have, to not use the default
    // collision normal of <0, 1, 0>
    public static final double AXIS_TOLERANCE = .3D;
    // Time in seconds between collision cache updates
    public static final double CACHE_UPDATE_FREQUENCY = 1D;
    // Determines how 'bouncy' collisions are
    public static final double COLLISION_ELASTICITY = 1.52D;
    // The radius which the algorithm will search for a nearby block to collide with
    public static final double COLLISION_RANGE_CHECK = .65D;
    // If true, will use the octree assisted algorithm for finding collisions,
    // (Approx. O(log(n)^3)).
    // If false then this class uses the much slower iterative approach O(n^3).
    public static final boolean USE_OCTREE_COLLISION = true;
    // How likely it is for the collision tasks to shuffle every physics tick
    // ie. (.50D => 50% chance to shuffle, .30D => 30% chance, etc.)
    public static final double COLLISION_TASK_SHUFFLE_FREQUENCY = .50D;
    // Greater coefficients result in more friction
    public static final double KINETIC_FRICTION_COEFFICIENT = .1D;
    private final MutableBlockPos mutablePos;
    private final Random rand;
    private final List<ShipCollisionTask> tasks;
    private final PhysicsCalculations calculator;
    private final World worldObj;
    private final PhysicsObject parent;
    private final TIntArrayList cachedPotentialHits;
    private final TIntArrayList cachedHitsToRemove;
    // Ensures this always updates the first tick after creation
    private double ticksSinceCacheUpdate;
    private boolean updateCollisionTasksCache;
    private BlockPos centerPotentialHit;

    public WorldPhysicsCollider(PhysicsCalculations calculations) {
        this.calculator = calculations;
        this.parent = calculations.parent;
        this.worldObj = parent.worldObj;
        this.cachedPotentialHits = new TIntArrayList();
        this.cachedHitsToRemove = new TIntArrayList();
        this.rand = new Random();
        this.mutablePos = new MutableBlockPos();
        this.tasks = new ArrayList<ShipCollisionTask>();
        this.ticksSinceCacheUpdate = 25D;
        this.updateCollisionTasksCache = true;
    }

    // Runs the collision code
    public void runPhysCollision() {
        tickUpdatingTheCollisionCache();
        processPotentialCollisionsAccurately();
    }

    public void tickUpdatingTheCollisionCache() {
        // Multiply by 20 to convert seconds (physTickSpeed) into ticks
        ticksSinceCacheUpdate += 20D * calculator.getPhysTickSpeed();
        for (int i = 0; i < cachedHitsToRemove.size(); i++) {
            cachedPotentialHits.remove(cachedHitsToRemove.get(i));
        }
        cachedHitsToRemove.resetQuick();
        if (ticksSinceCacheUpdate > CACHE_UPDATE_FREQUENCY) {
            updatePotentialCollisionCache();
            updateCollisionTasksCache = true;
        }
        if (Math.random() < COLLISION_TASK_SHUFFLE_FREQUENCY) {
            cachedPotentialHits.shuffle(rand);
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
        toAdd.addAll(tasks);
    }

    public void processCollisionTask(ShipCollisionTask task) {
        MutableBlockPos inWorldPos = new MutableBlockPos();
        MutableBlockPos inLocalPos = new MutableBlockPos();

        for (CollisionInformationHolder info : task.getCollisionInformationGenerated()) {
            inWorldPos.setPos(info.inWorldX, info.inWorldY, info.inWorldZ);
            inLocalPos.setPos(info.inLocalX, info.inLocalY, info.inLocalZ);
            handleActualCollision(info.collider, inWorldPos, inLocalPos, info.inWorldState, info.inLocalState);
        }

        task.getCollisionInformationGenerated().clear();
    }

    // Runs through the cache ArrayList, checking each possible BlockPos for SOLID
    // blocks that can collide, if it finds any it will
    // move to the next method

    // TODO: Optimize from here, this is taking 10x the processing time of updating
    // collision cache!
    private void processPotentialCollisionsAccurately() {
        final MutableBlockPos localCollisionPos = new MutableBlockPos();
        final Vector inWorld = new Vector();

        for (int i = 0; i < cachedPotentialHits.size(); i++) {
            // Converts the int to a mutablePos
            SpatialDetector.setPosWithRespectTo(cachedPotentialHits.get(i), centerPotentialHit, mutablePos);

            inWorld.X = mutablePos.getX() + .5;
            inWorld.Y = mutablePos.getY() + .5;
            inWorld.Z = mutablePos.getZ() + .5;
            parent.coordTransform.fromGlobalToLocal(inWorld);

            int minX = MathHelper.floor(inWorld.X - COLLISION_RANGE_CHECK);
            int minY = MathHelper.floor(inWorld.Y - COLLISION_RANGE_CHECK);
            int minZ = MathHelper.floor(inWorld.Z - COLLISION_RANGE_CHECK);

            int maxX = MathHelper.floor(inWorld.X + COLLISION_RANGE_CHECK);
            int maxY = MathHelper.floor(inWorld.Y + COLLISION_RANGE_CHECK);
            int maxZ = MathHelper.floor(inWorld.Z + COLLISION_RANGE_CHECK);

            /**
             * Something here is causing the game to freeze :/
             */

            int minChunkX = minX >> 4;
            int minChunkY = minY >> 4;
            int minChunkZ = minZ >> 4;

            int maxChunkX = maxX >> 4;
            int maxChunkY = maxY >> 4;
            int maxChunkZ = maxZ >> 4;

            entireLoop: if (!(minChunkY > 15 || maxChunkY < 0)) {
                for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                    for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                        if (parent.ownsChunk(chunkX, chunkZ)) {
                            final Chunk chunkIn = parent.VKChunkCache.getChunkAt(chunkX, chunkZ);

                            int minXToCheck = chunkX << 4;
                            int maxXToCheck = minXToCheck + 15;

                            int minZToCheck = chunkZ << 4;
                            int maxZToCheck = minZToCheck + 15;

                            minXToCheck = Math.max(minXToCheck, minX);
                            maxXToCheck = Math.min(maxXToCheck, maxX);

                            minZToCheck = Math.max(minZToCheck, minZ);
                            maxZToCheck = Math.min(maxZToCheck, maxZ);

                            for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
                                ExtendedBlockStorage storage = chunkIn.storageArrays[chunkY];
                                if (storage != null) {
                                    int minYToCheck = chunkY << 4;
                                    int maxYToCheck = minYToCheck + 15;

                                    minYToCheck = Math.max(minYToCheck, minY);
                                    maxYToCheck = Math.min(maxYToCheck, maxY);

                                    for (int x = minXToCheck; x <= maxXToCheck; x++) {
                                        for (int z = minZToCheck; z <= maxZToCheck; z++) {
                                            for (int y = minYToCheck; y <= maxYToCheck; y++) {
                                                final IBlockState state = storage.get(x & 15, y & 15, z & 15);
                                                if (state.getMaterial().isSolid()) {

                                                    // Inject the multithreaded code here

                                                    localCollisionPos.setPos(x, y, z);

                                                    boolean brokeAWorldBlock = handleLikelyCollision(mutablePos,
                                                            localCollisionPos, parent.surroundingWorldChunksCache
                                                                    .getBlockState(mutablePos),
                                                            state);

                                                    if (brokeAWorldBlock) {
                                                        int positionRemoved = SpatialDetector.getHashWithRespectTo(
                                                                mutablePos.getX(), mutablePos.getY(), mutablePos.getZ(),
                                                                centerPotentialHit);
                                                        cachedHitsToRemove.add(positionRemoved);
                                                        break entireLoop;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Tests two block positions directly against each other, and figures out
    // whether a collision is occuring or not
    private boolean handleLikelyCollision(BlockPos inWorldPos, BlockPos inLocalPos, IBlockState inWorldState,
            IBlockState inLocalState) {
        // System.out.println("Handling a likely collision");
        AxisAlignedBB inLocalBB = new AxisAlignedBB(inLocalPos.getX(), inLocalPos.getY(), inLocalPos.getZ(),
                inLocalPos.getX() + 1, inLocalPos.getY() + 1, inLocalPos.getZ() + 1);
        AxisAlignedBB inGlobalBB = new AxisAlignedBB(inWorldPos.getX(), inWorldPos.getY(), inWorldPos.getZ(),
                inWorldPos.getX() + 1, inWorldPos.getY() + 1, inWorldPos.getZ() + 1);

        // This changes the box bounding box to the real bounding box, not sure if this
        // is better or worse for this mod
        // List<AxisAlignedBB> colBB = worldObj.getCollisionBoxes(inLocalBB);
        // inLocalBB = colBB.get(0);

        Polygon shipInWorld = new Polygon(inLocalBB, parent.coordTransform.lToWTransform);
        Polygon worldPoly = new Polygon(inGlobalBB);
        PhysPolygonCollider collider = new PhysPolygonCollider(shipInWorld, worldPoly, parent.coordTransform.normals);
        if (!collider.seperated) {
            return handleActualCollision(collider, inWorldPos, inLocalPos, inWorldState, inLocalState);
        }

        return false;
    }

    // Takes the collision data along all axes generated prior, and creates the
    // ideal value that is to be followed
    private boolean handleActualCollision(PhysPolygonCollider collider, BlockPos inWorldPos, BlockPos inLocalPos,
            IBlockState inWorldState, IBlockState inLocalState) {
        PhysCollisionObject toCollideWith;
        toCollideWith = collider.collisions[1];

        if (toCollideWith.penetrationDistance > AXIS_TOLERANCE || toCollideWith.penetrationDistance < -AXIS_TOLERANCE) {
            toCollideWith = collider.collisions[collider.minDistanceIndex];
        }

        // NestedBoolean didBlockBreakInShip = new NestedBoolean(false);
        // NestedBoolean didBlockBreakInWorld = new NestedBoolean(false);

        Vector positionInBody = collider.entity.getCenter();
        positionInBody.subtract(parent.wrapper.posX, parent.wrapper.posY, parent.wrapper.posZ);

        Vector velocityAtPoint = calculator.getVelocityAtPoint(positionInBody);

        double impulseApplied = 1D;
        // BlockRammingManager.processBlockRamming(parent.wrapper, collisionSpeed,
        // inLocalState, inWorldState, inLocalPos, inWorldPos, didBlockBreakInShip,
        // didBlockBreakInWorld);

        Vector[] collisionPoints = PolygonCollisionPointFinder.getPointsOfCollisionForPolygons(collider, toCollideWith,
                velocityAtPoint);

        impulseApplied /= collisionPoints.length;

        for (Vector collisionPos : collisionPoints) {
            Vector inBody = collisionPos
                    .getSubtraction(new Vector(parent.wrapper.posX, parent.wrapper.posY, parent.wrapper.posZ));
            inBody.multiply(-1D);
            Vector momentumAtPoint = calculator.getVelocityAtPoint(inBody);
            Vector axis = toCollideWith.axis;
            Vector offsetVector = toCollideWith.getResponse();
            calculateCollisionImpulseForce(inBody, momentumAtPoint, axis, offsetVector, false, false, impulseApplied);
            // calculateCollisionImpulseForce(inBody, momentumAtPoint, axis, offsetVector,
            // didBlockBreakInShip.getValue(),
            // didBlockBreakInWorld.getValue(), impulseApplied);
        }

        // This is causing crashes
        // TODO: Fix the crashes
        if (false) {
            if (false) { // didBlockBreakInShip.getValue()) {
                worldObj.destroyBlock(inLocalPos, true);
            }

            if (false) { // didBlockBreakInWorld.getValue()) {

                if (worldObj.getBlockState(inWorldPos)
                        .getBlock() instanceof com.jackredcreeper.cannon.blocks.BlockAirMine) {
                    double x = inWorldPos.getX();
                    double y = inWorldPos.getY();
                    double z = inWorldPos.getZ();

                    float size = 8F;
                    float power = 0F;
                    float blast = 0F;
                    float damage = 100F;

                    NewExp2 explosion1 = new NewExp2(worldObj, null, x, y, z, size, power, damage, blast, false, true);
                    explosion1.newBoom(worldObj, null, x, y, z, size, power, damage, blast, false, true);

                    worldObj.setBlockToAir(inWorldPos);
                } else

                    worldObj.destroyBlock(inWorldPos, true);
                return true;
            }
        }

        return false;
    }

    // Finally, the end of all this spaghetti code! This step takes all of the math
    // generated before, and it directly adds the result to Ship velocities
    private void calculateCollisionImpulseForce(Vector inBody, Vector momentumAtPoint, Vector axis, Vector offsetVector,
            boolean didBlockBreakInShip, boolean didBlockBreakInWorld, double impulseApplied) {
        Vector firstCross = inBody.cross(axis);
        RotationMatrices.applyTransform3by3(calculator.invFramedMOI, firstCross);

        Vector secondCross = firstCross.cross(inBody);

        double impulseMagnitude = -momentumAtPoint.dot(axis) * COLLISION_ELASTICITY
                / (calculator.getInvMass() + secondCross.dot(axis));

        Vector collisionImpulseForce = new Vector(axis, impulseMagnitude);

        if (didBlockBreakInShip || didBlockBreakInWorld) {
            // collisionImpulseForce.multiply(BlockRammingManager.collisionImpulseAfterRamming);
            collisionImpulseForce.multiply(impulseApplied);
        }

        // This is just an optimized way to add this force quickly to the
        // PhysicsCalculations
        if (collisionImpulseForce.dot(offsetVector) < 0) {
            // collisionImpulseForce.multiply(1.8D);
            double collisionVelocity = momentumAtPoint.dot(axis);

            if (Math.abs(collisionVelocity) < 0.01D) {
                collisionImpulseForce.zero();
            } else {
                addFrictionToNormalForce(momentumAtPoint, collisionImpulseForce);
                // calculateCoulumbFriction(inBody, momentumAtPoint, axis, offsetVector);
            }

            calculator.linearMomentum.add(collisionImpulseForce);
            Vector thirdCross = inBody.cross(collisionImpulseForce);

            RotationMatrices.applyTransform3by3(calculator.invFramedMOI, thirdCross);
            calculator.angularVelocity.add(thirdCross);
        }
    }

    // Applies the friction force generated by the collision
    private void addFrictionToNormalForce(Vector momentumAtPoint, Vector impulseVector) {
        Vector contactNormal = new Vector(impulseVector);
        contactNormal.normalize();

        Vector frictionVector = new Vector(momentumAtPoint);
        frictionVector.normalize();
        frictionVector.multiply(impulseVector.length() * KINETIC_FRICTION_COEFFICIENT);

        if (frictionVector.dot(momentumAtPoint) > 0) {
            frictionVector.multiply(-1D);
        }

        // Remove all friction components along the impulse vector
        double frictionImpulseDot = frictionVector.dot(contactNormal);
        Vector toRemove = contactNormal.getProduct(frictionImpulseDot);
        frictionVector.subtract(toRemove);
        impulseVector.add(frictionVector);
    }

    private void updatePotentialCollisionCache() {
        final AxisAlignedBB collisionBB = parent.getCollisionBoundingBox().expand(calculator.linearMomentum.X * calculator.getInvMass(),
                calculator.linearMomentum.Y * calculator.getInvMass(), calculator.linearMomentum.Z * calculator.getInvMass())
                .grow(AABB_EXPANSION);
        ticksSinceCacheUpdate = 0D;
        // This is being used to occasionally offset the collision cache update, in the
        // hopes this will prevent multiple ships from all updating
        // in the same tick
        if (Math.random() > .5) {
            ticksSinceCacheUpdate -= .05D;
        }
        // Resets the potential hits array in O(1) time! Isn't that something.
        cachedPotentialHits.resetQuick();
        // Ship is outside of world blockSpace, just skip this all together
        if (collisionBB.maxY < 0 || collisionBB.minY > 255) {
            return;
        }

        // Has a -1 on the minY value, I hope this helps with preventing things from
        // falling through the floor
        BlockPos min = new BlockPos(collisionBB.minX, Math.max(collisionBB.minY - 1, 0), collisionBB.minZ);
        BlockPos max = new BlockPos(collisionBB.maxX, Math.min(collisionBB.maxY, 255), collisionBB.maxZ);
        centerPotentialHit = new BlockPos((min.getX() + max.getX()) / 2D, (min.getY() + max.getY()) / 2D,
                (min.getZ() + max.getZ()) / 2D);

        // Used to prevent jvm creating extra objects it doesnt need.
        Vector temp1 = new Vector();
        Vector temp2 = new Vector();
        Vector temp3 = new Vector();

        ChunkCache cache = parent.surroundingWorldChunksCache;

        int chunkMinX = min.getX() >> 4;
        int chunkMaxX = (max.getX() >> 4) + 1;
        int storageMinY = min.getY() >> 4;
        int storageMaxY = (max.getY() >> 4) + 1;
        int chunkMinZ = min.getZ() >> 4;
        int chunkMaxZ = (max.getZ() >> 4) + 1;
        // long startTime = System.nanoTime();

        for (int chunkX = chunkMinX; chunkX < chunkMaxX; chunkX++) {
            for (int chunkZ = chunkMinZ; chunkZ < chunkMaxZ; chunkZ++) {
                int arrayChunkX = chunkX - cache.chunkX;
                int arrayChunkZ = chunkZ - cache.chunkZ;

                if (!(arrayChunkX < 0 || arrayChunkZ < 0 || arrayChunkX > cache.chunkArray.length - 1
                        || arrayChunkZ > cache.chunkArray[0].length - 1)) {
                    Chunk chunk = cache.chunkArray[arrayChunkX][arrayChunkZ];
                    for (int storageY = storageMinY; storageY < storageMaxY; storageY++) {
                        ExtendedBlockStorage extendedblockstorage = chunk.storageArrays[storageY];
                        if (extendedblockstorage != null) {
                            int minStorageX = chunkX << 4;
                            int minStorageY = storageY << 4;
                            int minStorageZ = chunkZ << 4;

                            int maxStorageX = minStorageX + 16;
                            int maxStorageY = minStorageY + 16;
                            int maxStorageZ = minStorageZ + 16;

                            IBitOctreeProvider provider = IBitOctreeProvider.class.cast(extendedblockstorage.data);
                            IBitOctree octree = provider.getBitOctree();

                            if (USE_OCTREE_COLLISION) {
                                for (int levelThree = 0; levelThree < 8; levelThree++) {
                                    int levelThreeIndex = octree.getOctreeLevelThreeIndex(levelThree);
                                    if (octree.getAtIndex(levelThreeIndex)) {
                                        for (int levelTwo = 0; levelTwo < 8; levelTwo++) {
                                            int levelTwoIndex = octree.getOctreeLevelTwoIndex(levelThreeIndex,
                                                    levelTwo);
                                            if (octree.getAtIndex(levelTwoIndex)) {
                                                for (int levelOne = 0; levelOne < 8; levelOne++) {
                                                    int levelOneIndex = octree.getOctreeLevelOneIndex(levelTwoIndex,
                                                            levelOne);
                                                    if (octree.getAtIndex(levelOneIndex)) {
                                                        
                                                        int baseX = ((levelThree % 2) * 8) + ((levelTwo % 2) * 4)
                                                                + ((levelOne % 2) * 2);
                                                        int baseY = (((levelThree >> 1) % 2) * 8)
                                                                + (((levelTwo >> 1) % 2) * 4)
                                                                + (((levelOne >> 1) % 2) * 2);
                                                        int baseZ = (((levelThree >> 2) % 2) * 8)
                                                                + (((levelTwo >> 2) % 2) * 4)
                                                                + (((levelOne >> 2) % 2) * 2);
                                                        // Don't run the checks for anything out of range
                                                        // if (true || baseX >= mmX && baseX <= mxX && baseY >= mmY &&
                                                        // baseY <= mxY &&
                                                        // baseZ >= mmZ
                                                        // && baseZ <= mxZ) {
                                                        checkForCollision(baseX + minStorageX, baseY + minStorageY,
                                                                baseZ + minStorageZ, extendedblockstorage, octree,
                                                                temp1, temp2, temp3);
                                                        checkForCollision(baseX + minStorageX, baseY + minStorageY,
                                                                baseZ + minStorageZ + 1, extendedblockstorage, octree,
                                                                temp1, temp2, temp3);
                                                        checkForCollision(baseX + minStorageX, baseY + minStorageY + 1,
                                                                baseZ + minStorageZ, extendedblockstorage, octree,
                                                                temp1, temp2, temp3);
                                                        checkForCollision(baseX + minStorageX, baseY + minStorageY + 1,
                                                                baseZ + minStorageZ + 1, extendedblockstorage, octree,
                                                                temp1, temp2, temp3);

                                                        checkForCollision(baseX + minStorageX + 1, baseY + minStorageY,
                                                                baseZ + minStorageZ, extendedblockstorage, octree,
                                                                temp1, temp2, temp3);
                                                        checkForCollision(baseX + minStorageX + 1, baseY + minStorageY,
                                                                baseZ + minStorageZ + 1, extendedblockstorage, octree,
                                                                temp1, temp2, temp3);
                                                        checkForCollision(baseX + minStorageX + 1,
                                                                baseY + minStorageY + 1, baseZ + minStorageZ,
                                                                extendedblockstorage, octree, temp1, temp2, temp3);
                                                        checkForCollision(baseX + minStorageX + 1,
                                                                baseY + minStorageY + 1, baseZ + minStorageZ + 1,
                                                                extendedblockstorage, octree, temp1, temp2, temp3);
                                                        // }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                for (int x = minStorageX; x < maxStorageX; x++) {
                                    for (int y = minStorageY; y < maxStorageY; y++) {
                                        for (int z = minStorageZ; z < maxStorageZ; z++) {
                                            checkForCollision(x, y, z, extendedblockstorage, octree, temp1, temp2,
                                                    temp3);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // long endTime = System.nanoTime();
            // System.out.println("Took " + (endTime - startTime));
        }
    }

    private void checkForCollision(int x, int y, int z, ExtendedBlockStorage storage, IBitOctree octree, Vector inLocal,
            Vector inBody, Vector speedInBody) {
        if (octree.get(x & 15, y & 15, z & 15)) {
            inLocal.X = x + .5D;
            inLocal.Y = y + .5D;
            inLocal.Z = z + .5D;
            // TODO: Something
            parent.coordTransform.fromGlobalToLocal(inLocal);

            inBody.setSubtraction(inLocal, parent.centerCoord);
            parent.physicsProcessor.setVectorToVelocityAtPoint(inBody, speedInBody);
            speedInBody.multiply(-parent.physicsProcessor.physRawSpeed);

            if (ValkyrienWarfareMod.highAccuracyCollisions) {
                speedInBody.multiply(20D);
            }

            int minX, minY, minZ, maxX, maxY, maxZ;
            if (speedInBody.X > 0) {
                minX = MathHelper.floor(inLocal.X - RANGE_CHECK);
                maxX = MathHelper.floor(inLocal.X + RANGE_CHECK + speedInBody.X);
            } else {
                minX = MathHelper.floor(inLocal.X - RANGE_CHECK + speedInBody.X);
                maxX = MathHelper.floor(inLocal.X + RANGE_CHECK);
            }

            if (speedInBody.Y > 0) {
                minY = MathHelper.floor(inLocal.Y - RANGE_CHECK);
                maxY = MathHelper.floor(inLocal.Y + RANGE_CHECK + speedInBody.Y);
            } else {
                minY = MathHelper.floor(inLocal.Y - RANGE_CHECK + speedInBody.Y);
                maxY = MathHelper.floor(inLocal.Y + RANGE_CHECK);
            }

            if (speedInBody.Z > 0) {
                minZ = MathHelper.floor(inLocal.Z - RANGE_CHECK);
                maxZ = MathHelper.floor(inLocal.Z + RANGE_CHECK + speedInBody.Z);
            } else {
                minZ = MathHelper.floor(inLocal.Z - RANGE_CHECK + speedInBody.Z);
                maxZ = MathHelper.floor(inLocal.Z + RANGE_CHECK);
            }

            outermostloop: for (int localX = minX; localX < maxX; localX++) {
                for (int localZ = minZ; localZ < maxZ; localZ++) {
                    for (int localY = minY; localY < maxY; localY++) {
                        if (parent.ownsChunk(localX >> 4, localZ >> 4)) {
                            Chunk chunkIn = parent.VKChunkCache.getChunkAt(localX >> 4, localZ >> 4);
                            if (chunkIn.storageArrays[localY >> 4] != null) {
                                IBitOctreeProvider provider = IBitOctreeProvider.class
                                        .cast(chunkIn.storageArrays[localY >> 4].getData());
                                IBitOctree octreeInLocal = provider.getBitOctree();
                                if (octreeInLocal.get(localX & 15, localY & 15, localZ & 15)) {
                                    int hash = SpatialDetector.getHashWithRespectTo(x, y, z, centerPotentialHit);
                                    cachedPotentialHits.add(hash);
                                    break outermostloop;
                                }
                            }
                        }
                    }
                }
            }
        }
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
