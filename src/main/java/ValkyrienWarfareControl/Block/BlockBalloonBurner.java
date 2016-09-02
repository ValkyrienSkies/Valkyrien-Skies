package ValkyrienWarfareControl.Block;

import ValkyrienWarfareControl.Balloon.BalloonDetector;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class BlockBalloonBurner extends Block implements ITileEntityProvider{

	public BlockBalloonBurner(Material materialIn) {
		super(materialIn);
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
		BlockPos balloonStart = pos.up(2);
		if(!worldIn.isRemote){
			BalloonDetector detector = new BalloonDetector(balloonStart, worldIn, 25000);
			int balloonSize = detector.foundSet.size();
			if(balloonSize==0){
				System.out.println("Not enclosed");
			}else{
				System.out.println("Balloon Volume is " + balloonSize);
			}
			placer.addChatMessage(new TextComponentString(worldIn.getBlockState(balloonStart)+""));
		}
		return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
	}

}
