package ValkyrienWarfareControl.Block;

import java.util.List;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.GUI.ControlGUIEnum;
import ValkyrienWarfareControl.Item.ItemSystemLinker;
import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockHovercraftController extends Block implements ITileEntityProvider {

	public BlockHovercraftController(Material materialIn) {
		super(materialIn);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);
		ItemStack heldItem = playerIn.getHeldItem(hand);
		if (heldItem != null && heldItem.getItem() instanceof ItemSystemLinker) {
			return false;
		}
		if (wrapper != null) {
			if (!worldIn.isRemote) {
				if (playerIn instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) playerIn;
					int realWindowId = player.currentWindowId;

					// TODO: Fix this, I have to reset the window Id's because there is no container on client side, resulting in the client never changing its window id

					player.currentWindowId = player.inventoryContainer.windowId - 1;
					player.openGui(ValkyrienWarfareControlMod.instance, ControlGUIEnum.HoverCraftController.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());

					player.currentWindowId = realWindowId;
					// player.openContainer = playerIn.inventoryContainer;
				}

			}
			return true;
		}
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.BLUE + "Used to automatically control the thrust output of Ether Compressors, allowing for stable flight.");
		itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.GRAY + TextFormatting.ITALIC + "Auto stabalization control can be disabled with a redstone signal.");
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityHoverController();
	}

}