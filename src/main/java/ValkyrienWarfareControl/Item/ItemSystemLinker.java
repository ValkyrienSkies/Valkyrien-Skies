package ValkyrienWarfareControl.Item;

import ValkyrienWarfareBase.API.Block.EtherCompressor.BlockEtherCompressor;
import ValkyrienWarfareBase.API.Block.EtherCompressor.TileEntityEtherCompressor;
import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.Block.BlockHovercraftController;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemSystemLinker extends Item {

	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.BLUE + "Right click on the Hover Controller, then right click on any Ether Compressors you wish to automate control.");
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState state = worldIn.getBlockState(pos);
		Block block = state.getBlock();
		ItemStack stack = playerIn.getHeldItem(hand);
		NBTTagCompound stackCompound = stack.getTagCompound();
		if (stackCompound == null) {
			stackCompound = new NBTTagCompound();
			stack.setTagCompound(stackCompound);
		}
		if (block instanceof BlockHovercraftController) {
			if (!worldIn.isRemote) {
				NBTUtils.writeBlockPosToNBT("controllerPos", pos, stackCompound);
				playerIn.sendMessage(new TextComponentString("ControllerPos set <" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ() + ">"));
			} else {
				return EnumActionResult.SUCCESS;
			}
		}

		if (block instanceof BlockEtherCompressor) {
			if (!worldIn.isRemote) {
				BlockPos controllerPos = NBTUtils.readBlockPosFromNBT("controllerPos", stackCompound);
				if (controllerPos.equals(BlockPos.ORIGIN)) {
					playerIn.sendMessage(new TextComponentString("No selected Controller"));
				} else {
					PhysicsWrapperEntity controllerWrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, controllerPos);
					PhysicsWrapperEntity engineWrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);

					if (controllerWrapper != engineWrapper) {
						playerIn.sendMessage(new TextComponentString("Controller and Engine are on seperate ships"));
						return EnumActionResult.SUCCESS;
					}
					TileEntity worldTile = worldIn.getTileEntity(pos);

					if (worldTile instanceof TileEntityEtherCompressor) {
						TileEntityEtherCompressor tileEntity = (TileEntityEtherCompressor) worldTile;

						BlockPos gravControllerPos = tileEntity.getControllerPos();
						if (gravControllerPos == null || gravControllerPos.equals(BlockPos.ORIGIN)) {
							playerIn.sendMessage(new TextComponentString("Set Controller To " + controllerPos.toString()));
						} else {
							playerIn.sendMessage(new TextComponentString("Replaced controller position from: " + gravControllerPos.toString() + " to: " + controllerPos.toString()));
						}
						tileEntity.setControllerPos(controllerPos);
					}
				}
			} else {
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

}
