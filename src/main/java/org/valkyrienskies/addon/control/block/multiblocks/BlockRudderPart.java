package org.valkyrienskies.addon.control.block.multiblocks;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.util.BaseBlock;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.ship_handling.PhysicsObject;

public class BlockRudderPart extends BaseBlock implements ITileEntityProvider, IBlockForceProvider {

    public BlockRudderPart() {
        super("rudder_part", Material.WOOD, 0.0F, true);
        this.setHardness(5.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.rudder_part"));
        itemInformation.add(TextFormatting.BLUE + "" + TextFormatting.ITALIC + I18n.format("tooltip.vs_control.wrench_usage"));
    }

    private static Optional<Double> getRudderRotationDegrees(World world, BlockPos pos) {
        return Optional.empty();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityRudderPart();
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
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityRudderPart) {
            ((TileEntityRudderPart) tile).disassembleMultiblock();
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state,
        PhysicsObject physicsObject,
        double secondsToApply) {
        if (world.getTileEntity(pos) instanceof TileEntityRudderPart) {
            TileEntityRudderPart tileEntity = (TileEntityRudderPart) world
                .getTileEntity(pos);
            Vector forceBeforeTimeScale = tileEntity.calculateForceFromVelocity(physicsObject);
            if (forceBeforeTimeScale != null && forceBeforeTimeScale.lengthSq() > 1) {
                // System.out.println(forceBeforeTimeScale.toRoundedString());
                return forceBeforeTimeScale.getProduct(secondsToApply);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Vector getCustomBlockForcePosition(World world, BlockPos pos, IBlockState state,
        PhysicsObject physicsObject,
        double secondsToApply) {
        if (world.getTileEntity(pos) instanceof TileEntityRudderPart) {
            TileEntityRudderPart tileEntity = (TileEntityRudderPart) world
                .getTileEntity(pos);
            return null; // tileEntity.getForcePositionInShipSpace();
        } else {
            return null;
        }
    }

    @Override
    public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state,
        double secondsToApply) {
        return true;
    }

}
