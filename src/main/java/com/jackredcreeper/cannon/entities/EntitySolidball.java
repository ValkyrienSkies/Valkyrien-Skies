package com.jackredcreeper.cannon.entities;

import com.jackredcreeper.cannon.world.NewExp;
import com.jackredcreeper.cannon.world.NewExp2;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySolidball extends EntitySnowball {

	
	int Pen; 
	
	
	public EntitySolidball(World worldIn, double x, double y, double z) {
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
        	float size = 0.5F;
        	float power = 0;
        	float blast = 0;
        	float damage = 100F;
        	
        	NewExp2 explosion = new NewExp2(this.getEntityWorld(),null,x,y,z,size,power,damage,blast,false,true);
            explosion.newBoom(this.getEntityWorld(),null,x,y,z,size,power,damage,blast,false,true);
            Pen++;
            if (Pen > 4) { this.setDead(); }
        }
    }

    
    
    
    

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    	
    	super.writeToNBT(compound);
    	compound.setInteger("Penetration",Pen);
		return compound;    	
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
    	
    	super.readFromNBT(compound);
    	Pen = compound.getInteger("Penetration");
    }
    
    
}