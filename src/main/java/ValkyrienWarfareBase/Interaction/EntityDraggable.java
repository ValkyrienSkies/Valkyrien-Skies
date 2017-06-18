package ValkyrienWarfareBase.Interaction;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.CoordTransformObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareCombat.Entity.EntityCannonBall;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class EntityDraggable {
	public static void tickAddedVelocityForWorld(World world){
		try{
			//TODO: Fix this
			if(true){
//				return;
			}
			for(int i = 0;i < world.loadedEntityList.size(); i++){
				Entity e = world.loadedEntityList.get(i);
				//TODO: Maybe add a check to prevent moving entities that are fixed onto a Ship, but I like the visual effect
				if(!(e instanceof PhysicsWrapperEntity)&&!(e instanceof EntityCannonBall)){
					IDraggable draggable = getDraggableFromEntity(e);
//					e.onGround = true;
//
					doTheEntityThing(e);

//					draggable.tickAddedVelocity();
//
//					e.onGround = true;
//					e.setPosition(draggable.getVelocityAddedToPlayer().X + e.posX, draggable.getVelocityAddedToPlayer().Y + e.posY, draggable.getVelocityAddedToPlayer().Z + e.posZ);

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

	public static void doTheEntityThing(Entity entity){
		IDraggable draggable = EntityDraggable.getDraggableFromEntity(entity);
		if (draggable.getWorldBelowFeet() != null && !ValkyrienWarfareMod.physicsManager.isEntityFixed(entity)) {
            CoordTransformObject coordTransform = draggable.getWorldBelowFeet().wrapping.coordTransform;

            float rotYaw = entity.rotationYaw;
            float rotPitch = entity.rotationPitch;
            float prevYaw = entity.prevRotationYaw;
            float prevPitch = entity.prevRotationPitch;

            Vector oldPos = new Vector(entity);

            RotationMatrices.applyTransform(coordTransform.prevwToLTransform, coordTransform.prevWToLRotation, entity);
            RotationMatrices.applyTransform(coordTransform.lToWTransform, coordTransform.lToWRotation, entity);

            Vector newPos = new Vector(entity);

            //Move the entity back to its old position, the added velocity will be used afterwards
            entity.setPosition(oldPos.X, oldPos.Y, oldPos.Z);
            Vector addedVel = oldPos.getSubtraction(newPos);

            draggable.setVelocityAddedToPlayer(addedVel);

            entity.rotationYaw = rotYaw;
            entity.rotationPitch = rotPitch;
            entity.prevRotationYaw = prevYaw;
            entity.prevRotationPitch = prevPitch;

            Vector oldLookingPos = new Vector(entity.getLook(1.0F));
            RotationMatrices.applyTransform(coordTransform.prevWToLRotation, oldLookingPos);
            RotationMatrices.applyTransform(coordTransform.lToWRotation, oldLookingPos);

            double newPitch = Math.asin(oldLookingPos.Y) * -180D / Math.PI;
            double f4 = -Math.cos(-newPitch * 0.017453292D);
            double radianYaw = Math.atan2((oldLookingPos.X / f4), (oldLookingPos.Z / f4));
            radianYaw += Math.PI;
            radianYaw *= -180D / Math.PI;


            if (!(Double.isNaN(radianYaw) || Math.abs(newPitch) > 85)) {
                double wrappedYaw = MathHelper.wrapDegrees(radianYaw);
                double wrappedRotYaw = MathHelper.wrapDegrees(entity.rotationYaw);
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
                draggable.setYawDifVelocity(yawDif);
            }
        }

        boolean onGroundOrig = entity.onGround;

        if (!ValkyrienWarfareMod.physicsManager.isEntityFixed(entity)) {
            float originalWalked = entity.distanceWalkedModified;
            float originalWalkedOnStep = entity.distanceWalkedOnStepModified;
            boolean originallySneaking = entity.isSneaking();

            entity.setSneaking(false);

            if (entity.world.isRemote && EntityPlayerSP.class.isInstance(entity)) {
                EntityPlayerSP playerSP = EntityPlayerSP.class.cast(entity);
                MovementInput moveInput = playerSP.movementInput;
                originallySneaking = moveInput.sneak;
                moveInput.sneak = false;
            }


            if(draggable.getWorldBelowFeet() == null && entity.onGround){
            	draggable.getVelocityAddedToPlayer().zero();
            }

//            entity.move(MoverType.SELF, draggable.getVelocityAddedToPlayer().X, draggable.getVelocityAddedToPlayer().Y, draggable.getVelocityAddedToPlayer().Z);

            entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(draggable.getVelocityAddedToPlayer().X, draggable.getVelocityAddedToPlayer().Y, draggable.getVelocityAddedToPlayer().Z));
            entity.resetPositionToBB();

            if (!(EntityPlayerSP.class.isInstance(entity))) {
                if (EntityArrow.class.isInstance(entity)) {
                	entity.prevRotationYaw = entity.rotationYaw;
                	entity.rotationYaw -= draggable.getYawDifVelocity();
                } else {
                	entity.prevRotationYaw = entity.rotationYaw;
                	entity.rotationYaw += draggable.getYawDifVelocity();
                }
            } else {
                if (entity.world.isRemote) {
                	entity.prevRotationYaw = entity.rotationYaw;
                	entity.rotationYaw += draggable.getYawDifVelocity();
                }
            }

            //Do not add this movement as if the entity were walking it
            entity.distanceWalkedModified = originalWalked;
            entity.distanceWalkedOnStepModified = originalWalkedOnStep;
            entity.setSneaking(originallySneaking);

            if (entity.world.isRemote && EntityPlayerSP.class.isInstance(entity)) {
                EntityPlayerSP playerSP = EntityPlayerSP.class.cast(entity);
                MovementInput moveInput = playerSP.movementInput;
                moveInput.sneak = originallySneaking;
            }
        }

        if (onGroundOrig) {
        	entity.onGround = onGroundOrig;
        }

        draggable.getVelocityAddedToPlayer().multiply(.99D);
        draggable.setYawDifVelocity(draggable.getYawDifVelocity() * .95D);
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
