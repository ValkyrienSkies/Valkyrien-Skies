package ValkyrienWarfareBase.Block;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import ValkyrienWarfareBase.Relocation.DetectorManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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
		}
		return true;
	}

}
