package ValkyrienWarfareBase.CoreMod;

import ValkyrienWarfareBase.Collision.EntityCollisionInjector;
import net.minecraft.entity.Entity;

public class CallRunner {

	public static void onEntityMove(Entity entity,double dx,double dy,double dz){
		if(!EntityCollisionInjector.alterEntityMovement(entity, dx, dy, dz)){
			entity.moveEntity(dx, dy, dz);
		}
	}
	
}
