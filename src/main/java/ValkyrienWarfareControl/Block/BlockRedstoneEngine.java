package ValkyrienWarfareControl.Block;

import ValkyrienWarfareBase.API.Vector;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRedstoneEngine extends BlockNormalEngine {

	public BlockRedstoneEngine(Material materialIn, double powerMultiplier) {
		super(materialIn, powerMultiplier);
	}
	
	@Override
	public Vector getBlockForce(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
		Vector acting = new Vector(0,0,0);
		if(!world.isBlockPowered(pos)){
			return acting;
		}
		
		double power = enginePower * secondsToApply * world.isBlockIndirectlyGettingPowered(pos);
		
		switch(enumfacing){
			case DOWN: acting = new Vector(0,power,0);
			break;
			case UP: acting = new Vector(0,-power,0);
			break;
			case EAST: acting = new Vector(-power,0,0);
			break;
			case NORTH: acting = new Vector(0,0,power);
			break;
			case WEST: acting = new Vector(power,0,0);
			break;
			case SOUTH: acting = new Vector(0,0,-power);
			break;
		}
		return acting;
	}

}
