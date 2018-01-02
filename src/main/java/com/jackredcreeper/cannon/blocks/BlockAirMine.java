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

package com.jackredcreeper.cannon.blocks;

import com.jackredcreeper.cannon.CannonModReference;
import com.jackredcreeper.cannon.world.NewExp2;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockAirMine extends Block {

	public BlockAirMine() {
		super(Material.IRON);
		setHardness(0.5f);
		setResistance(0.5f);

		setUnlocalizedName(CannonModReference.ModBlocks.AIRMINE.getUnlocalizedName());
		setRegistryName(CannonModReference.ModBlocks.AIRMINE.getRegistryName());

		this.setCreativeTab(CreativeTabs.COMBAT);

	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World player, List<String> itemInformation, ITooltipFlag advanced)	{
		itemInformation.add(TextFormatting.BLUE + "Any Ship hitting this will have a bad time.");
	}

	public void BlockDestroyedByPlayer(BlockPos inWorldPos, World worldObj) {

		double x = inWorldPos.getX();
		double y = inWorldPos.getY();
		double z = inWorldPos.getZ();

		float size = 8F;
		float power = 0.01F;
		float blast = 0.01F;
		float damage = 100F;

		NewExp2 explosion1 = new NewExp2(worldObj, null, x, y, z, size, power, damage, blast, false, true);
		explosion1.newBoom(worldObj, null, x, y, z, size, power, damage, blast, false, true);

	}

	public void BlockDestroyedByExplosion(BlockPos inWorldPos, World worldObj) {

		double x = inWorldPos.getX();
		double y = inWorldPos.getY();
		double z = inWorldPos.getZ();

		float size = 8F;
		float power = 0.01F;
		float blast = 0.01F;
		float damage = 100F;

		NewExp2 explosion1 = new NewExp2(worldObj, null, x, y, z, size, power, damage, blast, false, true);
		explosion1.newBoom(worldObj, null, x, y, z, size, power, damage, blast, false, true);

	}
}
