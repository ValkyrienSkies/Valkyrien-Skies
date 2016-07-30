package ValkyrienWarfareCombat.Entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class EntityCannonBasic extends EntityMountingWeaponBase{

	public EntityCannonBasic(World worldIn) {
		super(worldIn);
	}

	@Override
	public void onRiderInteract(EntityPlayer player, ItemStack stack, EnumHand hand) {
		if(!player.worldObj.isRemote){
			
		}
	}
	
}
