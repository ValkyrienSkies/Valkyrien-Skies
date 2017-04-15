package com.jackredcreeper.cannon.entities;

import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;
import javax.management.NotificationBroadcaster;

import com.jackredcreeper.cannon.world.NewExp;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityCannonball extends EntitySnowball {

	public EntityCannonball(World worldIn, double x, double y, double z) {
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
        	double x = this.posX + this.motionX;
        	double y = this.posY + this.motionY;
        	double z = this.posZ + this.motionZ;
        	float size = 3F;
        	float power = 0.05F;
        	float blast = 0.01F;
        	float damage = 30F;
        	
        	NewExp explosion = new NewExp(this.getEntityWorld(),null,x,y,z,size,power,damage,blast,false,true);
            explosion.newBoom(this.getEntityWorld(),null,x,y,z,size,power,damage,blast,false,true);
            this.setDead();
        }
    }

}