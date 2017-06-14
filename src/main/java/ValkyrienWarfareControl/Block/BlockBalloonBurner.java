package ValkyrienWarfareControl.Block;

import java.util.List;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Balloon.BalloonDetector;
import ValkyrienWarfareControl.Balloon.BalloonProcessor;
import ValkyrienWarfareControl.TileEntity.BalloonBurnerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockBalloonBurner extends Block implements ITileEntityProvider {

	public BlockBalloonBurner(Material materialIn) {
		super(materialIn);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
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
						placer.sendMessage(new TextComponentString("No balloon above"));
					} else {
						placer.sendMessage(new TextComponentString("Created a new Balloon"));

						BalloonProcessor processor = BalloonProcessor.makeProcessorForDetector(wrapperEntity, detector);

						wrapperEntity.wrapping.balloonManager.addBalloonProcessor(processor);
						// System.out.println("Balloon Walls Are " + detector.balloonWalls.size());
					}
				} else {
					placer.sendMessage(new TextComponentString("Hooked onto Exisiting Balloon"));
				}
			}
		}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.RED + TextFormatting.ITALIC + "Unfinished until v_0.91_alpha");
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
