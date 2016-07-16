package ValkyrienWarfareBase.PhysicsManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * This class essentially handles all the issues with ticking and handling Physics
 * Objects in the given world
 * @author thebest108
 *
 */
public class WorldPhysObjectManager {

//	private static double ShipRangeCheck = 120D;
	public World worldObj;
	public ArrayList<PhysicsWrapperEntity> physicsEntities = new ArrayList<PhysicsWrapperEntity>();
	public ArrayList<PhysicsWrapperEntity> physicsEntitiesToUnload = new ArrayList<PhysicsWrapperEntity>();
	public ArrayList<Callable<Void>> physCollisonCallables = new ArrayList<Callable<Void>>();
	public int physIter = 5;
	public double physSpeed = .05D;
	
	public WorldPhysObjectManager(World toManage){
		worldObj = toManage;
	}
	
	public void onLoad(PhysicsWrapperEntity loaded){
		physicsEntities.add(loaded);
		physCollisonCallables.add(loaded.wrapping.collisionCallable);
	}
	
	public void onUnload(PhysicsWrapperEntity loaded){
		physicsEntities.remove(loaded);
		physCollisonCallables.remove(loaded.wrapping.collisionCallable);
		loaded.wrapping.onThisUnload();
	}
	
	public PhysicsWrapperEntity getManagingObjectForChunk(Chunk chunk){
		for(PhysicsWrapperEntity wrapper:physicsEntities){
			if(wrapper.wrapping.ownsChunk(chunk.xPosition,chunk.zPosition)){
				return wrapper;
			}
		}
		return null;
	}
	
	public List<PhysicsWrapperEntity> getNearbyPhysObjects(World world,AxisAlignedBB toCheck){
		ArrayList<PhysicsWrapperEntity> ships = new ArrayList<PhysicsWrapperEntity>();
		
		AxisAlignedBB expandedCheck = toCheck.expand(6, 6, 6);
		
		for(PhysicsWrapperEntity wrapper:physicsEntities){
			if(wrapper.wrapping.collisionBB.intersectsWith(expandedCheck)){
				ships.add(wrapper);
			}
		}
		
		return ships;
	}
	
}
