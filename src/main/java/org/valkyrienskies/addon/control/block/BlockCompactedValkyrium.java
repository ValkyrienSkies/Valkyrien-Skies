package org.valkyrienskies.addon.control.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.util.BaseBlock;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

import javax.annotation.Nullable;
import java.util.List;

public class BlockCompactedValkyrium extends BaseBlock implements IBlockForceProvider {

    public BlockCompactedValkyrium() {
        super("compacted_valkyrium", Material.GLASS, 4.0F, true);
    }

    /**
     * The force Vector this block gives within its local space (Not within World space).
     */
    @Nullable
    @Override
    public Vector3dc getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state,
                                              PhysicsObject physicsObject, double secondsToApply) {
        // TODO: Shouldn't this depend on the gravity vector?
        return new Vector3d(0, VSConfig.compactedValkyriumLift * secondsToApply, 0);
    }

    /**
     * Blocks that shouldn't have their force rotated (Like Valkyrium Compressors) must return false.
     */
    @Override
    public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state,
        double secondsToApply) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + "" + TextFormatting.BOLD +
            I18n.format("tooltip.vs_control.compacted_valkyrium", VSConfig.compactedValkyriumLift));
    }

}
