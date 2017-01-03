package ValkyrienWarfareWorld;

import java.util.Random;

import ValkyrienWarfareBase.CoreMod.CallRunner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEtheriumOre extends Block{

	public BlockEtheriumOre(Material materialIn) {
		super(materialIn);
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state){
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn){
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }
	
	@Override
	public int tickRate(World worldIn){
        return 2;
    }

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            tryFallingUp(worldIn, pos);
        }
    }
	
	private void tryFallingUp(World worldIn, BlockPos pos){
		BlockPos downPos = pos.up();
		if ((worldIn.isAirBlock(downPos) || canFallThrough(worldIn.getBlockState(downPos))) && pos.getY() >= 0)
        {
            int i = 32;

            if (!BlockFalling.fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32)))
            {
                if (!worldIn.isRemote)
                {
                	//Start falling up
                	EntityFallingUpBlock entityfallingblock = new EntityFallingUpBlock(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, worldIn.getBlockState(pos));
                    worldIn.spawnEntityInWorld(entityfallingblock);
                }
            }
            else
            {
                IBlockState state = worldIn.getBlockState(pos);
                worldIn.setBlockToAir(pos);
                BlockPos blockpos;

                for (blockpos = pos.up(); (worldIn.isAirBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos))) && blockpos.getY() < 255; blockpos = blockpos.up())
                {
                    ;
                }

                if (blockpos.getY() < 255)
                {
                	CallRunner.onSetBlockState(worldIn, blockpos.down(), state, 3);
                }
            }
        }
	}
	
	//Ripped from BlockFalling class for consistancy with game mechanics
	public static boolean canFallThrough(IBlockState state){
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return block == Blocks.FIRE || material == Material.AIR || material == Material.WATER || material == Material.LAVA;
    }

}
