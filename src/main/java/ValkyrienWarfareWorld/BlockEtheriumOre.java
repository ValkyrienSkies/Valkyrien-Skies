package ValkyrienWarfareWorld;

import java.util.Random;

import javax.annotation.Nullable;

import ValkyrienWarfareBase.CoreMod.CallRunner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEtheriumOre extends Block {

	public BlockEtheriumOre(Material materialIn) {
		super(materialIn);
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
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
		if ((worldIn.isAirBlock(downPos) || canFallThrough(worldIn.getBlockState(downPos))) && pos.getY() >= 0) {
			int i = 32;

			if (!BlockFalling.fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
				if (!worldIn.isRemote) {
					// Start falling up
					EntityFallingUpBlock entityfallingblock = new EntityFallingUpBlock(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D, worldIn.getBlockState(pos));
					worldIn.spawnEntityInWorld(entityfallingblock);
				}
			} else {
				IBlockState state = worldIn.getBlockState(pos);
				worldIn.setBlockToAir(pos);
				BlockPos blockpos;

				for (blockpos = pos.up(); (worldIn.isAirBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos))) && blockpos.getY() < 255; blockpos = blockpos.up()) {
					;
				}

				if (blockpos.getY() < 255) {
					CallRunner.onSetBlockState(worldIn, blockpos.down(), state, 3);
				}
			}
		}
	}

	// Ripped from BlockFalling class for consistancy with game mechanics
	public static boolean canFallThrough(IBlockState state) {
		Block block = state.getBlock();
		Material material = state.getMaterial();
		return block == Blocks.FIRE || material == Material.AIR || material == Material.WATER || material == Material.LAVA;
	}
	
	//Ore Properties Start Here
	@Nullable
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return ValkyrienWarfareWorldMod.etheriumCrystal;
    }
	
	public int quantityDroppedWithBonus(int fortune, Random random)
    {
        return this.quantityDropped(random) + random.nextInt(fortune + 1);
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random)
    {
        return 4 + random.nextInt(4);
    }
    
    @Override
    public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune)
    {
        if (this.getItemDropped(state, RANDOM, fortune) != Item.getItemFromBlock(this))
        {
            return 16 + RANDOM.nextInt(10);
        }
        return 0;
    }


}
