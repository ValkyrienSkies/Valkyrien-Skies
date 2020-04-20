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

public class BlockGiantPropellerPart extends BaseBlock implements ITileEntityProvider,
    IBlockForceProvider {

    public BlockGiantPropellerPart() {
        super("giant_propeller_part", Material.WOOD, 0.0F, true);
        this.setHardness(5.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.giant_propeller_part"));
                itemInformation.add(TextFormatting.BLUE + "" + TextFormatting.ITALIC + I18n.format("tooltip.vs_control.wrench_usage"));
    }

    @Nullable
    @Override
    public Vector3dc getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state,
                                              PhysicsObject physicsObject, double secondsToApply) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityGiantPropellerPart) {
            TileEntityGiantPropellerPart tileCompressorPart = (TileEntityGiantPropellerPart) tileEntity;
            return tileCompressorPart.getForceOutputUnoriented(secondsToApply, physicsObject);
        }
        return null;
    }

    @Override
    public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state,
        double secondsToApply) {
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityGiantPropellerPart) {
            ((TileEntityGiantPropellerPart) tile).disassembleMultiblock();
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityGiantPropellerPart();
    }

    // Lighting crap
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
