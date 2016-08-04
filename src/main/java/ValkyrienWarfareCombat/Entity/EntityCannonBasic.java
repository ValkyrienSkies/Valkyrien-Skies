package ValkyrienWarfareCombat.Entity;

import ValkyrienWarfareBase.API.Vector;
import net.minecraft.entity.player.EntityPlayer;
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
//			Vec3d velocityNormal = getVectorForRotation(rotationPitch, rotationYaw);
//			Vector velocityVector = new Vector(velocityNormal);
//			velocityVector.multiply(.1D);
//			EntityCannonBall projectile = new EntityCannonBall(worldObj, velocityVector,this);
//			worldObj.spawnEntityInWorld(projectile);
//			System.out.println("test");
		}
	}
	
}
