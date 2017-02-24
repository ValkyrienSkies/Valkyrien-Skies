package ValkyrienWarfareBase.EntityMultiWorldFixes;

import java.util.UUID;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.CoordTransformObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareCombat.Entity.EntityCannonBall;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityDraggable {

	public PhysicsWrapperEntity worldBelowFeet;
	public UUID lastWorldBelowFeetID;
	public Vector inWorldBelowPos = new Vector();
	public Vector velocityInWorldBelow = new Vector();
	
	public Vector velocityAddedToPlayer = new Vector();
	public Entity draggableAsEntity;
	public double yawDifVelocity;
	
	
	public EntityDraggable(){
		draggableAsEntity = getEntityFromDraggable(this);
	}
	
	public static void tickAddedVelocityForWorld(World world){
		try{
			for(Entity e:world.loadedEntityList){
				//TODO: Maybe add a check to prevent moving entities that are fixed onto a Ship, but I like the visual effect
				if(!(e instanceof PhysicsWrapperEntity)&&!(e instanceof EntityCannonBall)){
					EntityDraggable draggable = getDraggableFromEntity(e);
					draggable.tickAddedVelocity();
				}
			}
		}catch(Exception e){
			e.printStackTrace(System.out);
		}
	}
	
	//TODO: Finishme
	public void tickAddedVelocity(){
		if (worldBelowFeet != null && !ValkyrienWarfareMod.physicsManager.isEntityFixed(draggableAsEntity)) {
			CoordTransformObject coordTransform = worldBelowFeet.wrapping.coordTransform;
			
			float rotYaw = draggableAsEntity.rotationYaw;
			float rotPitch = draggableAsEntity.rotationPitch;
			float prevYaw = draggableAsEntity.prevRotationYaw;
			float prevPitch = draggableAsEntity.prevRotationPitch;

			Vector oldPos = new Vector(draggableAsEntity);
			
			RotationMatrices.applyTransform(coordTransform.prevwToLTransform, coordTransform.prevWToLRotation, draggableAsEntity);
			RotationMatrices.applyTransform(coordTransform.lToWTransform, coordTransform.lToWRotation, draggableAsEntity);

			Vector newPos = new Vector(draggableAsEntity);
			
			//Move the entity back to its old position, the added velocity will be used afterwards
			draggableAsEntity.setPosition(oldPos.X, oldPos.Y, oldPos.Z);
			Vector addedVel = oldPos.getSubtraction(newPos);
			
			velocityAddedToPlayer = addedVel;
			
			draggableAsEntity.rotationYaw = rotYaw;
			draggableAsEntity.rotationPitch = rotPitch;
			draggableAsEntity.prevRotationYaw = prevYaw;
			draggableAsEntity.prevRotationPitch = prevPitch;

			Vector oldLookingPos = new Vector(draggableAsEntity.getLook(1.0F));
			RotationMatrices.applyTransform(coordTransform.prevWToLRotation, oldLookingPos);
			RotationMatrices.applyTransform(coordTransform.lToWRotation, oldLookingPos);

			double newPitch = Math.asin(oldLookingPos.Y) * -180D / Math.PI;
			double f4 = -Math.cos(-newPitch * 0.017453292D);
			double radianYaw = Math.atan2((oldLookingPos.X / f4), (oldLookingPos.Z / f4));
			radianYaw += Math.PI;
			radianYaw *= -180D / Math.PI;
			
			
			if (!(Double.isNaN(radianYaw) || Math.abs(newPitch) > 85)) {
				double wrappedYaw = MathHelper.wrapDegrees(radianYaw);
				double wrappedRotYaw = MathHelper.wrapDegrees(draggableAsEntity.rotationYaw);
				double yawDif = wrappedYaw - wrappedRotYaw;
				if (Math.abs(yawDif) > 180D) {
					if (yawDif < 0) {
						yawDif += 360D;
					} else {
						yawDif -= 360D;
					}
				}
				yawDif %= 360D;
				final double threshold = .1D;
				if (Math.abs(yawDif) < threshold) {
					yawDif = 0D;
				}
				yawDifVelocity = yawDif;
			}
		}else{
			if(draggableAsEntity.onGround){
				//TODO: Make this do friction
				velocityAddedToPlayer.zero();
				yawDifVelocity = 0;
			}
		}
		
		if (!(draggableAsEntity instanceof EntityPlayer)) {
			if (draggableAsEntity instanceof EntityArrow) {
				draggableAsEntity.prevRotationYaw = draggableAsEntity.rotationYaw;
				draggableAsEntity.rotationYaw -= yawDifVelocity;
			} else {
				draggableAsEntity.prevRotationYaw = draggableAsEntity.rotationYaw;
				draggableAsEntity.rotationYaw += yawDifVelocity;
			}
		} else {
			if (draggableAsEntity.worldObj.isRemote) {
				draggableAsEntity.prevRotationYaw = draggableAsEntity.rotationYaw;
				draggableAsEntity.rotationYaw += yawDifVelocity;
			}
		}
		
		boolean onGroundOrig = draggableAsEntity.onGround;
//		CallRunner.onEntityMove(draggableAsEntity, velocityAddedToPlayer.X, velocityAddedToPlayer.Y, velocityAddedToPlayer.Z);
		if(!ValkyrienWarfareMod.physicsManager.isEntityFixed(draggableAsEntity)){
			draggableAsEntity.moveEntity(velocityAddedToPlayer.X, velocityAddedToPlayer.Y, velocityAddedToPlayer.Z);
		}
		
		if(onGroundOrig){
			draggableAsEntity.onGround = onGroundOrig;
		}
		velocityAddedToPlayer.multiply(.99D);
		yawDifVelocity *= .95D;
	}
	
	public static EntityDraggable getDraggableFromEntity(Entity entity){
		if(entity == null){
			return null;
		}
		Object o = entity;
		return (EntityDraggable)o;
	}
	
	public static Entity getEntityFromDraggable(EntityDraggable draggable){
		if(draggable == null){
			return null;
		}
		Object o = draggable;
		return (Entity)o;
	}
	
}
