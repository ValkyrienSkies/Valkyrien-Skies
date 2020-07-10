package org.valkyrienskies.addon.control.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.util.BaseBlock;

public class BlockDummyTelegraph extends BaseBlock {

    public BlockDummyTelegraph() {
        super("dummy_telegraph", Material.WOOD, 0.0F, false);
        this.setHardness(5.0F);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
        EntityPlayer playerIn,
        EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            IBlockState belowState = worldIn.getBlockState(pos.down());
            belowState.getBlock()
                .onBlockActivated(worldIn, pos.down(), belowState, playerIn, hand, side, hitX, hitY,
                    hitZ);
        }
        return true;
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
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (worldIn.getBlockState(pos.down())
            .getBlock() == ValkyrienSkiesControl.INSTANCE.vsControlBlocks.speedTelegraph) {
            worldIn.setBlockToAir(pos.down());
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world,
        BlockPos pos, EntityPlayer player) {
        return ValkyrienSkiesControl.INSTANCE.vsControlBlocks.speedTelegraph
            .getPickBlock(world.getBlockState(pos.down()), target, world, pos.down(), player);
    }
}
