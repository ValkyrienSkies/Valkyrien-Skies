package org.valkyrienskies.addon.world.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.world.EntityFallingUpBlock;
import org.valkyrienskies.addon.world.ValkyrienSkiesWorld;
import org.valkyrienskies.addon.world.util.BaseBlock;

public class BlockValkyriumOre extends BaseBlock {

    public BlockValkyriumOre() {
        super("valkyrium_ore", Material.ROCK, 3.0F / 15.0F, true); // Slight glow
        this.setHardness(3.0F);
    }

    // Ripped from BlockFalling class for consistancy with game mechanics
    public static boolean canFallThrough(IBlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return block == Blocks.FIRE || material == Material.AIR || material == Material.WATER
            || material == Material.LAVA;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation, ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.BLUE + TextFormatting.ITALIC + I18n.format("tooltip.vs_world.valkyrium_ore_1"));
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.BLUE + TextFormatting.ITALIC + I18n.format("tooltip.vs_world.valkyrium_ore_2"));
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn,
        BlockPos fromPos) {
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    @Override
    public int tickRate(World worldIn) {
        return 2;
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            tryFallingUp(worldIn, pos);
        }
    }

    private void tryFallingUp(World worldIn, BlockPos pos) {
        BlockPos downPos = pos.up();
        if ((worldIn.isAirBlock(downPos) || canFallThrough(worldIn.getBlockState(downPos)))
            && pos.getY() >= 0) {
            int i = 32;

            if (!BlockFalling.fallInstantly && worldIn
                .isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
                if (!worldIn.isRemote) {
                    // Start falling up
                    EntityFallingUpBlock entityfallingblock = new EntityFallingUpBlock(worldIn,
                        (double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D,
                        worldIn.getBlockState(pos));
                    worldIn.spawnEntity(entityfallingblock);
                }
            } else {
                IBlockState state = worldIn.getBlockState(pos);
                worldIn.setBlockToAir(pos);
                BlockPos blockpos;

                for (blockpos = pos.up(); (worldIn.isAirBlock(blockpos) || canFallThrough(
                    worldIn.getBlockState(blockpos))) && blockpos.getY() < 255;
                    blockpos = blockpos.up()) {
                }

                if (blockpos.getY() < 255) {
                    worldIn.setBlockState(blockpos.down(), state, 3);
                }
            }
        }
    }

    //Ore Properties Start Here
    @Nullable
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ValkyrienSkiesWorld.INSTANCE.valkyriumCrystal;
    }

    public int quantityDroppedWithBonus(int fortune, Random random) {
        return this.quantityDropped(random) + random.nextInt(fortune + 1);
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random) {
        return 4 + random.nextInt(4);
    }

    @Override
    public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos,
        int fortune) {
        if (this.getItemDropped(state, RANDOM, fortune) != Item.getItemFromBlock(this)) {
            return 16 + RANDOM.nextInt(10);
        }
        return 0;
    }


}
