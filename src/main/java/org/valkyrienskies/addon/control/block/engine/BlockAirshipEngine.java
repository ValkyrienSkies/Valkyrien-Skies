package org.valkyrienskies.addon.control.block.engine;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.tileentity.TileEntityPropellerEngine;
import org.valkyrienskies.addon.control.util.BaseBlock;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.JOML;

/**
 * All engines should extend this class, that way other kinds of engines can be made without making
 * tons of new classes for them. Only engines that add new functionality should have their own
 * class.
 */
public abstract class BlockAirshipEngine extends BaseBlock implements IBlockForceProvider,
    ITileEntityProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    protected double enginePower;

    public BlockAirshipEngine(String name, Material mat, double enginePower, float hardness) {
        super(name + "_engine", mat, 0.0F, true);
        this.setEnginePower(enginePower);
        this.setHardness(hardness);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
        float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState()
            .withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        // &7 to remove any higher bits
        return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & 7));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = state.getValue(FACING).getIndex();
        return i;
    }

    @Override
    public Vector3dc getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state,
                                              PhysicsObject physicsObject, double secondsToApply) {
        Vector3d acting = new Vector3d(0, 0, 0);
        if (!world.isBlockPowered(pos)) {
            return acting;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityPropellerEngine) {
            //Just set the Thrust to be the maximum
            ((TileEntityPropellerEngine) tileEntity).updateTicksSinceLastRecievedSignal();
            ((TileEntityPropellerEngine) tileEntity).setThrustMultiplierGoal(1D);
            return ((TileEntityPropellerEngine) tileEntity)
                .getForceOutputUnoriented(secondsToApply, physicsObject);
        }

        return acting;
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
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos,
        EnumFacing face) {
        return true;
    }

    /**
     * Used for calculating force applied to the airship by an engine. Override this in your
     * subclasses to make engines that are more dynamic than simply being faster engines.
     */
    public double getEnginePower() {
        return this.enginePower;
    }

    public TileEntity createNewTileEntity(World worldIn, int meta) {
        IBlockState state = getStateFromMeta(meta);
        return new TileEntityPropellerEngine(JOML.convertTo3d(state.getValue(FACING).getOpposite().getDirectionVec()), true, getEnginePower());
    }

    public void setEnginePower(double power) {
        this.enginePower = power;
    }

}
