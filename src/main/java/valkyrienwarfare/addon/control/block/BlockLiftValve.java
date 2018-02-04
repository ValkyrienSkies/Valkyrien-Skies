package valkyrienwarfare.addon.control.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockLiftValve extends Block implements ITileEntityProvider {

    public BlockLiftValve(Material materialIn) {
        super(materialIn);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

}
