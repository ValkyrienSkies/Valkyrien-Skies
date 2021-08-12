package org.valkyrienskies.mod.common.block;

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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.entity.EntityShipMovementData;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.tileentity.TileEntityBoatChair;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class BlockBoatChair extends BlockPilotableBasic implements IBlockForceProvider, IBlockTorqueProvider {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockBoatChair() {
        super("boat_chair", Material.WOOD, 4.0F);
    }

    protected BlockBoatChair(String name, Material mat, float hardness) {
        super(name, mat, hardness);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
                                    EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(worldIn, pos);
            if (physicsObject.isPresent()) {
                TileEntity tileEntity = worldIn.getTileEntity(pos);
                if (tileEntity instanceof TileEntityBoatChair) {
                    Vector3d playerPos = new Vector3d(playerIn.posX, playerIn.posY, playerIn.posZ);

                    physicsObject.get()
                            .getShipTransformationManager()
                            .getCurrentTickTransform()
                            .transformPosition(playerPos, TransformType.SUBSPACE_TO_GLOBAL);

                    playerIn.posX = playerPos.x;
                    playerIn.posY = playerPos.y;
                    playerIn.posZ = playerPos.z;

                    // Only mount the player if they're standing on the ship.
                    final EntityShipMovementData entityShipMovementData = ValkyrienUtils.getEntityShipMovementDataFor(playerIn);
                    if (entityShipMovementData.getTicksSinceTouchedShip() == 0 &&
                            (entityShipMovementData.getLastTouchedShip() == physicsObject.get().getShipData())) {
                        Vector3dc localMountPos = getPlayerMountOffset(state, pos);
                        ValkyrienUtils.fixEntityToShip(playerIn, localMountPos,
                                physicsObject.get());
                    }

                    ((TileEntityBoatChair) tileEntity).setPilotEntity(playerIn);
                    physicsObject.get()
                            .getShipTransformationManager()
                            .getCurrentTickTransform()
                            .transformPosition(playerPos, TransformType.GLOBAL_TO_SUBSPACE);

                    playerIn.posX = playerPos.x;
                    playerIn.posY = playerPos.y;
                    playerIn.posZ = playerPos.z;
                }

            }
        }

        return true;
    }

    private Vector3dc getPlayerMountOffset(IBlockState state, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        switch (facing) {
            case NORTH:
                return new Vector3d(pos.getX() + .5D, pos.getY(), pos.getZ() + .6D);
            case SOUTH:
                return new Vector3d(pos.getX() + .5D, pos.getY(), pos.getZ() + .4D);
            case WEST:
                return new Vector3d(pos.getX() + .6D, pos.getY(), pos.getZ() + .5D);
            case EAST:
                return new Vector3d(pos.getX() + .4D, pos.getY(), pos.getZ() + .5D);
            default:
                return new Vector3d(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player,
                               List<String> itemInformation, ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.BLUE + I18n
                .format("tooltip.valkyrienskies.boat_chair"));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityBoatChair();
    }

    @Nullable
    @Override
    public Vector3dc getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state, PhysicsObject physicsObject, double secondsToApply) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityBoatChair) {
            return ((TileEntityBoatChair) tileEntity).getBlockForceInShipSpace(world, pos, state, physicsObject, secondsToApply);
        }
        return null;
    }

    @Override
    public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state, double secondsToApply) {
        return false;
    }

    @Nullable
    @Override
    public Vector3dc getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
        final World world = physicsCalculations.getParent().getWorld();
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityBoatChair) {
            return ((TileEntityBoatChair) tileEntity).getTorqueInGlobal(physicsCalculations);
        }
        return null;
    }

    // region chair rotation
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

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return null;
    }

    public double getChairYaw(IBlockState state) {
        EnumFacing enumFace = state.getValue(BlockCaptainsChair.FACING);
        double chairYaw = -enumFace.getHorizontalAngle() - 90;
        return chairYaw;
    }

    @Nullable
    @Override
    public Vector3dc getCustomBlockForcePosition(World world, BlockPos pos, IBlockState state,
                                                  PhysicsObject physicsObject,
                                                  double secondsToApply) {
        return physicsObject.getPhysicsCalculations().getPhysCenterOfMass();
    }
    // endregion
}
