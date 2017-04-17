package ValkyrienWarfareBase.API.Block.EtherCompressor;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public abstract class BlockEtherCompressorLore extends BlockEtherCompressor {
	protected String[] lore;

	public BlockEtherCompressorLore(Material materialIn, double enginePower) {
		super(materialIn, enginePower);
		lore = new String[] { "" + TextFormatting.GRAY + TextFormatting.ITALIC + TextFormatting.BOLD + "Power:", "  " + this.getEnginePowerTooltip() };
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		for (String s : lore) {
			itemInformation.add(s);
		}
	}

	public abstract String getEnginePowerTooltip();
}
