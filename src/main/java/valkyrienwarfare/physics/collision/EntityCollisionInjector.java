/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.physics.collision;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.math.BigBastardMath;
import valkyrienwarfare.mod.physmanagement.interaction.EntityDraggable;
import valkyrienwarfare.mod.physmanagement.interaction.IDraggable;
import valkyrienwarfare.physics.data.PhysicsQueuedForce;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.physics.management.WorldPhysObjectManager;

public class EntityCollisionInjector {

	private static final double errorSignificance = .001D;

	// Returns false if game should use default collision
	public static IntermediateMovementVariableStorage alterEntityMovement(Entity entity, MoverType type, double dx,
			double dy, double dz) {

		Vector velVec = new Vector(dx, dy, dz);
		double origDx = dx;
		double origDy = dy;
		double origDz = dz;
		double origPosX = entity.posX;
		double origPosY = entity.posY;
		double origPosZ = entity.posZ;
		boolean isLiving = entity instanceof EntityLivingBase;
		Vec3d velocity = new Vec3d(dx, dy, dz);
		EntityPolygon playerBeforeMove = new EntityPolygon(entity.getEntityBoundingBox(), entity);
		ArrayList<Polygon> colPolys = getCollidingPolygonsAndDoBlockCols(entity, velocity);

		PhysicsWrapperEntity worldBelow = null;
		IDraggable draggable = EntityDraggable.getDraggableFromEntity(entity);

		Vector total = new Vector();

		// Used to reset the player position after collision processing, effectively
		// using the player to integrate their velocity
		double posOffestX = 0;
		double posOffestY = 0;
		double posOffestZ = 0;

		for (Polygon poly : colPolys) {
			if (poly instanceof ShipPolygon) {
				ShipPolygon shipPoly = (ShipPolygon) poly;
				try {

					EntityPolygonCollider fast = new EntityPolygonCollider(playerBeforeMove, shipPoly, shipPoly.normals,
							velVec.getAddition(total));
					if (!fast.arePolygonsSeperated()) {
						// fastCollisions.add(fast);
						worldBelow = shipPoly.shipFrom.wrapper;

						Vector response = fast.getCollisions()[fast.getMinDistanceIndex()].getResponse();
						// TODO: Add more potential yResponses
						double stepSquared = entity.stepHeight * entity.stepHeight;
						boolean isStep = isLiving && entity.onGround;
						if (response.Y >= 0 && BigBastardMath
								.canStandOnNormal(fast.getCollisionAxes()[fast.getMinDistanceIndex()])) {
							response = new Vector(0, -fast.getCollisions()[fast.getMinDistanceIndex()].getCollisionPenetrationDistance()
									/ fast.getCollisionAxes()[fast.getMinDistanceIndex()].Y, 0);
						}
						if (isStep) {
							EntityLivingBase living = (EntityLivingBase) entity;
							if (Math.abs(living.moveForward) > .01D || Math.abs(living.moveStrafing) > .01D) {
								for (int i = 3; i < 6; i++) {
									Vector tempResponse = fast.getCollisions()[i].getResponse();
									if (tempResponse.Y > 0 && BigBastardMath.canStandOnNormal(fast.getCollisions()[i].getCollisionNormal())
											&& tempResponse.lengthSq() < stepSquared) {
										if (tempResponse.lengthSq() < .1D) {
											// Too small to be a real step, let it through
											response = tempResponse;
										} else {
											// System.out.println("Try Stepping!");
											AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox()
													.offset(tempResponse.X, tempResponse.Y, tempResponse.Z);
											entity.setEntityBoundingBox(axisalignedbb);
											// I think this correct, but it may create more problems than it solves
											response.zero();
											entity.resetPositionToBB();
										}
										// entity.moveEntity(x, y, z);
										// response = tempResponse;
									}
								}
							}
						}
						// total.add(response);
						if (Math.abs(response.X) > .01D) {
							total.X += response.X;
						}
						if (Math.abs(response.Y) > .01D) {
							total.Y += response.Y;
						}
						if (Math.abs(response.Z) > .01D) {
							total.Z += response.Z;
						}

						entity.posX += response.X;
						entity.posY += response.Y;
						entity.posZ += response.Z;

						posOffestX += response.X;
						posOffestY += response.Y;
						posOffestZ += response.Z;

						AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().offset(response.X, response.Y,
								response.Z);
						entity.setEntityBoundingBox(axisalignedbb);
						entity.resetPositionToBB();

					}
				} catch (Exception e) {

				}
			}

		}

		AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().offset(-posOffestX, -posOffestY, -posOffestZ);
		entity.setEntityBoundingBox(axisalignedbb);
		entity.resetPositionToBB();

		draggable.setWorldBelowFeet(worldBelow);

		if (worldBelow == null) {
			return null;
		}

		dx += total.X;
		dy += total.Y;
		dz += total.Z;

		boolean alreadyOnGround = entity.onGround && (dy == origDy) && origDy < 0;
		Vector original = new Vector(origDx, origDy, origDz);
		Vector newMov = new Vector(dx - origDx, dy - origDy, dz - origDz);
		entity.collidedHorizontally = original.dot(newMov) < 0;
		entity.collidedVertically = isDifSignificant(dy, origDy);
		entity.onGround = entity.collidedVertically && origDy < 0 || alreadyOnGround;
		entity.collided = entity.collidedHorizontally || entity.collidedVertically;

		// entity.resetPositionToBB();

		double motionYBefore = entity.motionY;
		float oldFallDistance = entity.fallDistance;

		Vector dxyz;

		if (entity instanceof EntityLivingBase) {
			EntityLivingBase base = (EntityLivingBase) entity;
			base.motionY = dy;
			if (base.isOnLadder()) {

				base.motionX = MathHelper.clamp(base.motionX, -0.15000000596046448D, 0.15000000596046448D);
				base.motionZ = MathHelper.clamp(base.motionZ, -0.15000000596046448D, 0.15000000596046448D);
				base.fallDistance = 0.0F;

				if (base.motionY < -0.15D) {
					base.motionY = -0.15D;
				}

				boolean flag = base.isSneaking() && base instanceof EntityPlayer;

				if (flag && base.motionY < 0.0D) {
					base.motionY = 0.0D;
				}
			}
			dxyz = new Vector(dx, base.motionY, dz);
		} else {
			dxyz = new Vector(dx, dy, dz);
		}

		Vector origDxyz = new Vector(origDx, origDy, origDz);
		Vector origPosXyz = new Vector(origPosX, origPosY, origPosZ);

		if (worldBelow != null && false) {
			double playerMass = 100D;
			Vector impulse = new Vector(total);
			Vector inBodyPos = new Vector(entity.posX, entity.posY, entity.posZ);

			// inBodyPos.transform(worldBelow.wrapping.coordTransform.wToLRotation);
			// impulse.transform(worldBelow.wrapping.coordTransform.wToLRotation);

			impulse.multiply(playerMass * -100D);
			// impulse.multiply();

			PhysicsQueuedForce queuedForce = new PhysicsQueuedForce(impulse, inBodyPos, false, 1);

			worldBelow.wrapping.queueForce(queuedForce);
		}

		return new IntermediateMovementVariableStorage(dxyz, origDxyz, origPosXyz, alreadyOnGround, motionYBefore,
				oldFallDistance);
	}

	public static void alterEntityMovementPost(Entity entity, IntermediateMovementVariableStorage storage) {
		double dx = storage.dxyz.X;
		double dy = storage.dxyz.Y;
		double dz = storage.dxyz.Z;

		double origDx = storage.origDxyz.X;
		double origDy = storage.origDxyz.Y;
		double origDz = storage.origDxyz.Z;

		double origPosX = storage.origPosXyz.X;
		double origPosY = storage.origPosXyz.Y;
		double origPosZ = storage.origPosXyz.Z;

		boolean alreadyOnGround = storage.alreadyOnGround;
		double motionYBefore = storage.motionYBefore;
		float oldFallDistance = storage.oldFallDistance;

		IDraggable draggable = EntityDraggable.getDraggableFromEntity(entity);

		PhysicsWrapperEntity worldBelow = draggable.getWorldBelowFeet();

		entity.collidedHorizontally = (motionInterfering(dx, origDx)) || (motionInterfering(dz, origDz));
		entity.collidedVertically = isDifSignificant(dy, origDy);
		entity.onGround = entity.collidedVertically && origDy < 0 || alreadyOnGround || entity.onGround;
		entity.collided = entity.collidedHorizontally || entity.collidedVertically;

		Vector entityPosInShip = new Vector(entity.posX, entity.posY - 0.20000000298023224D, entity.posZ,
				worldBelow.wrapping.coordTransform.wToLTransform);

		int j4 = MathHelper.floor(entityPosInShip.X);
		int l4 = MathHelper.floor(entityPosInShip.Y);
		int i5 = MathHelper.floor(entityPosInShip.Z);
		BlockPos blockpos = new BlockPos(j4, l4, i5);
		IBlockState iblockstate = entity.world.getBlockState(blockpos);

		Block block = iblockstate.getBlock();

		// TODO: Use Mixins to call entity.updateFallState() instead!

		// fixes slime blocks
		if (block instanceof BlockSlime && !entity.isInWeb) {
			entity.motionY = motionYBefore;
		}

		entity.fallDistance = oldFallDistance;
		if (entity instanceof EntityLivingBase) {

			if (!entity.world.isRemote && entity.fallDistance > 3.0F && entity.onGround) {
				// System.out.println("LAND DAMNIT!");
				float f = MathHelper.ceil(entity.fallDistance - 3.0F);
				if (!iblockstate.getBlock().isAir(iblockstate, entity.world, blockpos)) {
					double d0 = Math.min(0.2F + f / 15.0F, 2.5D);

					int i = (int) (150.0D * d0);
					if (!iblockstate.getBlock().addLandingEffects(iblockstate, (WorldServer) entity.world, blockpos,
							iblockstate, (EntityLivingBase) entity, i))
						((WorldServer) entity.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX,
								entity.posY, entity.posZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D,
								Block.getStateId(iblockstate));
				}
			}
		}

		if (entity.onGround) {
			if (entity.fallDistance > 0.0F) {
				// Responsible for breaking crops when you jump on them
				iblockstate.getBlock().onFallenUpon(entity.world, blockpos, entity, entity.fallDistance);
			}

			entity.fallDistance = 0.0F;
		} else if (entity.motionY < 0.0D) {
			entity.fallDistance = (float) (entity.fallDistance - entity.motionY);
		}

		if (/** entity.canTriggerWalking() **/
		entity instanceof EntityPlayer && !entity.isRiding()) {
			if (dy != origDy) {
				// if (!(entity.motionY > 0 && dy > 0)) {
				block.onLanded(entity.world, entity);
				// }
			}

			if (block != null && entity.onGround) {
				block.onEntityWalk(entity.world, blockpos, entity);
			}

			// entity.distanceWalkedModified = (float)((double)entity.distanceWalkedModified
			// + (double)MathHelper.sqrt_double(d12 * d12 + d14 * d14) * 0.6D);
			// entity.distanceWalkedOnStepModified =
			// (float)((double)entity.distanceWalkedOnStepModified +
			// (double)MathHelper.sqrt_double(d12 * d12 + d13 * d13 + d14 * d14) * 0.6D);

			if (entity.distanceWalkedOnStepModified > entity.nextStepDistance
					&& iblockstate.getMaterial() != Material.AIR) {
				entity.nextStepDistance = (int) entity.distanceWalkedOnStepModified + 1;

				/*
				 * if (this.isInWater()) { float f = MathHelper.sqrt_double(this.motionX *
				 * this.motionX * 0.20000000298023224D + this.motionY * this.motionY +
				 * this.motionZ * this.motionZ * 0.20000000298023224D) * 0.35F;
				 * 
				 * if (f > 1.0F) { f = 1.0F; }
				 * 
				 * this.playSound(this.getSwimSound(), f, 1.0F + (this.rand.nextFloat() -
				 * this.rand.nextFloat()) * 0.4F); }
				 */

				// System.out.println("Play a sound!");
				// entity.playStepSound(blockpos, block);

				// TODO: In future, replace this with entity.playStepSound()
				SoundType soundtype = block.getSoundType(entity.world.getBlockState(blockpos), entity.world, blockpos,
						entity);

				if (entity.world.getBlockState(blockpos.up()).getBlock() == Blocks.SNOW_LAYER) {
					soundtype = Blocks.SNOW_LAYER.getSoundType();
					entity.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
				} else if (!block.getDefaultState().getMaterial().isLiquid()) {
					entity.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
				}
			}
		}

		if (dx != origDx) {
			entity.motionX = dx;
		}
		if (dy != origDy) {
			if (!(entity.motionY > 0 && dy > 0)) {
				entity.motionY = 0;
			}
		}
		if (dz != origDz) {
			entity.motionZ = dz;
		}
	}

	/*
	 * This method generates an arrayList of Polygons that the player is colliding
	 * with
	 */
	public static ArrayList<Polygon> getCollidingPolygonsAndDoBlockCols(Entity entity, Vec3d velocity) {
		ArrayList<Polygon> collisions = new ArrayList<Polygon>();
		AxisAlignedBB entityBB = entity.getEntityBoundingBox().offset(velocity.x, velocity.y, velocity.z).expand(1, 1,
				1);

		WorldPhysObjectManager localPhysManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(entity.world);

		List<PhysicsWrapperEntity> ships = localPhysManager.getNearbyPhysObjects(entityBB);
		// If a player is riding a Ship, don't process any collision between that Ship
		// and the Player
		for (PhysicsWrapperEntity wrapper : ships) {
			try {
				if (!entity.isRidingSameEntity(wrapper)) {
					Polygon playerInLocal = new Polygon(entityBB, wrapper.wrapping.coordTransform.wToLTransform);
					AxisAlignedBB bb = playerInLocal.getEnclosedAABB();

					if ((bb.maxX - bb.minX) * (bb.maxZ - bb.minZ) > 9898989) {
						// This is too big, something went wrong here
						break;
					}
					List<AxisAlignedBB> collidingBBs = entity.world.getCollisionBoxes(entity, bb);

					// TODO: Fix the performance of this!
					if (entity.world.isRemote || entity instanceof EntityPlayer) {
						BigBastardMath.mergeAABBList(collidingBBs);
					}

					for (AxisAlignedBB inLocal : collidingBBs) {
						ShipPolygon poly = new ShipPolygon(inLocal, wrapper.wrapping.coordTransform.lToWTransform,
								wrapper.wrapping.coordTransform.normals, wrapper.wrapping);
						collisions.add(poly);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (PhysicsWrapperEntity wrapper : ships) {
			if (!entity.isRidingSameEntity(wrapper)) {
				double posX = entity.posX;
				double posY = entity.posY;
				double posZ = entity.posZ;

				Vector entityPos = new Vector(posX, posY, posZ);
				RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, entityPos);

				setEntityPositionAndUpdateBB(entity, entityPos.X, entityPos.Y, entityPos.Z);

				int entityChunkX = MathHelper.floor(entity.posX / 16.0D);
				int entityChunkZ = MathHelper.floor(entity.posZ / 16.0D);

				if (wrapper.wrapping.ownsChunk(entityChunkX, entityChunkZ)) {
					Chunk chunkIn = wrapper.wrapping.claimedChunks[entityChunkX
							- wrapper.wrapping.claimedChunks[0][0].x][entityChunkZ
									- wrapper.wrapping.claimedChunks[0][0].z];

					int chunkYIndex = MathHelper.floor(entity.posY / 16.0D);

					if (chunkYIndex < 0) {
						chunkYIndex = 0;
					}

					if (chunkYIndex >= chunkIn.entityLists.length) {
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

	public static void setEntityPositionAndUpdateBB(Entity entity, double x, double y, double z) {
		entity.posX = x;
		entity.posY = y;
		entity.posZ = z;
		float f = entity.width / 2.0F;
		float f1 = entity.height;
		entity.boundingBox = new AxisAlignedBB(x - f, y, z - f, x + f, y + f1, z + f);
	}

	private static boolean isDifSignificant(double dif1, double d2) {
		return !(Math.abs(dif1 - d2) < errorSignificance);
	}

	private static boolean motionInterfering(double orig, double modded) {
		return Math.signum(orig) != Math.signum(modded);
	}

	public static class IntermediateMovementVariableStorage {
		public final Vector dxyz;
		public final Vector origDxyz;
		public final Vector origPosXyz;
		public final boolean alreadyOnGround;
		public final double motionYBefore;
		public final float oldFallDistance;

		public IntermediateMovementVariableStorage(Vector dxyz, Vector origDxyz, Vector origPosXyz,
				boolean alreadyOnGround, double motionYBefore, float oldFallDistance) {
			this.dxyz = dxyz;
			this.origDxyz = origDxyz;
			this.origPosXyz = origPosXyz;
			this.alreadyOnGround = alreadyOnGround;
			this.motionYBefore = motionYBefore;
			this.oldFallDistance = oldFallDistance;
		}

	}

}