package org.valkyrienskies.addon.control.block;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.tileentity.TileEntityNetworkRelay;
import org.valkyrienskies.addon.control.util.BaseBlock;
import org.valkyrienskies.mod.common.config.VSConfig;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockNetworkRelay extends BaseBlock implements ITileEntityProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    private static final AxisAlignedBB EAST = new AxisAlignedBB(0D / 16D, 5D / 16D, 5D / 16D,
        6D / 16D, 11D / 16D, 11D / 16D);
    private static final AxisAlignedBB WEST = new AxisAlignedBB(16D / 16D, 5D / 16D, 5D / 16D,
        10D / 16D, 11D / 16D, 11D / 16D);
    private static final AxisAlignedBB SOUTH = new AxisAlignedBB(5D / 16D, 5D / 16D, 0D / 16D,
        11D / 16D, 11D / 16D, 6D / 16D);
    private static final AxisAlignedBB NORTH = new AxisAlignedBB(5D / 16D, 5D / 16D, 16D / 16D,
        11D / 16D, 11D / 16D, 10D / 16D);
    private static final AxisAlignedBB UP = new AxisAlignedBB(5D / 16D, 0, 5D / 16D, 11D / 16D,
        6D / 16D, 11D / 16D);
    private static final AxisAlignedBB DOWN = new AxisAlignedBB(5D / 16D, 10D / 16D, 5D / 16D,
        11D / 16D, 16D / 16D, 11D / 16D);

    public BlockNetworkRelay() {
        super("network_relay", Material.IRON, 0.0F, true);
        this.setHardness(5.0F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos,
        EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing enumfacing = state.getValue(FACING);
        switch (enumfacing) {
            case EAST:
                return EAST;
            case WEST:
                return WEST;
            case SOUTH:
                return SOUTH;
            case NORTH:
                return NORTH;
            case UP:
                return UP;
            case DOWN:
                return DOWN;
        }

        throw new RuntimeException(
            "Encountered an EnumFacing that was not EAST, WEST, SOUTH, NORTH, UP, or DOWN. " +
                "This should never, ever happen.");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for
     * adjustments to the IBlockstate
     */
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
        float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing);
    }

    /**
     * Convert the given metadata into a BlockState for this block
     */
    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing;

        switch (meta & 7) {
            case 0:
                enumfacing = EnumFacing.DOWN;
                break;
            case 1:
                enumfacing = EnumFacing.EAST;
                break;
            case 2:
                enumfacing = EnumFacing.WEST;
                break;
            case 3:
                enumfacing = EnumFacing.SOUTH;
                break;
            case 4:
                enumfacing = EnumFacing.NORTH;
                break;
            case 5:
            default:
                enumfacing = EnumFacing.UP;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(IBlockState state) {
        int i;

        switch (state.getValue(FACING)) {
            case EAST:
                i = 1;
                break;
            case WEST:
                i = 2;
                break;
            case SOUTH:
                i = 3;
                break;
            case NORTH:
                i = 4;
                break;
            case UP:
            default:
                i = 5;
                break;
            case DOWN:
                i = 0;
        }

        return i;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation, ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.network_relay", VSConfig.networkRelayLimit));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityNetworkRelay();
    }
}
