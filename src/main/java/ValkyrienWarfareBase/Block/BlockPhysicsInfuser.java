package ValkyrienWarfareBase.Block;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.Capability.IAirshipCounterCapability;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import ValkyrienWarfareBase.Relocation.DetectorManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class BlockPhysicsInfuser extends Block {

	public BlockPhysicsInfuser(Material materialIn) {
		super(materialIn);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(worldIn);
			if (manager != null) {
				PhysicsWrapperEntity wrapperEnt = manager.getManagingObjectForChunk(worldIn.getChunkFromBlockCoords(pos));
				if (wrapperEnt != null) {
					wrapperEnt.wrapping.doPhysics = !wrapperEnt.wrapping.doPhysics;
					return true;
				}
			}
			PhysicsWrapperEntity wrapper = new PhysicsWrapperEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, DetectorManager.DetectorIDs.ShipSpawnerGeneral.ordinal());
			worldIn.spawnEntityInWorld(wrapper);
			
			if (ValkyrienWarfareMod.canChangeAirshipCounter(true, playerIn))	{
				IAirshipCounterCapability counter = playerIn.getCapability(ValkyrienWarfareMod.airshipCounter, null);
				counter.onCreate();
				//playerIn.addChatMessage(new TextComponentString("You've made " + counter.getAirshipCount() + " airships!"));
			} else {
				playerIn.addChatMessage(new TextComponentString("You've made too many airships! The limit per player is " + ValkyrienWarfareMod.maxAirships));
			}
		}
		return true;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

}
