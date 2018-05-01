package valkyrienwarfare.addon.opencomputers.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.apache.commons.lang3.NotImplementedException;
import valkyrienwarfare.addon.opencomputers.tileentity.GPSTileEntity;

import javax.annotation.Nullable;

public class GPSBlock extends Block implements ITileEntityProvider {
    public GPSBlock() {
        super(Material.ROCK);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new GPSTileEntity();
    }
}
