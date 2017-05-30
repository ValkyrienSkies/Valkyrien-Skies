package ValkyrienWarfareControl.Item;

import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.Block.BlockThrustRelay;
import ValkyrienWarfareControl.Capability.ICapabilityLastRelay;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemRelayWire extends Item {

	public static double range = 8D;

	public ItemRelayWire(){
		this.setMaxStackSize(1);
		this.setMaxDamage(80);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState clickedState = worldIn.getBlockState(pos);
		Block block = clickedState.getBlock();

		if(block instanceof BlockThrustRelay && !worldIn.isRemote){

			ICapabilityLastRelay inst = stack.getCapability(ValkyrienWarfareControlMod.lastRelayCapability, null);

			if(inst != null){
				System.out.println("Success");
			}

			stack.damageItem(1, playerIn);
		}

		return EnumActionResult.FAIL;
	}

}
