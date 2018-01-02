/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.addon.control.item;

import net.minecraft.client.util.ITooltipFlag;
import valkyrienwarfare.api.block.ethercompressor.BlockEtherCompressor;
import valkyrienwarfare.api.block.ethercompressor.TileEntityEtherCompressor;
import valkyrienwarfare.NBTUtils;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.block.BlockHovercraftController;
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

import javax.annotation.Nullable;
import java.util.List;

public class ItemSystemLinker extends Item {

	@Override
	public void addInformation(ItemStack stack, @Nullable World player, List<String> itemInformation, ITooltipFlag advanced)	{
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
						playerIn.sendMessage(new TextComponentString("Controller and engine are on seperate ships"));
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
