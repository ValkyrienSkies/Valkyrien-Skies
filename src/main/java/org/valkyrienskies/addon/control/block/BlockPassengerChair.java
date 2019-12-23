package org.valkyrienskies.addon.control.block;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.valkyrienskies.addon.control.tileentity.TileEntityPassengerChair;
import org.valkyrienskies.addon.control.util.BaseBlock;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockPassengerChair extends BaseBlock {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockPassengerChair() {
        super("passenger_chair", Material.WOOD, 0.0F, true);
        this.setHardness(4.0F);
    }

    public static double getChairYaw(IBlockState state, BlockPos pos) {
        EnumFacing enumFace = state.getValue(BlockPassengerChair.FACING);
        return -enumFace.getHorizontalAngle() - 90;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityPassengerChair();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
        EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            Vec3d chairPos = getPlayerMountOffset(state, pos);

            TileEntity chairTile = worldIn.getTileEntity(pos);
            if (chairTile instanceof TileEntityPassengerChair) {
                // Try mounting the player onto the chair if possible.
                ((TileEntityPassengerChair) chairTile).tryToMountPlayerToChair(playerIn, chairPos);
            } else {
                new IllegalStateException(
                    "world.getTileEntity() returned a tile that wasn't a chair at pos " + pos)
                    .printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity passengerChair = worldIn.getTileEntity(pos);
        if (passengerChair instanceof TileEntityPassengerChair && !passengerChair.isInvalid()) {
            ((TileEntityPassengerChair) passengerChair).onBlockBroken(state);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation, ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.BLUE + I18n
            .format("tooltip.vs_control.passenger_chair"));
    }

    private Vec3d getPlayerMountOffset(IBlockState state, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        switch (facing) {
            case NORTH:
                return new Vec3d(pos.getX() + .5, pos.getY(), pos.getZ() + .6);
            case SOUTH:
                return new Vec3d(pos.getX() + .5, pos.getY(), pos.getZ() + .4);
            case WEST:
                return new Vec3d(pos.getX() + .6, pos.getY(), pos.getZ() + .5);
            case EAST:
                return new Vec3d(pos.getX() + .4, pos.getY(), pos.getZ() + .5);
            default:
                return new Vec3d(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        }
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
        float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING,
            placer.isSneaking() ? placer.getHorizontalFacing().getOpposite()
                : placer.getHorizontalFacing());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
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

}