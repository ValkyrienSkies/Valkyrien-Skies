package com.jackredcreeper.cannon.entities;

import com.jackredcreeper.cannon.world.NewExp;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityGrapeshot extends EntitySnowball {

	public EntityGrapeshot(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	@Override
	protected float getGravityVelocity() {
	return 0.01F;
	}

    protected void onImpact(RayTraceResult result)
    {

        if (!this.worldObj.isRemote)
        {
        	double x = this.posX + this.motionX/2;
        	double y = this.posY + this.motionY/2;
        	double z = this.posZ + this.motionZ/2;
        	float size = 1F;
        	float power = 0.4F;
        	float blast = 0.1F;
        	float damage = 30F;
        	
        	NewExp explosion = new NewExp(this.getEntityWorld(),null,x,y,z,size,power,damage,blast,false,true);
            explosion.newBoom(this.getEntityWorld(),null,x,y,z,size,power,damage,blast,false,true);
            this.setDead();
        }
    }

}