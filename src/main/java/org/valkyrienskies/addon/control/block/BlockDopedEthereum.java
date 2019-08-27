/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.addon.control.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BlockDopedEthereum extends Block implements IBlockForceProvider {

    private static final double DOPED_ETHEREUM_FORCE = 200000;
    private static final String[] lore = new String[]{
            "" + TextFormatting.GRAY + TextFormatting.ITALIC + TextFormatting.BOLD +
                    "Force:", "  " + DOPED_ETHEREUM_FORCE + " Newtons"};

    public BlockDopedEthereum(Material materialIn) {
        super(materialIn);
    }

    /**
     * The force Vector this block gives within its local space (Not within World
     * space).
     */
    @Nullable
    @Override
    public Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state,
										   PhysicsObject physicsObject, double secondsToApply) {
        // TODO: Shouldn't this depend on the gravity vector?
        return new Vector(0, DOPED_ETHEREUM_FORCE * secondsToApply, 0);
    }

    /**
     * Blocks that shouldn't have their force rotated (Like Ether Compressors) must
     * return false.
     */
    @Override
    public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state, double secondsToApply) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> itemInformation,
                               ITooltipFlag advanced) {
        Collections.addAll(itemInformation, lore);
    }

}
