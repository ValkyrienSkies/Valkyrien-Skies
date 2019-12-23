package org.valkyrienskies.addon.opencomputers.block;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.valkyrienskies.addon.opencomputers.tileentity.TileEntityGPS;
import org.valkyrienskies.addon.opencomputers.util.BaseBlock;

public class BlockGPS extends BaseBlock implements ITileEntityProvider {

    public BlockGPS() {
        super("gps", Material.IRON, 0.0F, true);
        this.setHardness(5.0F);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityGPS();
    }
}
