package ValkyrienWarfareControl.GUI;

import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ControlGUIHandler implements IGuiHandler{

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID == ControlGUIEnum.HoverCraftController.ordinal()){
			TileEntityHoverController tile = (TileEntityHoverController) world.getTileEntity(new BlockPos(x,y,z));
			((EntityPlayerMP)player).connection.sendPacket(tile.getUpdatePacket());
			return new HovercraftControllerContainer(tile);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID == ControlGUIEnum.HoverCraftController.ordinal()){
			return new HovercraftControllerGUI(player,(TileEntityHoverController) world.getTileEntity(new BlockPos(x,y,z)));
		}
		return null;
	}

}