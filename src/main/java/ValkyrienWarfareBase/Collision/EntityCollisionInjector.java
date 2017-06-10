package ValkyrienWarfareBase.Collision;

import java.util.ArrayList;
import java.util.List;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Interaction.EntityDraggable;
import ValkyrienWarfareBase.Math.BigBastardMath;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class EntityCollisionInjector {

	private static final double errorSignificance = .001D;

	// Returns false if game should use default collision
	public static boolean alterEntityMovement(Entity entity, double dx, double dy, double dz) {
		if (entity instanceof PhysicsWrapperEntity) {
			return true;
		}

		Vector velVec = new Vector(dx, dy, dz);
		double origDx = dx;
		double origDy = dy;
		double origDz = dz;
		double origPosX = entity.posX;
		double origPosY = entity.posY;
		double origPosZ = entity.posZ;
		boolean isLiving = entity instanceof EntityLivingBase;
		boolean isMoving = false;
		if (isLiving) {
			EntityLivingBase living = (EntityLivingBase) entity;
			isMoving = Math.abs(living.moveForward) > .01 || Math.abs(living.moveStrafing) > .01;
		}
		Vec3d velocity = new Vec3d(dx, dy, dz);
		EntityPolygon playerBeforeMove = new EntityPolygon(entity.getEntityBoundingBox(), entity);
		ArrayList<Polygon> colPolys = getCollidingPolygonsAndDoBlockCols(entity, velocity);

		PhysicsWrapperEntity worldBelow = null;
		EntityDraggable draggable = EntityDraggable.getDraggableFromEntity(entity);

		int contX = 0;
		int contY = 0;
		int contZ = 0;
		Vector total = new Vector();

		//TODO: Make this more comprehensive
		draggable.worldBelowFeet = worldBelow;

		//Used to reset the player position after collision processing, effectively using the player to integrate their velocity
		double posOffestX = 0;
		double posOffestY = 0;
		double posOffestZ = 0;

		if(true /*entity instanceof EntityPlayer*/){
			for (Polygon poly : colPolys) {
				if (poly instanceof ShipPolygon) {
					ShipPolygon shipPoly = (ShipPolygon) poly;
					EntityPolygonCollider fast = new EntityPolygonCollider(playerBeforeMove, shipPoly, shipPoly.normals, velVec.getAddition(total));
					if (!fast.seperated) {
	//					fastCollisions.add(fast);
						worldBelow = shipPoly.shipFrom.wrapper;


						Vector response = fast.collisions[fast.minDistanceIndex].getResponse();
						// TODO: Add more potential yResponses
						double stepSquared = entity.stepHeight * entity.stepHeight;
						boolean isStep = isLiving && entity.onGround;
						if (response.Y >= 0 && BigBastardMath.canStandOnNormal(fast.potentialSeperatingAxes[fast.minDistanceIndex])) {
							response = new Vector(0, -fast.collisions[fast.minDistanceIndex].penetrationDistance / fast.potentialSeperatingAxes[fast.minDistanceIndex].Y, 0);
						}
						if (isStep) {
							EntityLivingBase living = (EntityLivingBase) entity;
							if (Math.abs(living.moveForward) > .01D || Math.abs(living.moveStrafing) > .01D) {
								for (int i = 3; i < 6; i++) {
									Vector tempResponse = fast.collisions[i].getResponse();
									if (tempResponse.Y > 0 && BigBastardMath.canStandOnNormal(fast.collisions[i].axis) && tempResponse.lengthSq() < stepSquared) {
										if(tempResponse.lengthSq() < .1D){
											//Too small to be a real step, let it through
											response = tempResponse;
										}else{
	//										System.out.println("Try Stepping!");
											AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().offset(tempResponse.X, tempResponse.Y, tempResponse.Z);
											entity.setEntityBoundingBox(axisalignedbb);
											//I think this correct, but it may create more problems than it solves
											response.zero();
											entity.resetPositionToBB();
										}
	//									entity.moveEntity(x, y, z);
	//									response = tempResponse;
									}
								}
							}
						}
						// total.add(response);
						if (Math.abs(response.X) > .01D) {
							total.X += response.X;
							contX++;
						}
						if (Math.abs(response.Y) > .01D) {
							total.Y += response.Y;
							contY++;
						}
						if (Math.abs(response.Z) > .01D) {
							total.Z += response.Z;
							contZ++;
						}
						entity.posX += response.X;
						entity.posX += response.Y;
						entity.posX += response.Z;

						posOffestX += response.X;
						posOffestY += response.Y;
						posOffestZ += response.Z;

						AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().offset(response.X, response.Y, response.Z);
						entity.setEntityBoundingBox(axisalignedbb);
						entity.resetPositionToBB();
					}
				}
			}

			AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().offset(-posOffestX, -posOffestY, -posOffestZ);
			entity.setEntityBoundingBox(axisalignedbb);
			entity.resetPositionToBB();

//			contX = contY = contZ = 1;
		}


		draggable.worldBelowFeet = worldBelow;

		if(worldBelow == null){
			return false;
		}

		/*if(!(entity instanceof EntityPlayer)){
			for (EntityPolygonCollider col : fastCollisions) {
				Vector response = col.collisions[col.minDistanceIndex].getResponse();
				// TODO: Add more potential yResponses
				double stepSquared = entity.stepHeight * entity.stepHeight;
				boolean isStep = isLiving && entity.onGround;
				if (response.Y >= 0 && BigBastardMath.canStandOnNormal(col.potentialSeperatingAxes[col.minDistanceIndex])) {
					response = new Vector(0, -col.collisions[col.minDistanceIndex].penetrationDistance / col.potentialSeperatingAxes[col.minDistanceIndex].Y, 0);
				}
				if (isStep) {
					EntityLivingBase living = (EntityLivingBase) entity;
					if (Math.abs(living.moveForward) > .01D || Math.abs(living.moveStrafing) > .01D) {
						for (int i = 3; i < 6; i++) {
							Vector tempResponse = col.collisions[i].getResponse();
							if (tempResponse.Y > 0 && BigBastardMath.canStandOnNormal(col.collisions[i].axis) && tempResponse.lengthSq() < stepSquared) {
								if(tempResponse.lengthSq() < .1D){
									//Too small to be a real step, let it through
									response = tempResponse;
								}else{
	//								System.out.println("Try Stepping!");
									AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().offset(tempResponse.X, tempResponse.Y, tempResponse.Z);
									entity.setEntityBoundingBox(axisalignedbb);
									//I think this correct, but it may create more problems than it solves
									response.zero();
								}
	//							entity.moveEntity(x, y, z);
	//							response = tempResponse;
							}
						}
					}
				}
				// total.add(response);
				if (Math.abs(response.X) > .01D) {
					total.X += response.X;
					contX++;
				}
				if (Math.abs(response.Y) > .01D) {
					total.Y += response.Y;
					contY++;
				}
				if (Math.abs(response.Z) > .01D) {
					total.Z += response.Z;
					contZ++;
				}
			}

		}

		if (contX != 0) {
			total.X /= contX;
		}
		if (contY != 0) {
			total.Y /= contY;
		}
		if (contZ != 0) {
			total.Z /= contZ;
		}

		*/

		dx += total.X;
		dy += total.Y;
		dz += total.Z;

		boolean alreadyOnGround = entity.onGround && (dy == origDy) && origDy < 0;
		Vector original = new Vector(origDx, origDy, origDz);
		Vector newMov = new Vector(dx - origDx, dy - origDy, dz - origDz);
		entity.isCollidedHorizontally = original.dot(newMov) < 0;
		entity.isCollidedVertically = isDifSignificant(dy, origDy);
		entity.onGround = entity.isCollidedVertically && origDy < 0 || alreadyOnGround;
		entity.isCollided = entity.isCollidedHorizontally || entity.isCollidedVertically;

		entity.resetPositionToBB();

		double motionYBefore = entity.motionY;
		float oldFallDistance = entity.fallDistance;

		if (entity instanceof EntityLivingBase) {
			EntityLivingBase base = (EntityLivingBase) entity;
			base.motionY = dy;
			if (base.isOnLadder()) {

				float f9 = 0.15F;
				base.motionX = MathHelper.clamp_double(base.motionX, -0.15000000596046448D, 0.15000000596046448D);
				base.motionZ = MathHelper.clamp_double(base.motionZ, -0.15000000596046448D, 0.15000000596046448D);
				base.fallDistance = 0.0F;

				if (base.motionY < -0.15D) {
					base.motionY = -0.15D;
				}

				boolean flag = base.isSneaking() && base instanceof EntityPlayer;

				if (flag && base.motionY < 0.0D) {
					base.motionY = 0.0D;
				}
			}
			entity.moveEntity(dx, base.motionY, dz);
		} else {
			entity.moveEntity(dx, dy, dz);
		}


		entity.isCollidedHorizontally = (motionInterfering(dx, origDx)) || (motionInterfering(dz, origDz));
		entity.isCollidedVertically = isDifSignificant(dy, origDy);
		entity.onGround = entity.isCollidedVertically && origDy < 0 || alreadyOnGround || entity.onGround;
		entity.isCollided = entity.isCollidedHorizontally || entity.isCollidedVertically;



		Vector entityPosInShip = new Vector(entity.posX, entity.posY - 0.20000000298023224D, entity.posZ, worldBelow.wrapping.coordTransform.wToLTransform);

		int j4 = MathHelper.floor_double(entityPosInShip.X);
        int l4 = MathHelper.floor_double(entityPosInShip.Y);
        int i5 = MathHelper.floor_double(entityPosInShip.Z);
        BlockPos blockpos = new BlockPos(j4, l4, i5);
        IBlockState iblockstate = entity.worldObj.getBlockState(blockpos);

        Block block = iblockstate.getBlock();

        //TODO: Use Mixins to call Entity.updateFallState() instead!

        //Fixes slime blocks
      	if(block instanceof BlockSlime && !entity.isInWeb){
      		entity.motionY = motionYBefore;
      	}

    	entity.fallDistance = oldFallDistance;
        if(entity instanceof EntityLivingBase){

	        if (!entity.worldObj.isRemote && entity.fallDistance > 3.0F && entity.onGround)
	        {
//	        	System.out.println("LAND DAMNIT!");
	            float f = (float)MathHelper.ceiling_float_int(entity.fallDistance - 3.0F);
	            if (!iblockstate.getBlock().isAir(iblockstate, entity.worldObj, blockpos))
	            {
	                double d0 = Math.min((double)(0.2F + f / 15.0F), 2.5D);

	                int i = (int)(150.0D * d0);
	                if (!iblockstate.getBlock().addLandingEffects(iblockstate, (WorldServer)entity.worldObj, blockpos, iblockstate, (EntityLivingBase)entity, i))
	                ((WorldServer)entity.worldObj).spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, new int[] {Block.getStateId(iblockstate)});
	            }
	        }
        }

        if (entity.onGround)
        {
            if (entity.fallDistance > 0.0F)
            {
            	//Responsible for breaking crops when you jump on them
            	iblockstate.getBlock().onFallenUpon(entity.worldObj, blockpos, entity, entity.fallDistance);
            }

            entity.fallDistance = 0.0F;
        }
        else if (entity.motionY < 0.0D)
        {
        	entity.fallDistance = (float)((double)entity.fallDistance - entity.motionY);
        }

        if (/**entity.canTriggerWalking()**/ entity instanceof EntityPlayer && !entity.isRiding())
        {
            double d12 = entity.posX - origPosX;
            double d13 = entity.posY - origPosY;
            double d14 = entity.posZ - origPosZ;

            if (block != Blocks.LADDER)
            {
                d13 = 0.0D;
            }

            if (dy != origDy) {
//    			if (!(entity.motionY > 0 && dy > 0)) {
    				block.onLanded(entity.worldObj, entity);
//    			}
    		}

            if (block != null && entity.onGround)
            {
                block.onEntityWalk(entity.worldObj, blockpos, entity);
            }

//            entity.distanceWalkedModified = (float)((double)entity.distanceWalkedModified + (double)MathHelper.sqrt_double(d12 * d12 + d14 * d14) * 0.6D);
//            entity.distanceWalkedOnStepModified = (float)((double)entity.distanceWalkedOnStepModified + (double)MathHelper.sqrt_double(d12 * d12 + d13 * d13 + d14 * d14) * 0.6D);

            if (entity.distanceWalkedOnStepModified > (float)entity.nextStepDistance && iblockstate.getMaterial() != Material.AIR)
            {
            	entity.nextStepDistance = (int)entity.distanceWalkedOnStepModified + 1;

                /*if (this.isInWater())
                {
                    float f = MathHelper.sqrt_double(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.35F;

                    if (f > 1.0F)
                    {
                        f = 1.0F;
                    }

                    this.playSound(this.getSwimSound(), f, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                }*/

//            	System.out.println("Play a sound!");
//              entity.playStepSound(blockpos, block);

            	//TODO: In future, replace this with entity.playStepSound()
            	SoundType soundtype = block.getSoundType(entity.worldObj.getBlockState(blockpos), entity.worldObj, blockpos, entity);

                if (entity.worldObj.getBlockState(blockpos.up()).getBlock() == Blocks.SNOW_LAYER)
                {
                    soundtype = Blocks.SNOW_LAYER.getSoundType();
                    entity.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
                }
                else if (!block.getDefaultState().getMaterial().isLiquid())
                {
                	entity.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
                }
            }
        }


		if (dx != origDx) {
			entity.motionX = dx;
		}
		if (dy != origDy) {
			if (!(entity.motionY > 0 && dy > 0)) {
//				entity.motionY = 0;
			}
		}
		if (dz != origDz) {
			entity.motionZ = dz;
		}

		return true;
	}

	/*
	 * This method generates an arrayList of Polygons that the player is colliding with
	 */
	public static ArrayList<Polygon> getCollidingPolygonsAndDoBlockCols(Entity entity, Vec3d velocity) {
		ArrayList<Polygon> collisions = new ArrayList<Polygon>();
		AxisAlignedBB entityBB = entity.getEntityBoundingBox().addCoord(velocity.xCoord, velocity.yCoord, velocity.zCoord).expand(1, 1, 1);

		WorldPhysObjectManager localPhysManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(entity.worldObj);

		List<PhysicsWrapperEntity> ships = localPhysManager.getNearbyPhysObjects(entityBB);
		//If a player is riding a Ship, don't process any collision between that Ship and the Player
		for (PhysicsWrapperEntity wrapper : ships) {
			if(!entity.isRidingSameEntity(wrapper)){
				Polygon playerInLocal = new Polygon(entityBB, wrapper.wrapping.coordTransform.wToLTransform);
				AxisAlignedBB bb = playerInLocal.getEnclosedAABB();

				if((bb.maxX - bb.minX)*(bb.maxZ - bb.minZ) > 9898989){
					//This is too big, something went wrong here
					break;
				}
				List<AxisAlignedBB> collidingBBs = entity.worldObj.getCollisionBoxes(bb);

				// TODO: Fix the performance of this!
				if (entity.worldObj.isRemote || entity instanceof EntityPlayer) {
					BigBastardMath.mergeAABBList(collidingBBs);
				}

				for (AxisAlignedBB inLocal : collidingBBs) {
					ShipPolygon poly = new ShipPolygon(inLocal, wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.normals, wrapper.wrapping);
					collisions.add(poly);
				}
			}
		}

		for(PhysicsWrapperEntity wrapper : ships){
			if(!entity.isRidingSameEntity(wrapper)){
				double posX = entity.posX;
				double posY = entity.posY;
				double posZ = entity.posZ;

				Vector entityPos = new Vector(posX, posY, posZ);
				RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, entityPos);

				setEntityPositionAndUpdateBB(entity, entityPos.X, entityPos.Y, entityPos.Z);

				int entityChunkX = MathHelper.floor_double(entity.posX / 16.0D);
				int entityChunkZ = MathHelper.floor_double(entity.posZ / 16.0D);

				if(wrapper.wrapping.ownsChunk(entityChunkX, entityChunkZ)){
					Chunk chunkIn = wrapper.wrapping.claimedChunks[entityChunkX-wrapper.wrapping.claimedChunks[0][0].xPosition][entityChunkZ-wrapper.wrapping.claimedChunks[0][0].zPosition];

					int chunkYIndex = MathHelper.floor_double(entity.posY / 16.0D);

			        if (chunkYIndex < 0)
			        {
			        	chunkYIndex = 0;
			        }

			        if (chunkYIndex >= chunkIn.entityLists.length)
			        {
			        	chunkYIndex = chunkIn.entityLists.length - 1;
			        }

					chunkIn.entityLists[chunkYIndex].add(entity);
					entity.doBlockCollisions();
					chunkIn.entityLists[chunkYIndex].remove(entity);
				}

				setEntityPositionAndUpdateBB(entity, posX, posY, posZ);
			}
		}

		return collisions;
	}

    public static void setEntityPositionAndUpdateBB(Entity entity, double x, double y, double z){
    	entity.posX = x;
    	entity.posY = y;
    	entity.posZ = z;
        float f = entity.width / 2.0F;
        float f1 = entity.height;
        entity.boundingBox = new AxisAlignedBB(x - (double)f, y, z - (double)f, x + (double)f, y + (double)f1, z + (double)f);
    }

	private static boolean isDifSignificant(double dif1, double d2) {
		if (Math.abs(dif1 - d2) < errorSignificance) {
			return false;
		} else {
			return true;
		}
	}

	private static boolean motionInterfering(double orig, double modded) {
		return Math.signum(orig) != Math.signum(modded);
	}

}
