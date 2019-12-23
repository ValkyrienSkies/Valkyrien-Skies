package org.valkyrienskies.addon.control.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.tileentity.TileEntityGyroscopeStabilizer;
import org.valkyrienskies.addon.control.util.BaseBlock;
import org.valkyrienskies.mod.common.block.IBlockTorqueProvider;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;

public class BlockGyroscopeStabilizer extends BaseBlock implements ITileEntityProvider,
    IBlockTorqueProvider {

    public BlockGyroscopeStabilizer() {
        super("gyroscope_stabilizer", Material.IRON, 0.0F, true);
        this.setHardness(5.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation
            .add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.gyroscope_stabilizer"));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityGyroscopeStabilizer();
    }

    @Override
    public Vector getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
        TileEntity thisTile = physicsCalculations.getParent().getWorld().getTileEntity(pos);
        if (thisTile instanceof TileEntityGyroscopeStabilizer) {
            TileEntityGyroscopeStabilizer tileGyroscope = (TileEntityGyroscopeStabilizer) thisTile;
            return tileGyroscope.getTorqueInGlobal(physicsCalculations, pos);
        }
        return null;
    }

}
