package com.jackredcreeper.cannon.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityExplosiveball extends EntitySnowball {

	public EntityExplosiveball(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}
 
	int Exp = 0;
	
	@Override
	protected float getGravityVelocity() {
	return 0.03F;
	}
		
	@Override
    protected void onImpact(RayTraceResult result)
    {
        if (!this.worldObj.isRemote)
        {

            this.worldObj.newExplosion((Entity)null, this.posX + this.motionX/2F, this.posY + this.motionY/2F, this.posZ + this.motionZ/2F, 2, false, true);
            Exp++;
            if (Exp >= 3) {
            this.setDead();}
        }
    }
	
	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    	
    	super.writeToNBT(compound);
    	compound.setInteger("Exp", Exp);
		return compound;    	
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
    	
    	super.readFromNBT(compound);
    	Exp = compound.getInteger("Exp");
    }
}