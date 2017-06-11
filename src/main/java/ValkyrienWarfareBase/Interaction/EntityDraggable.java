package ValkyrienWarfareBase.Interaction;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareCombat.Entity.EntityCannonBall;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public abstract class EntityDraggable {
	public static void tickAddedVelocityForWorld(World world){
		try{
			//TODO: Fix this
			if(true){
				return;
			}
			for(int i = 0;i < world.loadedEntityList.size(); i++){
				Entity e = world.loadedEntityList.get(i);
				//TODO: Maybe add a check to prevent moving entities that are fixed onto a Ship, but I like the visual effect
				if(!(e instanceof PhysicsWrapperEntity)&&!(e instanceof EntityCannonBall)){
					IDraggable draggable = getDraggableFromEntity(e);
					draggable.tickAddedVelocity();

					if(draggable.getWorldBelowFeet() == null){
						if(e.onGround){
							draggable.getVelocityAddedToPlayer().zero();
							draggable.setYawDifVelocity(0);
						}else{
							if(e instanceof EntityPlayer){
								EntityPlayer player = (EntityPlayer) e;
								if(player.isCreative() && player.capabilities.isFlying){
									draggable.getVelocityAddedToPlayer().multiply(.99D * .95D);
									draggable.setYawDifVelocity(draggable.getYawDifVelocity() * .95D * .95D);
								}
							}
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static IDraggable getDraggableFromEntity(Entity entity){
		if(entity == null){
			return null;
		}
		Object o = entity;
		return (IDraggable) o;
	}

	public static Entity getEntityFromDraggable(IDraggable draggable){
		if(draggable == null){
			return null;
		}
		Object o = draggable;
		return (Entity)o;
	}
}
