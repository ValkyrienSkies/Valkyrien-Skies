package com.jackredcreeper.cannon.entities;

import com.jackredcreeper.cannon.world.NewExp;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityCannonball extends EntitySnowball {

	public EntityCannonball(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	@Override
	protected float getGravityVelocity() {
		return 0.01F;
	}

	protected void onImpact(RayTraceResult result) {

		if (!this.world.isRemote) {
			double x = this.posX + this.motionX;
			double y = this.posY + this.motionY;
			double z = this.posZ + this.motionZ;
			float size = 3F;
			float power = 0.05F;
			float blast = 0.01F;
			float damage = 30F;

			NewExp explosion = new NewExp(this.getEntityWorld(), null, x, y, z, size, power, damage, blast, false, true);
			explosion.newBoom(this.getEntityWorld(), null, x, y, z, size, power, damage, blast, false, true);
			this.setDead();
		}
	}

}