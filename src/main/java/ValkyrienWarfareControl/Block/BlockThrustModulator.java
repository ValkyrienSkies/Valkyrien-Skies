package ValkyrienWarfareControl.Block;

import java.util.List;

import ValkyrienWarfareControl.TileEntity.ThrustModulatorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockThrustModulator extends Block implements ITileEntityProvider {

	public BlockThrustModulator(Material materialIn) {
		super(materialIn);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.RED + TextFormatting.ITALIC + "Unfinished until v_0.91_alpha");
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new ThrustModulatorTileEntity();
	}

}
