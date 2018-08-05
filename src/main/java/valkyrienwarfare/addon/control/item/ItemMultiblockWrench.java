package valkyrienwarfare.addon.control.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.MultiblockRegistry;
import valkyrienwarfare.addon.control.block.multiblocks.IMulitblockSchematic;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityBigEnginePart;

public class ItemMultiblockWrench extends Item {

	public ItemMultiblockWrench() {
		this.setMaxStackSize(1);
		this.setMaxDamage(80);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return EnumActionResult.FAIL;
		}
		IBlockState clickedState = worldIn.getBlockState(pos);
		Block block = clickedState.getBlock();
		TileEntity blockTile = worldIn.getTileEntity(pos);
		
		if (blockTile instanceof TileEntityBigEnginePart) {
			IMulitblockSchematic multiblockSchematic = MultiblockRegistry.getSchematicByID(1);
			System
			.out.println("close");
			if (multiblockSchematic.canCreateMultiblock(worldIn, pos)) {
				System.out.println("YE");
				multiblockSchematic.createMultiblock(worldIn, pos);
				return EnumActionResult.PASS;
			}
		}
		
		return EnumActionResult.FAIL;
	}
}
