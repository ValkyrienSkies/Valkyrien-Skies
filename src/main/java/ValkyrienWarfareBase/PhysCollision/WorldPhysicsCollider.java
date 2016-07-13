package ValkyrienWarfareBase.PhysCollision;

import java.util.ArrayList;

import ValkyrienWarfareBase.Collision.PhysPolygonCollider;
import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.Math.Vector;
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
	
	private ChunkCache cache;
	
	public static final double collisionCacheTickUpdateFrequency = 2D;
	private static final double expansion = 1D;
	
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
							Chunk chunkIn = parent.chunkCache.getChunkAt(x>>4, z>>4);
							IBlockState state = chunkIn.getBlockState(x,y,z);
							if(state.getMaterial().isSolid()){
								BlockPos localCollisionPos = new BlockPos(x,y,z);
								
								handleLikelyCollision(pos,localCollisionPos,cache.getBlockState(pos),state);
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
		
		
		PhysPolygonCollider collider = new PhysPolygonCollider(shipInWorld,worldPoly,parent.coordTransform.normals,new Vector());
		
		if(!collider.seperated){
			System.out.println("Active collision");
		}
	}
	
	private boolean shouldUpdateCollisonCache(){
		return (ticksSinceCacheUpdate)>collisionCacheTickUpdateFrequency;
	}
	
	private void updatePotentialCollisionCache(){
		AxisAlignedBB collisionBB = parent.collisionBB.expand(expansion, expansion, expansion);
		ticksSinceCacheUpdate = 0D;
		cachedPotentialHits = new ArrayList<BlockPos>();
		if(collisionBB.maxY<0){
			return;
		}
		
		BlockPos min = new BlockPos(collisionBB.minX,Math.max(collisionBB.minY,0),collisionBB.minZ);
		BlockPos max = new BlockPos(collisionBB.maxX,collisionBB.maxY,collisionBB.maxZ);
		cache = new ChunkCache(worldObj,min,max,0);
		
		for(int x = min.getX();x<=max.getX();x++){
			for(int z = min.getZ();z<max.getZ();z++){
				Chunk chunk = cache.chunkArray[(x>>4)-cache.chunkX][(z>>4)-cache.chunkZ];
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
