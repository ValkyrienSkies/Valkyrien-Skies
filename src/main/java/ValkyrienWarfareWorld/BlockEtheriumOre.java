package ValkyrienWarfareWorld;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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

}
