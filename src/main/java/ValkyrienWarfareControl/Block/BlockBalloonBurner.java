package ValkyrienWarfareControl.Block;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.IBlockForceProvider;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Balloon.BalloonDetector;
import ValkyrienWarfareControl.Balloon.BalloonProcessor;
import ValkyrienWarfareControl.TileEntity.AntiGravEngineTileEntity;
import ValkyrienWarfareControl.TileEntity.BalloonBurnerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class BlockBalloonBurner extends Block implements ITileEntityProvider {

	public BlockBalloonBurner(Material materialIn) {
		super(materialIn);
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		PhysicsWrapperEntity wrapperEntity = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);
		// Balloons can only be made on an active Ship
		if (wrapperEntity != null) {
			BlockPos balloonStart = pos.up(2);
			if (!worldIn.isRemote) {
				BalloonProcessor existingProcessor = wrapperEntity.wrapping.balloonManager.getProcessorAbovePos(pos);
				if (existingProcessor == null) {

					BalloonDetector detector = new BalloonDetector(balloonStart, worldIn, 25000);
					int balloonSize = detector.foundSet.size();
					if (balloonSize == 0) {
						placer.addChatMessage(new TextComponentString("No balloon above"));
					} else {
						placer.addChatMessage(new TextComponentString("Created a new Balloon"));

						BalloonProcessor processor = BalloonProcessor.makeProcessorForDetector(wrapperEntity, detector);

						wrapperEntity.wrapping.balloonManager.addBalloonProcessor(processor);
						// System.out.println("Balloon Walls Are " + detector.balloonWalls.size());
					}
				} else {
					placer.addChatMessage(new TextComponentString("Hooked onto Exisiting Balloon"));
				}
			}
		}
		return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new BalloonBurnerTileEntity();
	}

	private BalloonBurnerTileEntity getTileEntity(World world, BlockPos pos, IBlockState state, Entity shipEntity) {
		PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEntity;
		PhysicsObject obj = wrapper.wrapping;
		IBlockState controllerState = obj.VKChunkCache.getBlockState(pos);
		TileEntity worldTile = obj.VKChunkCache.getTileEntity(pos);
		if (worldTile == null) {
			return null;
		}
		if (worldTile instanceof BalloonBurnerTileEntity) {
			BalloonBurnerTileEntity burnerTile = (BalloonBurnerTileEntity) worldTile;
			return burnerTile;
		}
		return null;
	}

}
