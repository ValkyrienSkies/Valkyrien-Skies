package ValkyrienWarfareBase.PhysCollision;

import java.util.ArrayList;
import java.util.Collections;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.PhysCollisionObject;
import ValkyrienWarfareBase.Collision.PhysPolygonCollider;
import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.Relocation.SpatialDetector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class WorldPhysicsCollider {

	public PhysicsCalculations calculator;
	public World worldObj;
	public PhysicsObject parent;
	
	private ArrayList<BlockPos> cachedPotentialHits;
	private BlockPos[] internalCachedPotentialHits;
	
	//Ensures this always updates the first tick after creation
	private double ticksSinceCacheUpdate = 420;
	
	public static final double collisionCacheTickUpdateFrequency = 2D;
	private static final double expansion = 2D;
	
	public static double axisTolerance = .3D;
	
	public double e = .35D;
	
	//New stuff
	private int[] cachedPotentialHitsInt;
	private BlockPos centerPotentialHit;
	
	public WorldPhysicsCollider(PhysicsCalculations calculations){
		calculator = calculations;
		parent = calculations.parent;
		worldObj = parent.worldObj;
	}
	
	//TODO: DO THIS!!!
	public void runPhysCollision(){
		//Multiply by 20 to convert seconds (physTickSpeed) into ticks (ticksSinceCacheUpdate)
		ticksSinceCacheUpdate += 20D*calculator.physTickSpeed;
		if(shouldUpdateCollisonCache()){
			updatePotentialCollisionCache();
//			Collections.shuffle(cachedPotentialHits);
		}
		processPotentialCollisionsAccurately();
	}
	
	//Runs through the cache ArrayList, checking each possible BlockPos for SOLID blocks that can collide, if it finds any it will
	//move to the next method
	private void processPotentialCollisionsAccurately(){
		final MutableBlockPos localCollisionPos = new MutableBlockPos();
		final Vector inWorld = new Vector();
		
		int minX,minY,minZ,maxX,maxY,maxZ,x,y,z;
		
		BlockPos pos;
		final double rangeCheck = .65D;
		
//		for (Iterator<BlockPos> i=cachedPotentialHits.iterator(); i.hasNext();){
//			pos = i.next();
		for(int i:cachedPotentialHitsInt){
			pos = SpatialDetector.getPosWithRespectTo(i, centerPotentialHit);
			
			inWorld.X = pos.getX()+.5; inWorld.Y = pos.getY()+.5; inWorld.Z = pos.getZ()+.5;
			parent.coordTransform.fromGlobalToLocal(inWorld);
			
//			minX = (int) Math.floor(inWorld.X-1D);
//			minY = (int) Math.floor(inWorld.Y-1D);
//			minZ = (int) Math.floor(inWorld.Z-1D);
			
			maxX = (int) Math.floor(inWorld.X+rangeCheck);
			maxY = (int) Math.floor(inWorld.Y+rangeCheck);
			maxZ = (int) Math.floor(inWorld.Z+rangeCheck);
			
			for(x = MathHelper.floor_double(inWorld.X-rangeCheck);x<=maxX;x++){
				for(z = MathHelper.floor_double(inWorld.Z-rangeCheck);z<=maxZ;z++){
					for(y = MathHelper.floor_double(inWorld.Y-rangeCheck);y<=maxY;y++){
						if(parent.ownsChunk(x>>4, z>>4)){
							final Chunk chunkIn = parent.VKChunkCache.getChunkAt(x>>4, z>>4);
							final IBlockState state = chunkIn.getBlockState(x,y,z);
							if(state.getMaterial().isSolid()){
								localCollisionPos.setPos(x, y, z);
								
								handleLikelyCollision(pos,localCollisionPos,parent.surroundingWorldChunksCache.getBlockState(pos),state);
							}
						}
					}
				}
			}
		}
		
	}
	
	//TODO: Code this
	private void handleLikelyCollision(BlockPos inWorldPos,BlockPos inLocalPos,IBlockState inWorldState,IBlockState inLocalState){
//		System.out.println("Handling a likely collision");
		AxisAlignedBB inLocalBB = new AxisAlignedBB(inLocalPos.getX(),inLocalPos.getY(),inLocalPos.getZ(),inLocalPos.getX()+1,inLocalPos.getY()+1,inLocalPos.getZ()+1);
		AxisAlignedBB inGlobalBB  = new AxisAlignedBB(inWorldPos.getX(),inWorldPos.getY(),inWorldPos.getZ(),inWorldPos.getX()+1,inWorldPos.getY()+1,inWorldPos.getZ()+1);
		
		Polygon shipInWorld = new Polygon(inLocalBB,parent.coordTransform.lToWTransform);
		Polygon worldPoly = new Polygon(inGlobalBB);
		
		
		PhysPolygonCollider collider = new PhysPolygonCollider(shipInWorld,worldPoly,parent.coordTransform.normals);
		
		if(!collider.seperated){
			handleActualCollision(collider);
		}
	}

	private void handleActualCollision(PhysPolygonCollider collider){
//		Vector speedAtPoint = calculator.getMomentumAtPoint(collider.collisions[0].firstContactPoint.getSubtraction(new Vector(parent.wrapper.posX,parent.wrapper.posY,parent.wrapper.posZ)));
//		
//		double xDot = Math.abs(speedAtPoint.X);
//		double yDot = Math.abs(speedAtPoint.Y)/5D;
//		double zDot = Math.abs(speedAtPoint.Z);
		
		PhysCollisionObject toCollideWith = null;
		
		//NOTE: This is all EXPERIMENTAL! Could possibly revert 
//		if(yDot>xDot&&yDot>zDot){
//			//Y speed is greatest
//			if(xDot>zDot){
//				toCollideWith = collider.collisions[2];
//			}else{
//				toCollideWith = collider.collisions[0];
//			}
//		}else{
//			if(xDot>zDot){
//				//X speed is greatest
//				toCollideWith = collider.collisions[1];
//			}else{
//				//Z speed is greatest
//				toCollideWith = collider.collisions[1];
//			}
//		}
		
		toCollideWith = collider.collisions[1];
		
		if(toCollideWith.penetrationDistance>axisTolerance||toCollideWith.penetrationDistance<-axisTolerance){
			toCollideWith = collider.collisions[collider.minDistanceIndex];
		}
		
		Vector collisionPos = toCollideWith.firstContactPoint;
		
		//TODO: Maybe use Ship center of mass instead
		Vector inBody = collisionPos.getSubtraction(new Vector(parent.wrapper.posX,parent.wrapper.posY,parent.wrapper.posZ));
		
		inBody.multiply(-1D);
		
		Vector momentumAtPoint = calculator.getMomentumAtPoint(inBody);
		Vector axis = toCollideWith.axis;
		Vector offsetVector = toCollideWith.getResponse();
		
		processCollisionData(inBody, momentumAtPoint, axis, offsetVector);
		
		collisionPos = toCollideWith.getSecondContactPoint();
		
		//TODO: Maybe use Ship center of mass instead
		inBody = collisionPos.getSubtraction(new Vector(parent.wrapper.posX,parent.wrapper.posY,parent.wrapper.posZ));
		
		inBody.multiply(-1D);
		
		momentumAtPoint = calculator.getMomentumAtPoint(inBody);
		axis = toCollideWith.axis;
		offsetVector = toCollideWith.getResponse();
		
		processCollisionData(inBody, momentumAtPoint, axis, offsetVector);
	}
	
	private void processCollisionData(Vector inBody,Vector momentumAtPoint,Vector axis,Vector offsetVector){
		
		
		
		Vector firstCross = inBody.cross(axis);
		RotationMatrices.applyTransform3by3(calculator.invFramedMOI, firstCross);
		
		Vector secondCross = firstCross.cross(inBody);
		
//		momentumAtPoint.multiply(5D);
		
		double j = -momentumAtPoint.dot(axis)*(e+1D)/(calculator.invMass+secondCross.dot(axis));
		
		Vector simpleImpulse = new Vector(axis,j);
		
//		System.out.println(simpleImpulse);
		
		if(simpleImpulse.dot(offsetVector)<0){
			calculator.linearMomentum.add(simpleImpulse);
			Vector thirdCross = inBody.cross(simpleImpulse);
			
			RotationMatrices.applyTransform3by3(calculator.invFramedMOI,thirdCross);
			calculator.angularVelocity.add(thirdCross);
//			return true;
		}
		
	}
	
	private boolean shouldUpdateCollisonCache(){
		return (ticksSinceCacheUpdate)>collisionCacheTickUpdateFrequency;
	}
	
	private void updatePotentialCollisionCache(){
		final AxisAlignedBB collisionBB = parent.collisionBB.expand(expansion, expansion, expansion).addCoord(calculator.linearMomentum.X*calculator.invMass, calculator.linearMomentum.Y*calculator.invMass, calculator.linearMomentum.Z*calculator.invMass);
		ticksSinceCacheUpdate = 0D;
		cachedPotentialHits = new ArrayList<BlockPos>();
		//Ship is outside of world blockSpace, just skip this all together
		if(collisionBB.maxY<0||collisionBB.minY>255){
//			internalCachedPotentialHits = new BlockPos[0];
			cachedPotentialHitsInt = new int[0];
			return;
		}
		
		final BlockPos min = new BlockPos(collisionBB.minX,Math.max(collisionBB.minY,0),collisionBB.minZ);
		final BlockPos max = new BlockPos(collisionBB.maxX,Math.min(collisionBB.maxY, 255),collisionBB.maxZ);
		centerPotentialHit = new BlockPos((min.getX()+max.getX())/2D,(min.getY()+max.getY())/2D,(min.getZ()+max.getZ())/2D);
		
		final ChunkCache cache = parent.surroundingWorldChunksCache;
		final Vector inLocal = new Vector();
		int maxX,maxY,maxZ;
		final double rangeCheck = 1.8D;
		
		for(int x = min.getX();x<=max.getX();x++){
			for(int z = min.getZ();z<max.getZ();z++){
				int chunkX = (x>>4)-cache.chunkX;
				int chunkZ = (z>>4)-cache.chunkZ;
				if(!(chunkX<0||chunkZ<0||chunkX>cache.chunkArray.length-1||chunkZ>cache.chunkArray[0].length-1)){
					final Chunk chunk = cache.chunkArray[chunkX][chunkZ];
					for(int y = min.getY();y<max.getY();y+=1){
						final IBlockState state = chunk.getBlockState(x, y, z);
						if(state.getMaterial().isSolid()){
							inLocal.X = x+.5D;inLocal.Y = y+.5D;inLocal.Z = z+.5D;
							parent.coordTransform.fromGlobalToLocal(inLocal);
							
							maxX = (int) Math.floor(inLocal.X+rangeCheck);
							maxY = (int) Math.floor(inLocal.Y+rangeCheck);
							maxZ = (int) Math.floor(inLocal.Z+rangeCheck);
							
							for(int localX = MathHelper.floor_double(inLocal.X-rangeCheck);localX<maxX;localX++){
								for(int localZ = MathHelper.floor_double(inLocal.Z-rangeCheck);localZ<maxZ;localZ++){
									for(int localY = MathHelper.floor_double(inLocal.Y-rangeCheck);localY<maxY;localY++){
										if(parent.ownsChunk(localX>>4, localZ>>4)){
											final Chunk chunkIn = parent.VKChunkCache.getChunkAt(localX>>4, localZ>>4);
											final IBlockState localState = chunkIn.getBlockState(localX,localY,localZ);
											if(localState.getMaterial().isSolid()){
												cachedPotentialHits.add(new BlockPos(x,y,z));
												localX = localY = localZ = Integer.MAX_VALUE-420;
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
		Collections.shuffle(cachedPotentialHits);
		cachedPotentialHitsInt = new int[cachedPotentialHits.size()];
		int i = 0;
		for(BlockPos pos:cachedPotentialHits){
			int hash = SpatialDetector.getHashWithRespectTo(pos.getX(), pos.getY(), pos.getZ(), centerPotentialHit);
			cachedPotentialHitsInt[i] = hash;
			i++;
		}
//		internalCachedPotentialHits = new BlockPos[arrayInternals.length];
//		internalCachedPotentialHits = (BlockPos[]) Arrays.copyOf(arrayInternals, arrayInternals.length, BlockPos[].class);
	}
	
}
