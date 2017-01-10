package ValkyrienWarfareControl.Item;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.ValkyrienWarfareHooks;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Block.BlockAntiGravEngine;
import ValkyrienWarfareControl.Block.BlockHovercraftController;
import ValkyrienWarfareControl.TileEntity.AntiGravEngineTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemShipStealer extends Item {
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (ValkyrienWarfareHooks.isBlockPartOfShip(worldIn, pos))	{
			PhysicsWrapperEntity entity = ValkyrienWarfareHooks.getShipEntityManagingPos(worldIn, pos);
			EntityPlayer oldOwner = entity.wrapping.creator;
			if (oldOwner == playerIn)	{
				playerIn.addChatMessage(new TextComponentString("You can't steal your own airship!"));
				return EnumActionResult.SUCCESS;
			}
			switch (entity.wrapping.changeOwner(playerIn))	{
			case ERROR_NEWOWNER_NOT_ENOUGH:
				playerIn.addChatMessage(new TextComponentString("You already own the maximum amount of airships!"));
				break;
			case ERROR_IMPOSSIBLE_STATUS:
				playerIn.addChatMessage(new TextComponentString("Error! Please report to mod devs."));
				break;
			case SUCCESS:
				playerIn.addChatMessage(new TextComponentString("You've stolen an airship from " + oldOwner.getName() + "!"));
				oldOwner.addChatMessage(new TextComponentString(playerIn.getName() + " stole an airship from you!"));
				break;
			}
			return EnumActionResult.SUCCESS;
		}
		
		playerIn.addChatMessage(new TextComponentString("The block needs to be part of an airship!"));
		return EnumActionResult.SUCCESS;
	}
}
