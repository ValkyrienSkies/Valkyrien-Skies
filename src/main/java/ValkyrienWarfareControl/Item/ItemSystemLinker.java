package ValkyrienWarfareControl.Item;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
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

public class ItemSystemLinker extends Item {

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState state = worldIn.getBlockState(pos);
		Block block = state.getBlock();
		NBTTagCompound stackCompound = stack.getTagCompound();
		if (stackCompound == null) {
			stackCompound = new NBTTagCompound();
			stack.setTagCompound(stackCompound);
		}
		if (block instanceof BlockHovercraftController) {
			if (!worldIn.isRemote) {
				NBTUtils.writeBlockPosToNBT("controllerPos", pos, stackCompound);
				playerIn.addChatMessage(new TextComponentString("ControllerPos set <" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ() + ">"));
			} else {
				return EnumActionResult.SUCCESS;
			}
		}
		if (block instanceof BlockAntiGravEngine) {
			if (!worldIn.isRemote) {
				BlockPos controllerPos = NBTUtils.readBlockPosFromNBT("controllerPos", stackCompound);
				if (controllerPos.equals(BlockPos.ORIGIN)) {
					playerIn.addChatMessage(new TextComponentString("No selected Controller"));
				} else {
					PhysicsWrapperEntity controllerWrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, controllerPos);
					PhysicsWrapperEntity engineWrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);

					if (controllerWrapper != engineWrapper) {
						playerIn.addChatMessage(new TextComponentString("Controller and Engine are on seperate ships"));
						return EnumActionResult.SUCCESS;
					}
					TileEntity worldTile = worldIn.getTileEntity(pos);

					if (worldTile instanceof AntiGravEngineTileEntity) {
						AntiGravEngineTileEntity tileEntity = (AntiGravEngineTileEntity) worldTile;
						BlockPos gravControllerPos = tileEntity.controllerPos;
						if (gravControllerPos.equals(BlockPos.ORIGIN)) {
							playerIn.addChatMessage(new TextComponentString("Set Controller To " + controllerPos.toString()));
						} else {
							playerIn.addChatMessage(new TextComponentString("Replaced controller position from: " + gravControllerPos.toString() + " to: " + controllerPos.toString()));
						}
						tileEntity.setController(controllerPos);
					}
				}
			} else {
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

}
