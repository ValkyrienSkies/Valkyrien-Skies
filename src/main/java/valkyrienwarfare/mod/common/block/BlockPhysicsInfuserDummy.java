package valkyrienwarfare.mod.common.block;

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
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;

public class BlockPhysicsInfuserDummy extends BlockVWDirectional {

    public BlockPhysicsInfuserDummy(Material materialIn) {
        super(materialIn);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            BlockPos parentPos = getParentPos(state, pos);
            IBlockState belowState = worldIn.getBlockState(parentPos);
            belowState.getBlock()
                    .onBlockActivated(worldIn, parentPos, belowState, playerIn, hand, side, hitX, hitY,
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
        BlockPos parentPos = getParentPos(state, pos);
        super.breakBlock(worldIn, pos, state);
        if (worldIn.getBlockState(parentPos)
                .getBlock() == ValkyrienWarfareMod.INSTANCE.physicsInfuser) {
            worldIn.setBlockToAir(parentPos);
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        BlockPos parentPos = getParentPos(state, pos);
        return ValkyrienWarfareMod.INSTANCE.physicsInfuser.getPickBlock(world.getBlockState(parentPos), target, world, parentPos, player);
    }

    private BlockPos getParentPos(IBlockState state, BlockPos pos) {
        return pos.offset(state.getValue(FACING));
    }
}
