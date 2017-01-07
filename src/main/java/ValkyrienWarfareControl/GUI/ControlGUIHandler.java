package ValkyrienWarfareControl.GUI;

import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ControlGUIHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == ControlGUIEnum.HoverCraftController.ordinal()) {
			TileEntity tileEnt = world.getTileEntity(new BlockPos(x, y, z));
			if (!(tileEnt instanceof TileEntityHoverController)) {
				return null;
			}
			TileEntityHoverController tile = (TileEntityHoverController) tileEnt;
			((EntityPlayerMP) player).connection.sendPacket(tile.getUpdatePacket());
			return new HovercraftControllerContainer(player.inventory, tile);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == ControlGUIEnum.HoverCraftController.ordinal()) {
			return new HovercraftControllerGUI(player, (TileEntityHoverController) world.getTileEntity(new BlockPos(x, y, z)));
		}
		return null;
	}

}