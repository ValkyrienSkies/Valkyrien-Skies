package ValkyrienWarfareControl.Block;

import java.util.List;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.TileEntity.TileEntityPilotsChair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockShipPilotsChair extends Block implements ITileEntityProvider {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockShipPilotsChair(Material materialIn) {
        super(materialIn);
    }

    public static double getChairYaw(IBlockState state, BlockPos pos) {
        EnumFacing enumFace = state.getValue(BlockShipPilotsChair.FACING);

        double chairYaw = -enumFace.getHorizontalAngle() - 90;

        return chairYaw;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);
            if (wrapper != null) {
                if (playerIn.getLowestRidingEntity() != wrapper.getLowestRidingEntity()) {
                    TileEntity tileEntity = worldIn.getTileEntity(pos);
                    if(tileEntity instanceof TileEntityPilotsChair) {
                    	Vector playerPos = new Vector(playerIn);

                        wrapper.wrapping.coordTransform.fromLocalToGlobal(playerPos);

                        playerIn.posX = playerPos.X;
                        playerIn.posY = playerPos.Y;
                        playerIn.posZ = playerPos.Z;

                        playerIn.startRiding(wrapper);
                        Vector localMountPos = getPlayerMountOffset(state, pos);
                        wrapper.wrapping.fixEntity(playerIn, localMountPos);


                        //Nope
//                        wrapper.wrapping.pilotingController.setPilotEntity((EntityPlayerMP) playerIn, false);

                        ((TileEntityPilotsChair) tileEntity).setPilotEntity(playerIn);

                        wrapper.wrapping.coordTransform.fromGlobalToLocal(playerPos);

                        playerIn.posX = playerPos.X;
                        playerIn.posY = playerPos.Y;
                        playerIn.posZ = playerPos.Z;

                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.BLUE + "Use to mount and control Ships!");
        itemInformation.add(TextFormatting.BOLD + "" + TextFormatting.BOLD + TextFormatting.RED + "Can only be placed on a Ship");
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);
        return wrapper != null;
    }

    private Vector getPlayerMountOffset(IBlockState state, BlockPos pos) {
        EnumFacing facing = (EnumFacing) state.getValue(FACING);
        switch (facing) {
            case NORTH:
                return new Vector(pos.getX() + .5D, pos.getY(), pos.getZ() + .6D);
            case SOUTH:
                return new Vector(pos.getX() + .5D, pos.getY(), pos.getZ() + .4D);
            case WEST:
                return new Vector(pos.getX() + .6D, pos.getY(), pos.getZ() + .5D);
            case EAST:
                return new Vector(pos.getX() + .4D, pos.getY(), pos.getZ() + .5D);
            default:
                return new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityPilotsChair();
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.isSneaking() ? placer.getHorizontalFacing().getOpposite() : placer.getHorizontalFacing());
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing facingHorizontal = placer.getHorizontalFacing();

        if (!placer.isSneaking()) {
            facingHorizontal = facingHorizontal.getOpposite();
        }

        return this.getDefaultState().withProperty(FACING, facingHorizontal);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING});
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getFront(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = ((EnumFacing) state.getValue(FACING)).getIndex();
        return i;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
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