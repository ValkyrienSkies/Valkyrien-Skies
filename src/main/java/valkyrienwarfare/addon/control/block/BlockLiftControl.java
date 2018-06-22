package valkyrienwarfare.addon.control.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.tileentity.TileEntityLiftControl;

public class BlockLiftControl extends BlockPilotableBasic {

	public BlockLiftControl(Material materialIn) {
		super(materialIn);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityLiftControl();
	}

}
