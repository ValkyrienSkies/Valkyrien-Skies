package valkyrienwarfare.mixin.entity;

import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.collision.EntityCollisionInjector;
import valkyrienwarfare.collision.EntityCollisionInjector.IntermediateMovementVariableStorage;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.ValkyrienWarfareMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.List;

@Mixin(value = Entity.class, priority = 1)
public abstract class MixinEntityIntrinsic {

	@Shadow
	public double posX;

	@Shadow
	public double posY;

	@Shadow
	public double posZ;

	@Shadow
	public World world;

	public Entity thisClassAsAnEntity = Entity.class.cast(this);

	@Overwrite
	public void move(MoverType type, double dx, double dy, double dz) {
//    	System.out.println("test");
		if (PhysicsWrapperEntity.class.isInstance(this)) {
			//Don't move at all
			return;
		}

		double movDistSq = (dx * dx) + (dy * dy) + (dz * dz);

		if (movDistSq > 10000) {
			//Assume this will take us to Ship coordinates
			double newX = this.posX + dx;
			double newY = this.posY + dy;
			double newZ = this.posZ + dz;
			BlockPos newPosInBlock = new BlockPos(newX, newY, newZ);
			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this.world, newPosInBlock);

			if (wrapper == null) {
				return;
			}

			Vector endPos = new Vector(newX, newY, newZ);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, endPos);
			dx = endPos.X - this.posX;
			dy = endPos.Y - this.posY;
			dz = endPos.Z - this.posZ;
		}

		IntermediateMovementVariableStorage alteredMovement = EntityCollisionInjector.alterEntityMovement(thisClassAsAnEntity, type, dx, dy, dz);

		if (alteredMovement == null) {
			this.moveOriginal(type, dx, dy, dz);
		} else {
			this.moveOriginal(type, alteredMovement.dxyz.X, alteredMovement.dxyz.Y, alteredMovement.dxyz.Z);
			EntityCollisionInjector.alterEntityMovementPost(thisClassAsAnEntity, alteredMovement);
		}
	}

	public void moveOriginal(MoverType type, double x, double y, double z) {
		if (thisClassAsAnEntity.noClip) {
			thisClassAsAnEntity.setEntityBoundingBox(thisClassAsAnEntity.getEntityBoundingBox().offset(x, y, z));
			thisClassAsAnEntity.resetPositionToBB();
		} else {
			if (type == MoverType.PISTON) {
				long i = this.world.getTotalWorldTime();

				if (i != thisClassAsAnEntity.field_191506_aJ) {
					Arrays.fill(thisClassAsAnEntity.field_191505_aI, 0.0D);
					thisClassAsAnEntity.field_191506_aJ = i;
				}

				if (x != 0.0D) {
					int j = EnumFacing.Axis.X.ordinal();
					double d0 = MathHelper.clamp(x + thisClassAsAnEntity.field_191505_aI[j], -0.51D, 0.51D);
					x = d0 - thisClassAsAnEntity.field_191505_aI[j];
					thisClassAsAnEntity.field_191505_aI[j] = d0;

					if (Math.abs(x) <= 9.999999747378752E-6D) {
						return;
					}
				} else if (y != 0.0D) {
					int l4 = EnumFacing.Axis.Y.ordinal();
					double d12 = MathHelper.clamp(y + thisClassAsAnEntity.field_191505_aI[l4], -0.51D, 0.51D);
					y = d12 - thisClassAsAnEntity.field_191505_aI[l4];
					thisClassAsAnEntity.field_191505_aI[l4] = d12;

					if (Math.abs(y) <= 9.999999747378752E-6D) {
						return;
					}
				} else {
					if (z == 0.0D) {
						return;
					}

					int i5 = EnumFacing.Axis.Z.ordinal();
					double d13 = MathHelper.clamp(z + thisClassAsAnEntity.field_191505_aI[i5], -0.51D, 0.51D);
					z = d13 - thisClassAsAnEntity.field_191505_aI[i5];
					thisClassAsAnEntity.field_191505_aI[i5] = d13;

					if (Math.abs(z) <= 9.999999747378752E-6D) {
						return;
					}
				}
			}

			this.world.profiler.startSection("move");
			double d10 = this.posX;
			double d11 = this.posY;
			double d1 = this.posZ;

			if (thisClassAsAnEntity.isInWeb) {
				thisClassAsAnEntity.isInWeb = false;
				x *= 0.25D;
				y *= 0.05000000074505806D;
				z *= 0.25D;
				thisClassAsAnEntity.motionX = 0.0D;
				thisClassAsAnEntity.motionY = 0.0D;
				thisClassAsAnEntity.motionZ = 0.0D;
			}

			double d2 = x;
			double d3 = y;
			double d4 = z;

			if ((type == MoverType.SELF || type == MoverType.PLAYER) && thisClassAsAnEntity.onGround && thisClassAsAnEntity.isSneaking() && thisClassAsAnEntity instanceof EntityPlayer) {
				for (double d5 = 0.05D; x != 0.0D && this.world.getCollisionBoxes(thisClassAsAnEntity, thisClassAsAnEntity.getEntityBoundingBox().offset(x, (double) (-thisClassAsAnEntity.stepHeight), 0.0D)).isEmpty(); d2 = x) {
					if (x < 0.05D && x >= -0.05D) {
						x = 0.0D;
					} else if (x > 0.0D) {
						x -= 0.05D;
					} else {
						x += 0.05D;
					}
				}

				for (; z != 0.0D && this.world.getCollisionBoxes(thisClassAsAnEntity, thisClassAsAnEntity.getEntityBoundingBox().offset(0.0D, (double) (-thisClassAsAnEntity.stepHeight), z)).isEmpty(); d4 = z) {
					if (z < 0.05D && z >= -0.05D) {
						z = 0.0D;
					} else if (z > 0.0D) {
						z -= 0.05D;
					} else {
						z += 0.05D;
					}
				}

				for (; x != 0.0D && z != 0.0D && this.world.getCollisionBoxes(thisClassAsAnEntity, thisClassAsAnEntity.getEntityBoundingBox().offset(x, (double) (-thisClassAsAnEntity.stepHeight), z)).isEmpty(); d4 = z) {
					if (x < 0.05D && x >= -0.05D) {
						x = 0.0D;
					} else if (x > 0.0D) {
						x -= 0.05D;
					} else {
						x += 0.05D;
					}

					d2 = x;

					if (z < 0.05D && z >= -0.05D) {
						z = 0.0D;
					} else if (z > 0.0D) {
						z -= 0.05D;
					} else {
						z += 0.05D;
					}
				}
			}

			List<AxisAlignedBB> list1 = this.world.getCollisionBoxes(thisClassAsAnEntity, thisClassAsAnEntity.getEntityBoundingBox().addCoord(x, y, z));
			AxisAlignedBB axisalignedbb = thisClassAsAnEntity.getEntityBoundingBox();

			if (y != 0.0D) {
				int k = 0;

				for (int l = list1.size(); k < l; ++k) {
					y = ((AxisAlignedBB) list1.get(k)).calculateYOffset(thisClassAsAnEntity.getEntityBoundingBox(), y);
				}

				thisClassAsAnEntity.setEntityBoundingBox(thisClassAsAnEntity.getEntityBoundingBox().offset(0.0D, y, 0.0D));
			}

			if (x != 0.0D) {
				int j5 = 0;

				for (int l5 = list1.size(); j5 < l5; ++j5) {
					x = ((AxisAlignedBB) list1.get(j5)).calculateXOffset(thisClassAsAnEntity.getEntityBoundingBox(), x);
				}

				if (x != 0.0D) {
					thisClassAsAnEntity.setEntityBoundingBox(thisClassAsAnEntity.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
				}
			}

			if (z != 0.0D) {
				int k5 = 0;

				for (int i6 = list1.size(); k5 < i6; ++k5) {
					z = ((AxisAlignedBB) list1.get(k5)).calculateZOffset(thisClassAsAnEntity.getEntityBoundingBox(), z);
				}

				if (z != 0.0D) {
					thisClassAsAnEntity.setEntityBoundingBox(thisClassAsAnEntity.getEntityBoundingBox().offset(0.0D, 0.0D, z));
				}
			}

			boolean flag = thisClassAsAnEntity.onGround || d3 != y && d3 < 0.0D;

			if (thisClassAsAnEntity.stepHeight > 0.0F && flag && (d2 != x || d4 != z)) {
				double d14 = x;
				double d6 = y;
				double d7 = z;
				AxisAlignedBB axisalignedbb1 = thisClassAsAnEntity.getEntityBoundingBox();
				thisClassAsAnEntity.setEntityBoundingBox(axisalignedbb);
				y = (double) thisClassAsAnEntity.stepHeight;
				List<AxisAlignedBB> list = this.world.getCollisionBoxes(thisClassAsAnEntity, thisClassAsAnEntity.getEntityBoundingBox().addCoord(d2, y, d4));
				AxisAlignedBB axisalignedbb2 = thisClassAsAnEntity.getEntityBoundingBox();
				AxisAlignedBB axisalignedbb3 = axisalignedbb2.addCoord(d2, 0.0D, d4);
				double d8 = y;
				int j1 = 0;

				for (int k1 = list.size(); j1 < k1; ++j1) {
					d8 = ((AxisAlignedBB) list.get(j1)).calculateYOffset(axisalignedbb3, d8);
				}

				axisalignedbb2 = axisalignedbb2.offset(0.0D, d8, 0.0D);
				double d18 = d2;
				int l1 = 0;

				for (int i2 = list.size(); l1 < i2; ++l1) {
					d18 = ((AxisAlignedBB) list.get(l1)).calculateXOffset(axisalignedbb2, d18);
				}

				axisalignedbb2 = axisalignedbb2.offset(d18, 0.0D, 0.0D);
				double d19 = d4;
				int j2 = 0;

				for (int k2 = list.size(); j2 < k2; ++j2) {
					d19 = ((AxisAlignedBB) list.get(j2)).calculateZOffset(axisalignedbb2, d19);
				}

				axisalignedbb2 = axisalignedbb2.offset(0.0D, 0.0D, d19);
				AxisAlignedBB axisalignedbb4 = thisClassAsAnEntity.getEntityBoundingBox();
				double d20 = y;
				int l2 = 0;

				for (int i3 = list.size(); l2 < i3; ++l2) {
					d20 = ((AxisAlignedBB) list.get(l2)).calculateYOffset(axisalignedbb4, d20);
				}

				axisalignedbb4 = axisalignedbb4.offset(0.0D, d20, 0.0D);
				double d21 = d2;
				int j3 = 0;

				for (int k3 = list.size(); j3 < k3; ++j3) {
					d21 = ((AxisAlignedBB) list.get(j3)).calculateXOffset(axisalignedbb4, d21);
				}

				axisalignedbb4 = axisalignedbb4.offset(d21, 0.0D, 0.0D);
				double d22 = d4;
				int l3 = 0;

				for (int i4 = list.size(); l3 < i4; ++l3) {
					d22 = ((AxisAlignedBB) list.get(l3)).calculateZOffset(axisalignedbb4, d22);
				}

				axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d22);
				double d23 = d18 * d18 + d19 * d19;
				double d9 = d21 * d21 + d22 * d22;

				if (d23 > d9) {
					x = d18;
					z = d19;
					y = -d8;
					thisClassAsAnEntity.setEntityBoundingBox(axisalignedbb2);
				} else {
					x = d21;
					z = d22;
					y = -d20;
					thisClassAsAnEntity.setEntityBoundingBox(axisalignedbb4);
				}

				int j4 = 0;

				for (int k4 = list.size(); j4 < k4; ++j4) {
					y = ((AxisAlignedBB) list.get(j4)).calculateYOffset(thisClassAsAnEntity.getEntityBoundingBox(), y);
				}

				thisClassAsAnEntity.setEntityBoundingBox(thisClassAsAnEntity.getEntityBoundingBox().offset(0.0D, y, 0.0D));

				if (d14 * d14 + d7 * d7 >= x * x + z * z) {
					x = d14;
					y = d6;
					z = d7;
					thisClassAsAnEntity.setEntityBoundingBox(axisalignedbb1);
				}
			}

			this.world.profiler.endSection();
			this.world.profiler.startSection("rest");
			thisClassAsAnEntity.resetPositionToBB();
			thisClassAsAnEntity.isCollidedHorizontally = d2 != x || d4 != z;
			thisClassAsAnEntity.isCollidedVertically = d3 != y;
			thisClassAsAnEntity.onGround = thisClassAsAnEntity.isCollidedVertically && d3 < 0.0D;
			thisClassAsAnEntity.isCollided = thisClassAsAnEntity.isCollidedHorizontally || thisClassAsAnEntity.isCollidedVertically;
			int j6 = MathHelper.floor(this.posX);
			int i1 = MathHelper.floor(this.posY - 0.20000000298023224D);
			int k6 = MathHelper.floor(this.posZ);
			BlockPos blockpos = new BlockPos(j6, i1, k6);
			IBlockState iblockstate = this.world.getBlockState(blockpos);

			if (iblockstate.getMaterial() == Material.AIR) {
				BlockPos blockpos1 = blockpos.down();
				IBlockState iblockstate1 = this.world.getBlockState(blockpos1);
				Block block1 = iblockstate1.getBlock();

				if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
					iblockstate = iblockstate1;
					blockpos = blockpos1;
				}
			}

			updateFallState(y, thisClassAsAnEntity.onGround, iblockstate, blockpos);

			if (d2 != x) {
				thisClassAsAnEntity.motionX = 0.0D;
			}

			if (d4 != z) {
				thisClassAsAnEntity.motionZ = 0.0D;
			}

			Block block = iblockstate.getBlock();

			if (d3 != y) {
				block.onLanded(this.world, thisClassAsAnEntity);
			}

			if (canTriggerWalking() && (!thisClassAsAnEntity.onGround || !thisClassAsAnEntity.isSneaking() || !(thisClassAsAnEntity instanceof EntityPlayer)) && !thisClassAsAnEntity.isRiding()) {
				double d15 = this.posX - d10;
				double d16 = this.posY - d11;
				double d17 = this.posZ - d1;

				if (block != Blocks.LADDER) {
					d16 = 0.0D;
				}

				if (block != null && thisClassAsAnEntity.onGround) {
					block.onEntityWalk(this.world, blockpos, thisClassAsAnEntity);
				}

				thisClassAsAnEntity.distanceWalkedModified = (float) ((double) thisClassAsAnEntity.distanceWalkedModified + (double) MathHelper.sqrt(d15 * d15 + d17 * d17) * 0.6D);
				thisClassAsAnEntity.distanceWalkedOnStepModified = (float) ((double) thisClassAsAnEntity.distanceWalkedOnStepModified + (double) MathHelper.sqrt(d15 * d15 + d16 * d16 + d17 * d17) * 0.6D);

				if (thisClassAsAnEntity.distanceWalkedOnStepModified > (float) thisClassAsAnEntity.nextStepDistance && iblockstate.getMaterial() != Material.AIR) {
					thisClassAsAnEntity.nextStepDistance = (int) thisClassAsAnEntity.distanceWalkedOnStepModified + 1;

					if (thisClassAsAnEntity.isInWater()) {
						Entity entity = thisClassAsAnEntity.isBeingRidden() && thisClassAsAnEntity.getControllingPassenger() != null ? thisClassAsAnEntity.getControllingPassenger() : thisClassAsAnEntity;
						float f = entity == thisClassAsAnEntity ? 0.35F : 0.4F;
						float f1 = MathHelper.sqrt(entity.motionX * entity.motionX * 0.20000000298023224D + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ * 0.20000000298023224D) * f;

						if (f1 > 1.0F) {
							f1 = 1.0F;
						}

						thisClassAsAnEntity.playSound(getSwimSound(), f1, 1.0F + (thisClassAsAnEntity.rand.nextFloat() - thisClassAsAnEntity.rand.nextFloat()) * 0.4F);
					} else {
						playStepSound(blockpos, block);
					}
				}
			}

			try {
				thisClassAsAnEntity.doBlockCollisions();
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("entity being checked for collision");
				thisClassAsAnEntity.addEntityCrashInfo(crashreportcategory);
				throw new ReportedException(crashreport);
			}

			boolean flag1 = thisClassAsAnEntity.isWet();

			if (this.world.isFlammableWithin(thisClassAsAnEntity.getEntityBoundingBox().contract(0.001D))) {
				dealFireDamage(1);

				if (!flag1) {
					++thisClassAsAnEntity.fire;

					if (thisClassAsAnEntity.fire == 0) {
						thisClassAsAnEntity.setFire(8);
					}
				}
			} else if (thisClassAsAnEntity.fire <= 0) {
				thisClassAsAnEntity.fire = -getFireImmuneTicks();
			}

			if (flag1 && thisClassAsAnEntity.isBurning()) {
				thisClassAsAnEntity.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (thisClassAsAnEntity.rand.nextFloat() - thisClassAsAnEntity.rand.nextFloat()) * 0.4F);
				thisClassAsAnEntity.fire = -getFireImmuneTicks();
			}

			this.world.profiler.endSection();
		}
	}

	@Shadow
	protected abstract void playStepSound(BlockPos pos, Block blockIn);

	@Shadow
	protected abstract SoundEvent getSwimSound();

	@Shadow
	protected abstract boolean canTriggerWalking();

	@Shadow
	protected abstract void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos);

	@Shadow
	protected abstract void dealFireDamage(int amount);

	@Shadow
	protected abstract int getFireImmuneTicks();

//    @Shadow
//    public abstract void move(MoverType type, double x, double y, double z);
}
