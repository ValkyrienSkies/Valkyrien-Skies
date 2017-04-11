package ValkyrienWarfareBase.PhysCollision;

import java.util.Random;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.PhysCollisionObject;
import ValkyrienWarfareBase.Collision.PhysPolygonCollider;
import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.Relocation.SpatialDetector;
import gnu.trove.iterator.TIntIterator;
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

public class WorldPhysicsCollider {

	public PhysicsCalculations calculator;
	public World worldObj;
	public PhysicsObject parent;

	private TIntArrayList cachedPotentialHits;
	private final MutableBlockPos mutablePos = new MutableBlockPos();

	// Ensures this always updates the first tick after creation
	private double ticksSinceCacheUpdate = 420;

	public double collisionCacheTickUpdateFrequency = 1D;
	private static final double expansion = 2D;

	public static double axisTolerance = .3D;

	public double collisionElasticity = .52D;

	private final Random rand = new Random();

	// New stuff
	// private int[] cachedPotentialHitsInt;
	private BlockPos centerPotentialHit;

	public WorldPhysicsCollider(PhysicsCalculations calculations) {
		calculator = calculations;
		parent = calculations.parent;
		worldObj = parent.worldObj;
	}

	//Runs the collision code
	public void runPhysCollision() {
		// Multiply by 20 to convert seconds (physTickSpeed) into ticks (ticksSinceCacheUpdate)
		ticksSinceCacheUpdate += 20D * calculator.physTickSpeed;
		if (shouldUpdateCollisonCache()) {
			updatePotentialCollisionCache();
			// Collections.shuffle(cachedPotentialHits);
		}
		processPotentialCollisionsAccurately();
	}

	// Runs through the cache ArrayList, checking each possible BlockPos for SOLID blocks that can collide, if it finds any it will
	// move to the next method
	
	
	//TODO: Optimize from here, this is taking 10x the processing time of updating collision cache!
	private void processPotentialCollisionsAccurately() {
		final MutableBlockPos localCollisionPos = new MutableBlockPos();
		final Vector inWorld = new Vector();

		int minX, minY, minZ, maxX, maxY, maxZ, x, y, z;

		final double rangeCheck = .65D;

		TIntIterator intIterator = cachedPotentialHits.iterator();

		while (intIterator.hasNext()) {
			// Converts the int to a mutablePos
			SpatialDetector.setPosWithRespectTo(intIterator.next(), centerPotentialHit, mutablePos);

			inWorld.X = mutablePos.getX() + .5;
			inWorld.Y = mutablePos.getY() + .5;
			inWorld.Z = mutablePos.getZ() + .5;
			parent.coordTransform.fromGlobalToLocal(inWorld);

			minX = MathHelper.floor_double(inWorld.X - rangeCheck);
			minY = MathHelper.floor_double(inWorld.Y - rangeCheck);
			minZ = MathHelper.floor_double(inWorld.Z - rangeCheck);
			
			maxX = MathHelper.floor_double(inWorld.X + rangeCheck);
			maxY = MathHelper.floor_double(inWorld.Y + rangeCheck);
			maxZ = MathHelper.floor_double(inWorld.Z + rangeCheck);

			/**
			 * Something here is causing the game to freeze :/
			 */
			
			int minChunkX = minX >> 4;
			int minChunkY = minY >> 4;
			int minChunkZ = minZ >> 4;
			
			int maxChunkX = maxX >> 4;
			int maxChunkY = maxY >> 4;
			int maxChunkZ = maxZ >> 4;
			
			for(int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++){
				for(int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++){
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
						
						for(int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++){
							ExtendedBlockStorage storage = chunkIn.storageArrays[chunkY];
							if(storage != null){
								int minYToCheck = chunkY << 4;
								int maxYToCheck = minYToCheck + 15;
								
								minYToCheck = Math.max(minYToCheck, minY);
								maxYToCheck = Math.min(maxYToCheck, maxY);
								
								for(x = minXToCheck; x <= maxXToCheck; x++){
									for(z = minZToCheck; z <= maxZToCheck; z++){
										for (y = minYToCheck; y <= maxYToCheck; y++) {
											final IBlockState state = storage.get(x & 15, y & 15, z & 15);
											if (state.getMaterial().isSolid()) {
												localCollisionPos.setPos(x, y, z);
		
												handleLikelyCollision(mutablePos, localCollisionPos, parent.surroundingWorldChunksCache.getBlockState(mutablePos), state);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			
			//The old way of doing things
			
			/*for (x = minX; x <= maxX; x++) {
				for (z = minZ; z <= maxZ; z++) {
					if (parent.ownsChunk(x >> 4, z >> 4)) {
						for (y = minY; y <= maxY; y++) {
							final Chunk chunkIn = parent.VKChunkCache.getChunkAt(x >> 4, z >> 4);
							final IBlockState state = chunkIn.getBlockState(x, y, z);
							if (state.getMaterial().isSolid()) {
								localCollisionPos.setPos(x, y, z);

								handleLikelyCollision(mutablePos, localCollisionPos, parent.surroundingWorldChunksCache.getBlockState(mutablePos), state);
							}
						}
					}
				}
			}*/
		}

	}

	//Tests two block positions directly against each other, and figures out whether a collision is occuring or not
	private void handleLikelyCollision(BlockPos inWorldPos, BlockPos inLocalPos, IBlockState inWorldState, IBlockState inLocalState) {
		// System.out.println("Handling a likely collision");
		AxisAlignedBB inLocalBB = new AxisAlignedBB(inLocalPos.getX(), inLocalPos.getY(), inLocalPos.getZ(), inLocalPos.getX() + 1, inLocalPos.getY() + 1, inLocalPos.getZ() + 1);
		AxisAlignedBB inGlobalBB = new AxisAlignedBB(inWorldPos.getX(), inWorldPos.getY(), inWorldPos.getZ(), inWorldPos.getX() + 1, inWorldPos.getY() + 1, inWorldPos.getZ() + 1);

		//This changes the box bounding box to the real bounding box, not sure if this is better or worse for this mod
//		List<AxisAlignedBB> colBB = worldObj.getCollisionBoxes(inLocalBB);
//		inLocalBB = colBB.get(0);
		
		Polygon shipInWorld = new Polygon(inLocalBB, parent.coordTransform.lToWTransform);
		Polygon worldPoly = new Polygon(inGlobalBB);

		PhysPolygonCollider collider = new PhysPolygonCollider(shipInWorld, worldPoly, parent.coordTransform.normals);

		if (!collider.seperated) {
			handleActualCollision(collider);
		}
	}

	//Takes the collision data along all axes generated prior, and creates the ideal value that is to be followed
	private void handleActualCollision(PhysPolygonCollider collider) {

		PhysCollisionObject toCollideWith = null;

		toCollideWith = collider.collisions[1];

		if (toCollideWith.penetrationDistance > axisTolerance || toCollideWith.penetrationDistance < -axisTolerance) {
			toCollideWith = collider.collisions[collider.minDistanceIndex];
		}

		Vector collisionPos = toCollideWith.firstContactPoint;

		// TODO: Maybe use Ship center of mass instead
		Vector inBody = collisionPos.getSubtraction(new Vector(parent.wrapper.posX, parent.wrapper.posY, parent.wrapper.posZ));

		inBody.multiply(-1D);

		Vector momentumAtPoint = calculator.getMomentumAtPoint(inBody);
		Vector axis = toCollideWith.axis;
		Vector offsetVector = toCollideWith.getResponse();

		calculateCollisionImpulseForce(inBody, momentumAtPoint, axis, offsetVector);

		collisionPos = toCollideWith.getSecondContactPoint();

		// TODO: Maybe use Ship center of mass instead
		inBody = collisionPos.getSubtraction(new Vector(parent.wrapper.posX, parent.wrapper.posY, parent.wrapper.posZ));

		inBody.multiply(-1D);

		momentumAtPoint = calculator.getMomentumAtPoint(inBody);
		axis = toCollideWith.axis;
		offsetVector = toCollideWith.getResponse();

		calculateCollisionImpulseForce(inBody, momentumAtPoint, axis, offsetVector);
		//Do this after
		momentumAtPoint = calculator.getMomentumAtPoint(inBody);
		calculateCoulumbFriction(inBody, momentumAtPoint, axis, offsetVector);
	}

	//Finally, the end of all this spaghetti code! This step takes all of the math generated before, and it directly adds the result to Ship velocities
	private void calculateCollisionImpulseForce(Vector inBody, Vector momentumAtPoint, Vector axis, Vector offsetVector) {
		Vector firstCross = inBody.cross(axis);
		RotationMatrices.applyTransform3by3(calculator.invFramedMOI, firstCross);

		Vector secondCross = firstCross.cross(inBody);

		double impulseMagnitude = -momentumAtPoint.dot(axis) * (collisionElasticity + 1D) / (calculator.invMass + secondCross.dot(axis));

		Vector collisionImpulseForce = new Vector(axis, impulseMagnitude);

		//This is just an optimized way to add this force quickly to the PhysicsCalculations
		if (collisionImpulseForce.dot(offsetVector) < 0) {
			calculator.linearMomentum.add(collisionImpulseForce);
			Vector thirdCross = inBody.cross(collisionImpulseForce);

			RotationMatrices.applyTransform3by3(calculator.invFramedMOI, thirdCross);
			calculator.angularVelocity.add(thirdCross);
			// return true;
		}

	}
	
	//Applies Coulumb Friction to the collision
	private void calculateCoulumbFriction(Vector inBody, Vector momentumAtPoint, Vector axis, Vector offsetVector){
		//Some number between 0 and 1
		double coefficientOfFriction = .5D;
		
		Vector tangentOfSliding = new Vector();
		
		double dotProduct = momentumAtPoint.dot(axis);
		
		tangentOfSliding = new Vector(momentumAtPoint);
		tangentOfSliding.subtract(axis.getProduct(dotProduct));
		
		//This is probably wrong
//		tangentOfSliding = momentumAtPoint.cross(axis).cross(axis);
		tangentOfSliding.normalize();
		
//		System.out.println(tangentOfSliding);
		
		Vector firstCross = inBody.cross(tangentOfSliding);
		RotationMatrices.applyTransform3by3(calculator.invFramedMOI, firstCross);

		Vector secondCross = firstCross.cross(inBody);
		
		
		double magnitudeOfFriction = -momentumAtPoint.dot(tangentOfSliding);
		
		magnitudeOfFriction /= (calculator.invMass + secondCross.dot(tangentOfSliding));
		
		Vector simpleImpulse = new Vector(tangentOfSliding, magnitudeOfFriction);
//		System.out.println(simpleImpulse);
		if (simpleImpulse.dot(offsetVector) < 0) {
			calculator.linearMomentum.add(simpleImpulse);
			Vector thirdCross = inBody.cross(simpleImpulse);

			RotationMatrices.applyTransform3by3(calculator.invFramedMOI, thirdCross);
			calculator.angularVelocity.add(thirdCross);
			// return true;
		}
	}

	private boolean shouldUpdateCollisonCache() {
		return (ticksSinceCacheUpdate) > collisionCacheTickUpdateFrequency;
	}

	private void updatePotentialCollisionCache() {
		final AxisAlignedBB collisionBB = parent.collisionBB.expand(expansion, expansion, expansion).addCoord(calculator.linearMomentum.X * calculator.invMass, calculator.linearMomentum.Y * calculator.invMass, calculator.linearMomentum.Z * calculator.invMass);

		ticksSinceCacheUpdate = 0D;
		//This is being used to occasionally offset the collision cache update, in the hopes this will prevent multiple ships from all updating
		//in the same tick
		if(Math.random()>.5){
			ticksSinceCacheUpdate -= .05D;
		}
		
		// cachedPotentialHits = new ArrayList<BlockPos>();
		cachedPotentialHits = new TIntArrayList();
		// Ship is outside of world blockSpace, just skip this all together
		if (collisionBB.maxY < 0 || collisionBB.minY > 255) {
			// internalCachedPotentialHits = new BlockPos[0];
			return;
		}

		//Has a -1 on the minY value, I hope this helps with preventing things from falling through the floor
		final BlockPos min = new BlockPos(collisionBB.minX, Math.max(collisionBB.minY - 1, 0), collisionBB.minZ);
		final BlockPos max = new BlockPos(collisionBB.maxX, Math.min(collisionBB.maxY, 255), collisionBB.maxZ);
		centerPotentialHit = new BlockPos((min.getX() + max.getX()) / 2D, (min.getY() + max.getY()) / 2D, (min.getZ() + max.getZ()) / 2D);

		final ChunkCache cache = parent.surroundingWorldChunksCache;
		final Vector inLocal = new Vector();
		int maxX, maxY, maxZ, localX, localY, localZ, x, y, z, chunkX, chunkZ;
		double rangeCheck = 1.8D;
		
		if(ValkyrienWarfareMod.highAccuracyCollisions){
			rangeCheck = 3D;
		}
		
		Chunk chunk, chunkIn;
		ExtendedBlockStorage extendedblockstorage;
		IBlockState state, localState;

		int chunkMinX = min.getX() >> 4;
		int chunkMaxX = (max.getX() >> 4) + 1;		
		int storageMinY = min.getY() >> 4;
		int storageMaxY = (max.getY() >> 4) + 1;
		int chunkMinZ = min.getZ() >> 4;
		int chunkMaxZ = (max.getZ() >> 4) + 1;
		
		int storageY;
		
		int mmX = min.getX(), mmY = min.getY(), mmZ = min.getZ(), mxX = max.getX(), mxY = max.getY(), mxZ = max.getZ();
		
		Vector inBody = new Vector();
		Vector speedInBody = new Vector();
		
		for(chunkX = chunkMinX; chunkX < chunkMaxX; chunkX++){
			for(chunkZ = chunkMinZ; chunkZ < chunkMaxZ; chunkZ++){
				
				int arrayChunkX = chunkX - cache.chunkX;
				int arrayChunkZ = chunkZ - cache.chunkZ;
				
				if (!(arrayChunkX < 0 || arrayChunkZ < 0 || arrayChunkX > cache.chunkArray.length - 1 || arrayChunkZ > cache.chunkArray[0].length - 1)) {
					chunk = cache.chunkArray[arrayChunkX][arrayChunkZ];
					for(storageY = storageMinY; storageY < storageMaxY; storageY++){
						extendedblockstorage = chunk.storageArrays[storageY];
						if(extendedblockstorage != null){
							int minStorageX = chunkX << 4;
							int minStorageY = storageY << 4;
							int minStorageZ = chunkZ << 4;
							
							int maxStorageX = minStorageX + 16;
							int maxStorageY = minStorageY + 16;
							int maxStorageZ = minStorageZ + 16;
							
							minStorageX = Math.max(minStorageX, mmX);
							minStorageY = Math.max(minStorageY, mmY);
							minStorageZ = Math.max(minStorageZ, mmZ);
							
							maxStorageX = Math.min(maxStorageX, mxX);
							maxStorageY = Math.min(maxStorageY, mxY);
							maxStorageZ = Math.min(maxStorageZ, mxZ);
							
							for(x = minStorageX; x < maxStorageX; x++){
								for(y = minStorageY; y < maxStorageY; y++){
									for(z = minStorageZ; z < maxStorageZ; z++){
										state = extendedblockstorage.get(x & 15, y & 15, z & 15);
										
										if (state.getMaterial().isSolid()) {
											inLocal.X = x + .5D;
											inLocal.Y = y + .5D;
											inLocal.Z = z + .5D;
											
											parent.coordTransform.fromGlobalToLocal(inLocal);
											
											inBody.setSubtraction(inLocal, parent.centerCoord);
											
											parent.physicsProcessor.setMomentumAtPoint(inBody, speedInBody);
											
											speedInBody.multiply(-parent.physicsProcessor.physRawSpeed);
											
											if(ValkyrienWarfareMod.highAccuracyCollisions){
												speedInBody.multiply(20D);
											}
//											System.out.println(speedInBody);
											
											int minX,minY,minZ;
											
											if(speedInBody.X > 0){
												minX = MathHelper.floor_double(inLocal.X - rangeCheck);
												maxX = MathHelper.floor_double(inLocal.X + rangeCheck + speedInBody.X);
											}else{
												minX = MathHelper.floor_double(inLocal.X - rangeCheck + speedInBody.X);
												maxX = MathHelper.floor_double(inLocal.X + rangeCheck);
											}
											
											if(speedInBody.Y > 0){
												minY = MathHelper.floor_double(inLocal.Y - rangeCheck);
												maxY = MathHelper.floor_double(inLocal.Y + rangeCheck + speedInBody.Y);
											}else{
												minY = MathHelper.floor_double(inLocal.Y - rangeCheck + speedInBody.Y);
												maxY = MathHelper.floor_double(inLocal.Y + rangeCheck);
											}
											
											if(speedInBody.Z > 0){
												minZ = MathHelper.floor_double(inLocal.Z - rangeCheck);
												maxZ = MathHelper.floor_double(inLocal.Z + rangeCheck + speedInBody.Z);
											}else{
												minZ = MathHelper.floor_double(inLocal.Z - rangeCheck + speedInBody.Z);
												maxZ = MathHelper.floor_double(inLocal.Z + rangeCheck);
											}
											
											
											
											
											
											/** The Old Way of doing things; approx. 33% slower overall when running this code instead of new
											for (localX = minX; localX < maxX; localX++) {
												for (localZ = minZ; localZ < maxZ; localZ++) {
													for (localY = minY; localY < maxY; localY++) {
														if (parent.ownsChunk(localX >> 4, localZ >> 4)) {
															chunkIn = parent.VKChunkCache.getChunkAt(localX >> 4, localZ >> 4);
															localState = chunkIn.getBlockState(localX, localY, localZ);
															if (localState.getMaterial().isSolid()) {
																cachedPotentialHits.add(SpatialDetector.getHashWithRespectTo(x, y, z, centerPotentialHit));
																localX = localY = localZ = Integer.MAX_VALUE - 420;
															}
														}
													}
												}
											}
											**/
											
											
											int shipChunkMinX = minX >> 4;
											int shipChunkMinY = Math.max(minY >> 4, 0);
											int shipChunkMinZ = minZ >> 4;
					
											int shipChunkMaxX = maxX >> 4;
											int shipChunkMaxY = Math.min(maxY >> 4, 15);
											int shipChunkMaxZ = maxZ >> 4;
											
											shipChunkMaxX++;shipChunkMaxY++;shipChunkMaxZ++;
											
											if(shipChunkMaxZ - shipChunkMinZ > 200 || shipChunkMaxX - shipChunkMinX > 200){
												System.err.println("Wtf. This fucking error");
												return;
											}
											
											
											testForNearbyBlocks:
											for(int shipChunkX = shipChunkMinX;shipChunkX < shipChunkMaxX; shipChunkX++){
												for(int shipChunkZ = shipChunkMinZ;shipChunkZ < shipChunkMaxZ; shipChunkZ++){
													if (parent.ownsChunk(shipChunkX, shipChunkZ)) {
														chunkIn = parent.VKChunkCache.getChunkAt(shipChunkX, shipChunkZ);
														for(int shipChunkYStorage = shipChunkMinY; shipChunkYStorage < shipChunkMaxY; shipChunkYStorage++){
															ExtendedBlockStorage storage = chunkIn.storageArrays[shipChunkYStorage];
															
															if(storage != null){
																int shipStorageMinX = shipChunkX << 4;
																int shipStorageMinY = shipChunkYStorage << 4;
																int shipStorageMinZ = shipChunkZ << 4;
																
																int shipStorageMaxX = shipStorageMinX + 16;
																int shipStorageMaxY = shipStorageMinY + 16;
																int shipStorageMaxZ = shipStorageMinZ + 16;
																
																shipStorageMinX = Math.max(shipStorageMinX, minX);
																shipStorageMinY = Math.max(shipStorageMinY, minY);
																shipStorageMinZ = Math.max(shipStorageMinZ, minZ);
																
																shipStorageMaxX = Math.min(shipStorageMaxX, maxX);
																shipStorageMaxY = Math.min(shipStorageMaxY, maxY);
																shipStorageMaxZ = Math.min(shipStorageMaxZ, maxZ);
																
																for(localX = shipStorageMinX; localX < shipStorageMaxX; localX++){
																	for(localY = shipStorageMinY; localY < shipStorageMaxY; localY++){
																		for(localZ = shipStorageMinZ; localZ < shipStorageMaxZ; localZ++){
																			localState = chunkIn.getBlockState(localX, localY, localZ);
																			if (localState.getMaterial().isSolid()) {
																				cachedPotentialHits.add(SpatialDetector.getHashWithRespectTo(x, y, z, centerPotentialHit));
																				break testForNearbyBlocks;
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
						}
					}
				}
			}
		}
		
	
		/**
		for (x = min.getX(); x <= max.getX(); x++) {
			for (z = min.getZ(); z < max.getZ(); z++) {
				chunkX = (x >> 4) - cache.chunkX;
				chunkZ = (z >> 4) - cache.chunkZ;
				if (!(chunkX < 0 || chunkZ < 0 || chunkX > cache.chunkArray.length - 1 || chunkZ > cache.chunkArray[0].length - 1)) {
					chunk = cache.chunkArray[chunkX][chunkZ];
					for (y = min.getY(); y < max.getY(); y++) {
						extendedblockstorage = chunk.storageArrays[y >> 4];
						if (extendedblockstorage != null) {
							state = extendedblockstorage.get(x & 15, y & 15, z & 15);
							
							if (state.getMaterial().isSolid()) {
								inLocal.X = x + .5D;
								inLocal.Y = y + .5D;
								inLocal.Z = z + .5D;
								parent.coordTransform.fromGlobalToLocal(inLocal);

								maxX = (int) Math.floor(inLocal.X + rangeCheck);
								maxY = (int) Math.floor(inLocal.Y + rangeCheck);
								maxZ = (int) Math.floor(inLocal.Z + rangeCheck);

								for (localX = MathHelper.floor_double(inLocal.X - rangeCheck); localX < maxX; localX++) {
									for (localZ = MathHelper.floor_double(inLocal.Z - rangeCheck); localZ < maxZ; localZ++) {
										for (localY = MathHelper.floor_double(inLocal.Y - rangeCheck); localY < maxY; localY++) {
											if (parent.ownsChunk(localX >> 4, localZ >> 4)) {
												chunkIn = parent.VKChunkCache.getChunkAt(localX >> 4, localZ >> 4);
												localState = chunkIn.getBlockState(localX, localY, localZ);
												if (localState.getMaterial().isSolid()) {
													cachedPotentialHits.add(SpatialDetector.getHashWithRespectTo(x, y, z, centerPotentialHit));
													localX = localY = localZ = Integer.MAX_VALUE - 420;
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
		**/

		cachedPotentialHits.shuffle(rand);
	}

}
