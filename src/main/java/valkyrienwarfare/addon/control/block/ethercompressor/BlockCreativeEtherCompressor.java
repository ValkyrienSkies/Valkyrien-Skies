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

package valkyrienwarfare.addon.control.block.ethercompressor;

import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.tileentity.TileEntityNormalEtherCompressor;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.api.block.ethercompressor.BlockEtherCompressorLore;

import javax.annotation.Nullable;
import java.util.List;

public class BlockCreativeEtherCompressor extends BlockEtherCompressorLore {

    public BlockCreativeEtherCompressor(Material materialIn, double enginePower) {
        super(materialIn, enginePower);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityNormalEtherCompressor(new Vector(0D, 1.0D, 0.0D), this.enginePower);
    }

    @Override
    public String getEnginePowerTooltip() {
        return "(Nearly) Infinite";
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> itemInformation, ITooltipFlag advanced) {
        for (String s : lore) {
            itemInformation.add(s);
        }

        itemInformation.add(TextFormatting.BOLD + "" + TextFormatting.RED + TextFormatting.ITALIC + "Warning! Glitchy!");
        itemInformation.add(TextFormatting.BOLD + "" + TextFormatting.RED + TextFormatting.ITALIC + "Can cause crashes, lag and/or ships dissapearing.");
    }
}
