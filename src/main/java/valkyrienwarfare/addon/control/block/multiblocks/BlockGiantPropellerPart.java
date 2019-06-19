package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.deprecated_api.IBlockForceProvider;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import javax.annotation.Nullable;

public class BlockGiantPropellerPart extends Block implements ITileEntityProvider, IBlockForceProvider {

    public BlockGiantPropellerPart(Material materialIn) {
        super(materialIn);
    }

    @Nullable
    @Override
    public Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
        if (true) {
//            return new Vector(0, 1000 * secondsToApply, 0);
        }
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityGiantPropellerPart) {
            TileEntityGiantPropellerPart tileCompressorPart = (TileEntityGiantPropellerPart) tileEntity;
            return tileCompressorPart.getForceOutputUnoriented(secondsToApply, ((PhysicsWrapperEntity) shipEntity).getPhysicsObject());
        }
        return null;
    }

    @Override
    public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state, double secondsToApply) {
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityGiantPropellerPart) {
            ((TileEntityGiantPropellerPart) tile).dissembleMultiblock();
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityGiantPropellerPart();
    }

    // Lighting crap
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
}
