package ValkyrienWarfareControl.Block;

import ValkyrienWarfareBase.API.Vector;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class BlockRedstoneEngine extends BlockNormalEngine {

	public BlockRedstoneEngine(Material materialIn, double powerMultiplier) {
		super(materialIn, powerMultiplier);
	}

	@Override
	public double getEnginePower(World world, BlockPos pos, IBlockState state, Entity shipEntity) {
		return world.isBlockIndirectlyGettingPowered(pos) * this.enginePower;
	}

}
