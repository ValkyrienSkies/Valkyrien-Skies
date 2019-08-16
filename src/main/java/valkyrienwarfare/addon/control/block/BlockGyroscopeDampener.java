package valkyrienwarfare.addon.control.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.tileentity.TileEntityGyroscopeDampener;
import valkyrienwarfare.mod.common.block.IBlockTorqueProvider;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.PhysicsCalculations;

public class BlockGyroscopeDampener extends Block implements ITileEntityProvider, IBlockTorqueProvider {

    public BlockGyroscopeDampener(Material materialIn) {
        super(materialIn);
    }

    @Override
    public Vector getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
        TileEntity thisTile = physicsCalculations.getParent().getWorldObj().getTileEntity(pos);
        if (thisTile instanceof TileEntityGyroscopeDampener) {
            TileEntityGyroscopeDampener tileGyroscope = (TileEntityGyroscopeDampener) thisTile;
            return tileGyroscope.getTorqueInGlobal(physicsCalculations, pos);
        }
        return null;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityGyroscopeDampener();
    }

    @Override
    public int getBlockSortingIndex() {
        // Since we're damping angular velocity, we want this to run at the very end, so
        // we give it a large sorting value to put it at the end.
        return 5;
    }

}
