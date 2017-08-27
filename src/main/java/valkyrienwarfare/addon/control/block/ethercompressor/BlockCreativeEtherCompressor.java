package valkyrienwarfare.addon.control.block.ethercompressor;

import valkyrienwarfare.addon.control.tileentity.TileEntityNormalEtherCompressor;
import valkyrienwarfare.api.block.ethercompressor.BlockEtherCompressorLore;
import valkyrienwarfare.api.Vector;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

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
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		for (String s : lore) {
			itemInformation.add(s);
		}

		itemInformation.add(TextFormatting.BOLD + "" + TextFormatting.RED + TextFormatting.ITALIC + "Warning! Glitchy!");
		itemInformation.add(TextFormatting.BOLD + "" + TextFormatting.RED + TextFormatting.ITALIC + "Can cause crashes, lag and/or ships dissapearing.");
	}
}
