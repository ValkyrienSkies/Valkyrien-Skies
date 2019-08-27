package org.valkyrienskies.addon.control.block.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;

public class BlockEtherCompressorPart extends Block implements ITileEntityProvider, IBlockForceProvider {

    // The maximum thrust in newtons that each compressor block can provide.
    public static final double COMPRESSOR_PART_MAX_THRUST = 1300000;

    public BlockEtherCompressorPart(Material materialIn) {
        super(materialIn);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityEthereumCompressorPart(COMPRESSOR_PART_MAX_THRUST);
    }

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

    @Override
    public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state, double secondsToApply) {
        return false;
    }

    @Override
    public Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state, PhysicsObject physicsObject,
                                           double secondsToApply) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityEthereumCompressorPart) {
            TileEntityEthereumCompressorPart tileCompressorPart = (TileEntityEthereumCompressorPart) tileEntity;
            return tileCompressorPart.getForceOutputUnoriented(secondsToApply, physicsObject);
        }
        return null;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityEthereumCompressorPart) {
            ((TileEntityEthereumCompressorPart) tile).dissembleMultiblock();
        }
        super.breakBlock(worldIn, pos, state);
    }

}
