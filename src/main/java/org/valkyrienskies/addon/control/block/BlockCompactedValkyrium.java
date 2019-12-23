package org.valkyrienskies.addon.control.block;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;

public class BlockCompactedValkyrium extends Block implements IBlockForceProvider {

    private static final double DOPED_ETHEREUM_FORCE = 200000;
    private static final String[] lore = new String[]{
        "" + TextFormatting.GRAY + TextFormatting.ITALIC + TextFormatting.BOLD +
            "Force:", "  " + DOPED_ETHEREUM_FORCE + " Newtons"};

    public BlockCompactedValkyrium(Material materialIn) {
        super(materialIn);
    }

    /**
     * The force Vector this block gives within its local space (Not within World space).
     */
    @Nullable
    @Override
    public Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state,
        PhysicsObject physicsObject, double secondsToApply) {
        // TODO: Shouldn't this depend on the gravity vector?
        return new Vector(0, DOPED_ETHEREUM_FORCE * secondsToApply, 0);
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
        Collections.addAll(itemInformation, lore);
    }

}
