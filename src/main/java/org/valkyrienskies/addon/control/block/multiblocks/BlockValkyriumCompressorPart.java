package org.valkyrienskies.addon.control.block.multiblocks;

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
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.util.BaseBlock;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

import javax.annotation.Nullable;
import java.util.List;

public class BlockValkyriumCompressorPart extends BaseBlock implements ITileEntityProvider,
    IBlockForceProvider {

    // The maximum thrust in newtons that each compressor block can provide.
    public static final double COMPRESSOR_PART_MAX_THRUST = 1300000;

    public BlockValkyriumCompressorPart() {
        super("valkyrium_compressor_part", Material.IRON, 0.0F, true);
        this.setHardness(6.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.valkyrium_compressor_part_1"));
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.valkyrium_compressor_part_2"));
        itemInformation.add(TextFormatting.BLUE + "" + TextFormatting.ITALIC + I18n.format("tooltip.vs_control.wrench_usage"));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityValkyriumCompressorPart(COMPRESSOR_PART_MAX_THRUST);
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
    public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state,
        double secondsToApply) {
        return false;
    }

    @Override
    public Vector3dc getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state,
                                              PhysicsObject physicsObject,
                                              double secondsToApply) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityValkyriumCompressorPart) {
            TileEntityValkyriumCompressorPart tileCompressorPart = (TileEntityValkyriumCompressorPart) tileEntity;
            return tileCompressorPart.getForceOutputUnoriented(secondsToApply, physicsObject);
        }
        return null;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityValkyriumCompressorPart) {
            ((TileEntityValkyriumCompressorPart) tile).disassembleMultiblock();
        }
        super.breakBlock(worldIn, pos, state);
    }

}
