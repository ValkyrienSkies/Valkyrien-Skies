package org.valkyrienskies.addon.opencomputers.block;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.valkyrienskies.addon.opencomputers.tileentity.GPSTileEntity;

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
