package ValkyrienWarfareBase.PhysCollision;

import java.util.ArrayList;

import ValkyrienWarfareBase.Vector;
import ValkyrienWarfareBase.Collision.PhysCollisionObject;
import ValkyrienWarfareBase.Collision.PhysPolygonCollider;
import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.Math.BigBastardMath;
import ValkyrienWarfareBase.Math.RotationMatrices;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class WorldPhysicsCollider {

	public PhysicsCalculations calculator;
	public World worldObj;
	public PhysicsObject parent;
	
	private ArrayList<BlockPos> cachedPotentialHits;
	
	//Ensures this always updates the first tick after creation
	private double ticksSinceCacheUpdate = 420;
	
	public static final double collisionCacheTickUpdateFrequency = 2D;
	private static final double expansion = 1D;
	
	public static double axisTolerance = .3D;
	
	public double e = .25D;
	
	public WorldPhysicsCollider(PhysicsCalculations calculations){
		calculator = calculations;
		parent = calculations.parent;
		worldObj = parent.worldObj;
	}
	
	//TODO: DO THIS!!!
	public void runPhysCollision(){
		//Multiply by 20 to convert seconds (physTickSpeed) into ticks (ticksSinceCacheUpdate)
		ticksSinceCacheUpdate += (20D*calculator.physTickSpeed);
		if(shouldUpdateCollisonCache()){
			updatePotentialCollisionCache();
		}
		processPotentialCollisions();
	}
	
	//Runs through the cache ArrayList, checking each possible BlockPos for SOLID blocks that can collide, if it finds any it will
	//move to the next method
	private void processPotentialCollisions(){
		for(BlockPos pos:cachedPotentialHits){
			Vector inWorld = new Vector(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);
			parent.coordTransform.fromGlobalToLocal(inWorld);
			
			int minX = (int) Math.floor(inWorld.X-1D);
			int minY = (int) Math.floor(inWorld.Y-1D);
			int minZ = (int) Math.floor(inWorld.Z-1D);
			
			int maxX = (int) Math.floor(inWorld.X+1D);
			int maxY = (int) Math.floor(inWorld.Y+1D);
			int maxZ = (int) Math.floor(inWorld.Z+1D);
			
			for(int x = minX;x<=maxX;x++){
				for(int z = minZ;z<=maxZ;z++){
					for(int y = minY;y<=maxY;y++){
						if(parent.ownsChunk(x>>4, z>>4)){
							Chunk chunkIn = parent.VKChunkCache.getChunkAt(x>>4, z>>4);
							IBlockState state = chunkIn.getBlockState(x,y,z);
							if(state.getMaterial().isSolid()){
								BlockPos localCollisionPos = new BlockPos(x,y,z);
								
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
		//The default <0,1,0> normal collision
		PhysCollisionObject toCollideWith = collider.collisions[1];
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
		AxisAlignedBB collisionBB = parent.collisionBB.expand(expansion, expansion, expansion);
		ticksSinceCacheUpdate = 0D;
		cachedPotentialHits = new ArrayList<BlockPos>();
		//Ship is outside of world blockSpace, just skip this all together
		if(collisionBB.maxY<0||collisionBB.minY>255){
			return;
		}
		
		BlockPos min = new BlockPos(collisionBB.minX,Math.max(collisionBB.minY,0),collisionBB.minZ);
		BlockPos max = new BlockPos(collisionBB.maxX,Math.min(collisionBB.maxY, 255),collisionBB.maxZ);
		
		ChunkCache cache = parent.surroundingWorldChunksCache;
		
		for(int x = min.getX();x<=max.getX();x++){
			for(int z = min.getZ();z<max.getZ();z++){
				int chunkX = (x>>4)-cache.chunkX;
				int chunkZ = (z>>4)-cache.chunkZ;
				if(!(chunkX<0||chunkZ<0||chunkX>cache.chunkArray.length-1||chunkZ>cache.chunkArray[0].length-1)){
					Chunk chunk = cache.chunkArray[chunkX][chunkZ];
					for(int y = min.getY();y<max.getY();y++){
						IBlockState state = chunk.getBlockState(x, y, z);
						if(state.getMaterial().isSolid()){
							cachedPotentialHits.add(new BlockPos(x,y,z));
						}
					}
				}
			}
		}
	}
	
}
