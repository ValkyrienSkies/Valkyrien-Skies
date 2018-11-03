package valkyrienwarfare.addon.control.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.MultiblockRegistry;
import valkyrienwarfare.addon.control.block.multiblocks.IMulitblockSchematic;
import valkyrienwarfare.addon.control.block.multiblocks.RudderAxleMultiblockSchematic;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityEthereumCompressorPart;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityEthereumEnginePart;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityRudderAxlePart;

public class ItemWrench extends Item {

	public ItemWrench() {
		this.setMaxStackSize(1);
		this.setMaxDamage(80);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return EnumActionResult.SUCCESS;
		}
		IBlockState clickedState = worldIn.getBlockState(pos);
		Block block = clickedState.getBlock();
		TileEntity blockTile = worldIn.getTileEntity(pos);

		if (blockTile instanceof TileEntityEthereumEnginePart) {
			List<IMulitblockSchematic> ethereumEngineMultiblockSchematics = MultiblockRegistry
					.getSchematicsWithPrefix("multiblock_ether_engine");
			for (IMulitblockSchematic schematic : ethereumEngineMultiblockSchematics) {
				if (schematic.attemptToCreateMultiblock(worldIn, pos)) {
					return EnumActionResult.SUCCESS;
				}
			}
		}

		if (blockTile instanceof TileEntityEthereumCompressorPart) {
			List<IMulitblockSchematic> ethereumEngineMultiblockSchematics = MultiblockRegistry
					.getSchematicsWithPrefix("multiblock_ether_compressor");
			for (IMulitblockSchematic schematic : ethereumEngineMultiblockSchematics) {
				if (schematic.attemptToCreateMultiblock(worldIn, pos)) {
					return EnumActionResult.SUCCESS;
				}
			}
		}

		if (blockTile instanceof TileEntityRudderAxlePart) {
			List<IMulitblockSchematic> rudderAxelMultiblockSchematics = MultiblockRegistry
					.getSchematicsWithPrefix("multiblock_rudder_axle");
			for (IMulitblockSchematic schematic : rudderAxelMultiblockSchematics) {
				RudderAxleMultiblockSchematic rudderSchem = (RudderAxleMultiblockSchematic) schematic;
				if (facing.getAxis() != rudderSchem.getAxleAxisDirection().getAxis()) {
					if (rudderSchem.getAxleFacingDirection() == facing) {
						if (schematic.attemptToCreateMultiblock(worldIn, pos)) {
							System.out.println(facing);
							System.out.println(rudderSchem.getSchematicID());
							return EnumActionResult.SUCCESS;
						}
					}

				}
			}
		}
		
		return EnumActionResult.PASS;
	}
}
