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
import org.valkyrienskies.addon.control.tileentity.TileEntityGyroscopeDampener;
import org.valkyrienskies.mod.common.block.IBlockTorqueProvider;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;

public class BlockGyroscopeDampener extends Block implements ITileEntityProvider,
    IBlockTorqueProvider {

    public BlockGyroscopeDampener(Material materialIn) {
        super(materialIn);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation
            .add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.gyroscope_dampener"));
    }

    @Override
    public Vector getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
        TileEntity thisTile = physicsCalculations.getParent().world().getTileEntity(pos);
        if (thisTile instanceof TileEntityGyroscopeDampener) {
            TileEntityGyroscopeDampener tileGyroscope = (TileEntityGyroscopeDampener) thisTile;
            return tileGyroscope.getTorqueInGlobal(physicsCalculations, pos);
        }
        return null;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityGyroscopeDampener();
    }

    @Override
    public int getBlockSortingIndex() {
        // Since we're damping angular velocity, we want this to run at the very end, so
        // we give it a large sorting value to put it at the end.
        return 5;
    }

}
