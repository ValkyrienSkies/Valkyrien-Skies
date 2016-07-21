package ValkyrienWarfareControl.Block;


import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockHovercraftController extends Block implements ITileEntityProvider{

	public BlockHovercraftController(Material materialIn){
		super(materialIn);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
		//Open GUI on client side and request TileData from the server
		return false;
    }
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new TileEntityHoverController();
	}

}