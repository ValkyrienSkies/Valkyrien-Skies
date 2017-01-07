package ValkyrienWarfareBase.PhysicsManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ValkyrienWarfareBase.API.Vector;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * This class essentially handles all the issues with ticking and handling Physics Objects in the given world
 * 
 * @author thebest108
 *
 */
public class WorldPhysObjectManager {

	// private static double ShipRangeCheck = 120D;
	public World worldObj;
	public ArrayList<PhysicsWrapperEntity> physicsEntities = new ArrayList<PhysicsWrapperEntity>();
	public ArrayList<PhysicsWrapperEntity> physicsEntitiesToUnload = new ArrayList<PhysicsWrapperEntity>();
	public ArrayList<Callable<Void>> physCollisonCallables = new ArrayList<Callable<Void>>();

	public WorldPhysObjectManager(World toManage) {
		worldObj = toManage;
	}

	public void onLoad(PhysicsWrapperEntity loaded) {
		if (!loaded.wrapping.fromSplit) {
			physicsEntities.add(loaded);
			physCollisonCallables.add(loaded.wrapping.collisionCallable);
		} else {
			// reset check to prevent strange errors
			loaded.wrapping.fromSplit = false;
		}
	}

	public void onUnload(PhysicsWrapperEntity loaded) {
		physicsEntities.remove(loaded);
		physCollisonCallables.remove(loaded.wrapping.collisionCallable);
		loaded.wrapping.onThisUnload();
	}

	public PhysicsWrapperEntity getManagingObjectForChunk(Chunk chunk) {
		for (PhysicsWrapperEntity wrapper : physicsEntities) {
			if (wrapper.wrapping.ownsChunk(chunk.xPosition, chunk.zPosition)) {
				return wrapper;
			}
		}
		return null;
	}

	public List<PhysicsWrapperEntity> getNearbyPhysObjects(AxisAlignedBB toCheck) {
		ArrayList<PhysicsWrapperEntity> ships = new ArrayList<PhysicsWrapperEntity>();

		AxisAlignedBB expandedCheck = toCheck.expand(6, 6, 6);

		for (PhysicsWrapperEntity wrapper : physicsEntities) {
			if (wrapper.wrapping.collisionBB.intersectsWith(expandedCheck)) {
				ships.add(wrapper);
			}
		}

		return ships;
	}

	public boolean isEntityFixed(Entity entity) {
		if (getShipFixedOnto(entity) != null) {
			return true;
		}
		return false;
	}

	public PhysicsWrapperEntity getShipFixedOnto(Entity entity) {
		for (PhysicsWrapperEntity wrapper : physicsEntities) {
			if (wrapper.wrapping.entityLocalPositions.containsKey(entity.getPersistentID().hashCode())) {
				if (wrapper.riddenByEntities.contains(entity)) {
					return wrapper;
				}
			}
		}
		return null;
	}

}
