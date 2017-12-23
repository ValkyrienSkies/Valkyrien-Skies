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

package valkyrienwarfare.addon.control.block;

import valkyrienwarfare.addon.control.item.ItemSystemLinker;
import valkyrienwarfare.addon.control.tileentity.TileEntityHoverController;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.gui.ControlGUIEnum;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class BlockHovercraftController extends Block implements ITileEntityProvider {

	public BlockHovercraftController(Material materialIn) {
		super(materialIn);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);
		ItemStack heldItem = playerIn.getHeldItem(hand);
		if (heldItem != null && heldItem.getItem() instanceof ItemSystemLinker) {
			return false;
		}
		if (wrapper != null) {
			if (!worldIn.isRemote) {
				if (playerIn instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) playerIn;
					int realWindowId = player.currentWindowId;

					// TODO: Fix this, I have to reset the window IDs because there is no container on client side, resulting in the client never changing its window id

					player.currentWindowId = player.inventoryContainer.windowId - 1;
					player.openGui(ValkyrienWarfareMod.INSTANCE, ControlGUIEnum.HoverCraftController.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());

					player.currentWindowId = realWindowId;
					// player.openContainer = playerIn.inventoryContainer;
				}

			}
			return true;
		}
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.BLUE + "Used to automatically control the thrust output of Ether Compressors, allowing for stable flight.");
		itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.GRAY + TextFormatting.ITALIC + "Auto stabalization control can be disabled with a redstone signal.");
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityHoverController();
	}

}