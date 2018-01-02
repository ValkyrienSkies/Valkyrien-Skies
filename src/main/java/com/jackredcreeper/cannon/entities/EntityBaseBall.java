/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.jackredcreeper.cannon.entities;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityBaseBall extends Entity {
	
	
	public EntityLivingBase shootingEntity;
	public double accelerationX;
	public double accelerationY;
	public double accelerationZ;
	private int xTile = -1;
	private int yTile = -1;
	private int zTile = -1;
	private Block inTile;
	private boolean inGround;
	private int ticksAlive;
	private int ticksInAir;
	
	public EntityBaseBall(World worldIn) {
		super(worldIn);
		this.setSize(1.0F, 1.0F);
	}
	
	public EntityBaseBall(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ) {
		super(worldIn);
		this.setSize(1.0F, 1.0F);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
		double d0 = (double) MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
		this.accelerationX = accelX / d0 * 0.1D;
		this.accelerationY = accelY / d0 * 0.1D;
		this.accelerationZ = accelZ / d0 * 0.1D;
	}
	
	public EntityBaseBall(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ) {
		super(worldIn);
		this.shootingEntity = shooter;
		this.setSize(1.0F, 1.0F);
		this.setLocationAndAngles(shooter.posX, shooter.posY, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		accelX = accelX + this.rand.nextGaussian() * 0.4D;
		accelY = accelY + this.rand.nextGaussian() * 0.4D;
		accelZ = accelZ + this.rand.nextGaussian() * 0.4D;
		double d0 = (double) MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
		this.accelerationX = accelX / d0 * 0.1D;
		this.accelerationY = accelY / d0 * 0.1D;
		this.accelerationZ = accelZ / d0 * 0.1D;
	}
	
	public static void registerFixesFireball(DataFixer fixer, String name) {
	}
	
	protected void entityInit() {
	}
	
	/**
	 * Checks if the entity is in range to render.
	 */
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
		
		if (Double.isNaN(d0)) {
			d0 = 4.0D;
		}
		
		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}
	
	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {
		if (this.world.isRemote || (this.shootingEntity == null || !this.shootingEntity.isDead) && this.world.isBlockLoaded(new BlockPos(this))) {
			super.onUpdate();
			
			if (this.isFireballFiery()) {
				this.setFire(1);
			}
			
			if (this.inGround) {
				if (this.world.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile) {
					++this.ticksAlive;
					
					if (this.ticksAlive == 600) {
						this.setDead();
					}
					
					return;
				}
				
				this.inGround = false;
				this.motionX *= (double) (this.rand.nextFloat() * 0.2F);
				this.motionY *= (double) (this.rand.nextFloat() * 0.2F);
				this.motionZ *= (double) (this.rand.nextFloat() * 0.2F);
				this.ticksAlive = 0;
				this.ticksInAir = 0;
			} else {
				++this.ticksInAir;
			}
			
			RayTraceResult raytraceresult = ProjectileHelper.forwardsRaycast(this, true, this.ticksInAir >= 25, this.shootingEntity);
			
			if (raytraceresult != null) {
				this.onImpact(raytraceresult);
			}
			
			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			ProjectileHelper.rotateTowardsMovement(this, 0.2F);
			float f = this.getMotionFactor();
			
			if (this.isInWater()) {
				for (int i = 0; i < 4; ++i) {
					float f1 = 0.25F;
					this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
				}
				
				f = 0.8F;
			}
			
			this.motionX += this.accelerationX;
			this.motionY += this.accelerationY;
			this.motionZ += this.accelerationZ;
			this.motionX *= (double) f;
			this.motionY *= (double) f;
			this.motionZ *= (double) f;
			this.world.spawnParticle(this.getParticleType(), this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
			this.setPosition(this.posX, this.posY, this.posZ);
		} else {
			this.setDead();
		}
	}
	
	protected boolean isFireballFiery() {
		return true;
	}
	
	protected EnumParticleTypes getParticleType() {
		return EnumParticleTypes.SMOKE_NORMAL;
	}
	
	/**
	 * Return the motion factor for this projectile. The factor is multiplied by the original motion.
	 */
	protected float getMotionFactor() {
		return 0.95F;
	}
	
	/**
	 * Called when this EntityFireball hits a block or entity.
	 */
	protected abstract void onImpact(RayTraceResult result);
	
	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("xTile", this.xTile);
		compound.setInteger("yTile", this.yTile);
		compound.setInteger("zTile", this.zTile);
		ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(this.inTile);
		compound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
		compound.setByte("inGround", (byte) (this.inGround ? 1 : 0));
		compound.setTag("direction", this.newDoubleNBTList(this.motionX, this.motionY, this.motionZ));
		compound.setTag("power", this.newDoubleNBTList(this.accelerationX, this.accelerationY, this.accelerationZ));
		compound.setInteger("life", this.ticksAlive);
	}
	
	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {
		this.xTile = compound.getInteger("xTile");
		this.yTile = compound.getInteger("yTile");
		this.zTile = compound.getInteger("zTile");
		
		if (compound.hasKey("inTile", 8)) {
			this.inTile = Block.getBlockFromName(compound.getString("inTile"));
		} else {
			this.inTile = Block.getBlockById(compound.getByte("inTile") & 255);
		}
		
		this.inGround = compound.getByte("inGround") == 1;
		
		if (compound.hasKey("power", 9)) {
			NBTTagList nbttaglist = compound.getTagList("power", 6);
			
			if (nbttaglist.tagCount() == 3) {
				this.accelerationX = nbttaglist.getDoubleAt(0);
				this.accelerationY = nbttaglist.getDoubleAt(1);
				this.accelerationZ = nbttaglist.getDoubleAt(2);
			}
		}
		
		this.ticksAlive = compound.getInteger("life");
		
		if (compound.hasKey("direction", 9) && compound.getTagList("direction", 6).tagCount() == 3) {
			NBTTagList nbttaglist1 = compound.getTagList("direction", 6);
			this.motionX = nbttaglist1.getDoubleAt(0);
			this.motionY = nbttaglist1.getDoubleAt(1);
			this.motionZ = nbttaglist1.getDoubleAt(2);
		} else {
			this.setDead();
		}
	}
	
	/**
	 * Returns true if other Entities should be prevented from moving through this entity.
	 */
	public boolean canBeCollidedWith() {
		return true;
	}
	
	public float getCollisionBorderSize() {
		return 1.0F;
	}
	
	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		} else {
			this.markVelocityChanged();
			
			if (source.getImmediateSource() != null) {
				Vec3d vec3d = source.getImmediateSource().getLookVec();
				
				if (vec3d != null) {
					this.motionX = vec3d.x;
					this.motionY = vec3d.y;
					this.motionZ = vec3d.z;
					this.accelerationX = this.motionX * 0.1D;
					this.accelerationY = this.motionY * 0.1D;
					this.accelerationZ = this.motionZ * 0.1D;
				}
				
				if (source.getImmediateSource() instanceof EntityLivingBase) {
					this.shootingEntity = (EntityLivingBase) source.getImmediateSource();
				}
				
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Gets how bright this entity is.
	 */
	public float getBrightness(float partialTicks) {
		return 1.0F;
	}
	
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float partialTicks) {
		return 15728880;
	}
}