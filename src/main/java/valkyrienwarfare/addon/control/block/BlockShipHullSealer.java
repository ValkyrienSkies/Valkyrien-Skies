package valkyrienwarfare.addon.control.block;

import valkyrienwarfare.addon.control.tileentity.TileEntityHullSealer;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockShipHullSealer extends Block implements ITileEntityProvider {

	public BlockShipHullSealer(Material materialIn) {
		super(materialIn);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityHullSealer();
	}

}
