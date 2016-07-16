package ValkyrienWarfareControl.Block;


import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HovercraftController extends Block implements ITileEntityProvider{

	public HovercraftController(Material materialIn){
		super(materialIn);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return null;
	}

}