package ValkyrienWarfareControl.Block;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.GUI.ControlGUIEnum;
import ValkyrienWarfareControl.Item.ItemSystemLinker;
import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.FMLOutboundHandler.OutboundTarget;
import net.minecraftforge.fml.common.network.internal.FMLMessage;
import net.minecraftforge.fml.relauncher.Side;

public class BlockHovercraftController extends Block implements ITileEntityProvider {

	public BlockHovercraftController(Material materialIn) {
		super(materialIn);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);
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

				// ModContainer mc = FMLCommonHandler.instance().findContainerFor(ValkyrienWarfareControlMod.instance);
				// if (playerIn instanceof EntityPlayerMP && !(playerIn instanceof FakePlayer))
				// {
				// EntityPlayerMP entityPlayerMP = (EntityPlayerMP) playerIn;
				// Container remoteGuiContainer = NetworkRegistry.INSTANCE.getRemoteGuiContainer(mc, entityPlayerMP, ControlGUIEnum.HoverCraftController.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
				// if (remoteGuiContainer != null)
				// {
				// entityPlayerMP.getNextWindowId();
				// entityPlayerMP.closeContainer();
				// int windowId = entityPlayerMP.currentWindowId;
				// entityPlayerMP.openContainer = remoteGuiContainer;
				// entityPlayerMP.openContainer = entityPlayerMP.inventoryContainer;
				//// entityPlayerMP.openContainer.windowId = windowId;
				//// entityPlayerMP.openContainer.addListener(entityPlayerMP);
				// net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(playerIn, playerIn.openContainer));
				// }
				// }
			}
			return true;
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityHoverController();
	}

}