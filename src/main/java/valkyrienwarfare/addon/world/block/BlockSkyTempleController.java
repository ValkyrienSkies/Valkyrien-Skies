package valkyrienwarfare.addon.world.block;

import valkyrienwarfare.addon.world.tileentity.TileEntitySkyTempleController;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSkyTempleController extends Block implements ITileEntityProvider {

	public BlockSkyTempleController(Material materialIn) {
		super(materialIn);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntitySkyTempleController();
	}

}
