package ValkyrienWarfareBase.Block;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.ShipType;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import ValkyrienWarfareBase.Relocation.DetectorManager;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class BlockPhysicsInfuser extends Block {
	
	int shipSpawnDetectorID;
	
	public BlockPhysicsInfuser(Material materialIn) {
		super(materialIn);
		shipSpawnDetectorID = DetectorManager.DetectorIDs.ShipSpawnerGeneral.ordinal();
	}
	
	
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.BLUE + "Turns any blocks attatched to this one into a brand new Ship, just be careful not to infuse your entire world");
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(worldIn);
			if (manager != null) {
				PhysicsWrapperEntity wrapperEnt = manager.getManagingObjectForChunk(worldIn.getChunkFromBlockCoords(pos));
				if (wrapperEnt != null) {
					wrapperEnt.wrapping.doPhysics = !wrapperEnt.wrapping.doPhysics;
					return true;
				}
			}
			
			if (ValkyrienWarfareMod.canChangeAirshipCounter(true, playerIn)) {
				PhysicsWrapperEntity wrapper = new PhysicsWrapperEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, shipSpawnDetectorID, ShipType.Full_Unlocked);
				worldIn.spawnEntity(wrapper);
			} else {
				playerIn.sendMessage(new TextComponentString("You've made too many airships! The limit per player is " + ValkyrienWarfareMod.maxAirships));
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