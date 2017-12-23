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

import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.ValkyrienWarfareMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemShipStealer extends Item {

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.RED + TextFormatting.ITALIC + "Unfinished until v_0.91_alpha");
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		BlockPos looking = playerIn.rayTrace(playerIn.isCreative() ? 5.0 : 4.5, 1).getBlockPos();
		PhysicsWrapperEntity entity = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(playerIn.getEntityWorld(), looking);

		if (entity != null) {
			String oldOwner = entity.wrapping.creator;
			if (oldOwner == playerIn.entityUniqueID.toString()) {
				playerIn.sendMessage(new TextComponentString("You can't steal your own airship!"));
				return EnumActionResult.SUCCESS;
			}
			switch (entity.wrapping.changeOwner(playerIn)) {
				case ERROR_NEWOWNER_NOT_ENOUGH:
					playerIn.sendMessage(new TextComponentString("You already own the maximum amount of airships!"));
					break;
				case ERROR_IMPOSSIBLE_STATUS:
					playerIn.sendMessage(new TextComponentString("Error! Please report to mod devs."));
					break;
				case SUCCESS:
					playerIn.sendMessage(new TextComponentString("You've stolen an airship!"));
					break;
				case ALREADY_CLAIMED:
					playerIn.sendMessage(new TextComponentString("You already own that airship!"));
					break;
			}
			return EnumActionResult.SUCCESS;
		}

		playerIn.sendMessage(new TextComponentString("The block needs to be part of an airship!"));
		return EnumActionResult.SUCCESS;
	}
}
