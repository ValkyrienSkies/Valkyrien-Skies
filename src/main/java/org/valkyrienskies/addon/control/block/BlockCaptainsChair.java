package org.valkyrienskies.addon.control.block;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.tileentity.TileEntityCaptainsChair;
import org.valkyrienskies.addon.control.util.BaseBlock;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.interaction.EntityDraggable;
import org.valkyrienskies.mod.common.physmanagement.interaction.IDraggable;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

public class BlockCaptainsChair extends BlockPilotableBasic {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockCaptainsChair() {
        super("captains_chair", Material.WOOD, 4.0F);
    }

    public static double getChairYaw(IBlockState state, BlockPos pos) {
        EnumFacing enumFace = state.getValue(BlockCaptainsChair.FACING);
        double chairYaw = -enumFace.getHorizontalAngle() - 90;
        return chairYaw;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
        EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(worldIn, pos);
            if (physicsObject.isPresent()) {
                    TileEntity tileEntity = worldIn.getTileEntity(pos);
                    if (tileEntity instanceof TileEntityCaptainsChair) {
                        Vector playerPos = new Vector(playerIn);

                        physicsObject.get()
                            .getShipTransformationManager()
                            .fromLocalToGlobal(playerPos);

                        playerIn.posX = playerPos.x;
                        playerIn.posY = playerPos.y;
                        playerIn.posZ = playerPos.z;

                        IDraggable entityDraggable = EntityDraggable
                            .getDraggableFromEntity(playerIn);
                        // Only mount the player if they're standing on the ship.
                        if (entityDraggable.getWorldBelowFeet() == physicsObject.get()) {
                            Vector localMountPos = getPlayerMountOffset(state, pos);
                            ValkyrienUtils.fixEntityToShip(playerIn, localMountPos,
                                    physicsObject.get());
                        }

                        ((TileEntityCaptainsChair) tileEntity).setPilotEntity(playerIn);
                        physicsObject.get()
                            .getShipTransformationManager()
                            .fromGlobalToLocal(playerPos);

                        playerIn.posX = playerPos.x;
                        playerIn.posY = playerPos.y;
                        playerIn.posZ = playerPos.z;
                    }

            }
        }

        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation, ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.BLUE + I18n
            .format("tooltip.vs_control.captains_chair_1"));
        itemInformation.add(TextFormatting.RED + "" + TextFormatting.ITALIC + I18n
            .format("tooltip.vs_control.captains_chair_2"));
    }

    private Vector getPlayerMountOffset(IBlockState state, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
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
        return new TileEntityCaptainsChair();
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
        float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING,
            placer.isSneaking() ? placer.getHorizontalFacing().getOpposite()
                : placer.getHorizontalFacing());
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX,
        float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing facingHorizontal = placer.getHorizontalFacing();

        if (!placer.isSneaking()) {
            facingHorizontal = facingHorizontal.getOpposite();
        }

        return this.getDefaultState().withProperty(FACING, facingHorizontal);
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
        int i = state.getValue(FACING)
            .getIndex();
        return i;
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