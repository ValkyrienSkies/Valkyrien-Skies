package ValkyrienWarfareBase.PhysicsManagement;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class PhysObjectManager {

	private static double ShipRangeCheck = 120D;
	
	public static List<PhysicsWrapperEntity> getNearbyPhysObjects(World world,AxisAlignedBB toCheck){
		ArrayList<PhysicsWrapperEntity> ships = new ArrayList<PhysicsWrapperEntity>();
		
		AxisAlignedBB chunkBB = toCheck.expand(ShipRangeCheck, ShipRangeCheck, ShipRangeCheck);
		List<PhysicsWrapperEntity> rawEntities = world.getEntitiesWithinAABB(PhysicsWrapperEntity.class, chunkBB);
		
		AxisAlignedBB expandedCheck = toCheck.expand(6, 6, 6);
		
		for(PhysicsWrapperEntity wrapper:rawEntities){
			if(wrapper.wrapping.collisionBB.intersectsWith(expandedCheck)){
				ships.add(wrapper);
			}
		}
		
		return ships;
	}
	
}
