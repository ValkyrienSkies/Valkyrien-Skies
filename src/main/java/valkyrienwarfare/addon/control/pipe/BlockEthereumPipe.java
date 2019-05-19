package valkyrienwarfare.addon.control.pipe;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockEthereumPipe extends Block {

    public BlockEthereumPipe(Material materialIn) {
        super(materialIn);
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityEthereumPipe();
    }

}
