package ValkyrienWarfareCombat.Entity;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareCombat.ValkyrienWarfareCombatMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityCannonBasic extends EntityMountingWeaponBase{

	public EntityCannonBasic(World worldIn) {
		super(worldIn);
	}

	@Override
	public void onRiderInteract(EntityPlayer player, ItemStack stack, EnumHand hand) {
		if(!player.worldObj.isRemote){
			Vec3d velocityNormal = getVectorForRotation(rotationPitch, rotationYaw);
			Vector velocityVector = new Vector(velocityNormal);
			velocityVector.multiply(2D);
			EntityCannonBall projectile = new EntityCannonBall(worldObj, velocityVector,this);
			projectile.posY+=.5;
			worldObj.spawnEntityInWorld(projectile);
//			worldObj.playSound(null, posX, posY, posZ, new SoundEvent(), SoundCategory.AMBIENT, volume, pitch, true);
//			System.out.println("test");
		}
	}

	@Override
	public void doItemDrops() {
      ItemStack itemstack = new ItemStack(ValkyrienWarfareCombatMod.instance.basicCannonSpawner, 1);

      if (this.getName() != null)
      {
          itemstack.setStackDisplayName(this.getName());
      }

      this.entityDropItem(itemstack, 0.0F);
	}
	
}
