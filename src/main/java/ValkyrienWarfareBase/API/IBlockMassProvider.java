package ValkyrienWarfareBase.API;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//Blocks implement this to add a variable mass based on certain conditions
public interface IBlockMassProvider {

	double getBlockMass(World world, BlockPos pos, IBlockState state);

}
