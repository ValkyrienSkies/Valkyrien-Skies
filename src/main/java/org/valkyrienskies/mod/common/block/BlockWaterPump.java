package org.valkyrienskies.mod.common.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.tileentity.TileEntityWaterPump;
import org.valkyrienskies.mod.common.util.BaseBlock;

import javax.annotation.Nullable;

public class BlockWaterPump extends BaseBlock implements ITileEntityProvider {

    public BlockWaterPump() {
        super("water_pump", Material.IRON, 0, true);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityWaterPump();
    }
}
