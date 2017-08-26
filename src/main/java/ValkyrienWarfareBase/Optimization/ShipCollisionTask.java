package ValkyrienWarfareBase.Optimization;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.PhysPolygonCollider;
import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.PhysCollision.WorldPhysicsCollider;
import ValkyrienWarfareBase.Relocation.SpatialDetector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ShipCollisionTask implements Callable<Void> {
	
	public final static int maxTasksToCheck = 45;
	public final WorldPhysicsCollider toTask;
	final int taskStartIndex;
	final int tasksToCheck;
	final MutableBlockPos mutablePos = new MutableBlockPos();
	final MutableBlockPos inLocalPos = new MutableBlockPos();
	
	private final Vector inWorld = new Vector();

//	public TIntArrayList foundPairs = new TIntArrayList();
	
	public ArrayList<CollisionInformationHolder> collisionInformationGenerated = new ArrayList<CollisionInformationHolder>();
	
	private IBlockState inWorldState;
	
	public ShipCollisionTask(WorldPhysicsCollider toTask, int taskStartIndex) {
		this.taskStartIndex = taskStartIndex;
		this.toTask = toTask;
		
		int size = toTask.cachedPotentialHits.size();
		
		if (taskStartIndex + maxTasksToCheck > size + 1) {
			tasksToCheck = size + 1 - taskStartIndex;
		} else {
			tasksToCheck = maxTasksToCheck;
		}
	}
	
	@Override
	public Void call() {
		for (int index = taskStartIndex; index < tasksToCheck + 1; index++) {
			int integer = toTask.cachedPotentialHits.get(index);
			
			processNumber(integer);
		}
		
		return null;
	}
	
	private void processNumber(int integer) {
		SpatialDetector.setPosWithRespectTo(integer, toTask.centerPotentialHit, mutablePos);
		inWorldState = toTask.parent.surroundingWorldChunksCache.getBlockState(mutablePos);
		
		inWorld.X = mutablePos.getX() + .5;
		inWorld.Y = mutablePos.getY() + .5;
		inWorld.Z = mutablePos.getZ() + .5;
		
		toTask.parent.coordTransform.fromGlobalToLocal(inWorld);
		
		int midX = MathHelper.floor(inWorld.X + .5D);
		int midY = MathHelper.floor(inWorld.Y + .5D);
		int midZ = MathHelper.floor(inWorld.Z + .5D);
		
		//Check the 27 possible positions
		checkPosition(midX - 1, midY - 1, midZ - 1, integer);
		checkPosition(midX - 1, midY - 1, midZ, integer);
		checkPosition(midX - 1, midY - 1, midZ + 1, integer);
		checkPosition(midX - 1, midY, midZ - 1, integer);
		checkPosition(midX - 1, midY, midZ, integer);
		checkPosition(midX - 1, midY, midZ + 1, integer);
		checkPosition(midX - 1, midY + 1, midZ - 1, integer);
		checkPosition(midX - 1, midY + 1, midZ, integer);
		checkPosition(midX - 1, midY + 1, midZ + 1, integer);
		
		checkPosition(midX, midY - 1, midZ - 1, integer);
		checkPosition(midX, midY - 1, midZ, integer);
		checkPosition(midX, midY - 1, midZ + 1, integer);
		checkPosition(midX, midY, midZ - 1, integer);
		checkPosition(midX, midY, midZ, integer);
		checkPosition(midX, midY, midZ + 1, integer);
		checkPosition(midX, midY + 1, midZ - 1, integer);
		checkPosition(midX, midY + 1, midZ, integer);
		checkPosition(midX, midY + 1, midZ + 1, integer);
		
		checkPosition(midX + 1, midY - 1, midZ - 1, integer);
		checkPosition(midX + 1, midY - 1, midZ, integer);
		checkPosition(midX + 1, midY - 1, midZ + 1, integer);
		checkPosition(midX + 1, midY, midZ - 1, integer);
		checkPosition(midX + 1, midY, midZ, integer);
		checkPosition(midX + 1, midY, midZ + 1, integer);
		checkPosition(midX + 1, midY + 1, midZ - 1, integer);
		checkPosition(midX + 1, midY + 1, midZ, integer);
		checkPosition(midX + 1, midY + 1, midZ + 1, integer);
		
	}
	
	public void checkPosition(int x, int y, int z, int positionHash) {
		final Chunk chunkIn = toTask.parent.VKChunkCache.getChunkAt(x >> 4, z >> 4);
		
		y = Math.max(0, Math.min(y, 255));
		
		IBlockState inLocalState = chunkIn.getBlockState(x, y, z);
		
		if (inLocalState.getMaterial().isSolid()) {
			
			//Only if you want to stop short
//			foundPairs.add(positionHash);
//			foundPairs.add(x);
//			foundPairs.add(y);
//			foundPairs.add(z);
			
			inLocalPos.setPos(x, y, z);
			
			
			AxisAlignedBB inLocalBB = new AxisAlignedBB(inLocalPos.getX(), inLocalPos.getY(), inLocalPos.getZ(), inLocalPos.getX() + 1, inLocalPos.getY() + 1, inLocalPos.getZ() + 1);
			AxisAlignedBB inGlobalBB = new AxisAlignedBB(mutablePos.getX(), mutablePos.getY(), mutablePos.getZ(), mutablePos.getX() + 1, mutablePos.getY() + 1, mutablePos.getZ() + 1);
			
			//This changes the box bounding box to the real bounding box, not sure if this is better or worse for this mod
//			List<AxisAlignedBB> colBB = worldObj.getCollisionBoxes(inLocalBB);
//			inLocalBB = colBB.get(0);
			
			Polygon shipInWorld = new Polygon(inLocalBB, toTask.parent.coordTransform.lToWTransform);
			Polygon worldPoly = new Polygon(inGlobalBB);
			
			PhysPolygonCollider collider = new PhysPolygonCollider(shipInWorld, worldPoly, toTask.parent.coordTransform.normals);
			
			if (!collider.seperated) {
//	            return handleActualCollision(collider, mutablePos, inLocalPos, inWorldState, inLocalState);
				CollisionInformationHolder holder = new CollisionInformationHolder(collider, mutablePos.getX(), mutablePos.getY(), mutablePos.getZ(), inLocalPos.getX(), inLocalPos.getY(), inLocalPos.getZ(), inWorldState, inLocalState);
				
				collisionInformationGenerated.add(holder);
			}
		}
	}
	
}
