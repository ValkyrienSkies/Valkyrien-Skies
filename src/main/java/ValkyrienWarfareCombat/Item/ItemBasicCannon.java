package ValkyrienWarfareCombat.Item;

import java.util.List;

import ValkyrienWarfareCombat.Entity.EntityCannonBasic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemBasicCannon extends Item {

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.BLUE + "A basic mountable cannon that can be placed in world, or on Ships. Requires cannon balls and power pouches to fire.");
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			EnumFacing playerFacing = playerIn.getHorizontalFacing();
			EntityCannonBasic cannon = new EntityCannonBasic(worldIn);
			cannon.setFacing(playerFacing);
			cannon.setPosition(pos.getX() + .5D, pos.getY() + 1D, pos.getZ() + .5D);
			worldIn.spawnEntityInWorld(cannon);
			stack.stackSize--;
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}
}
