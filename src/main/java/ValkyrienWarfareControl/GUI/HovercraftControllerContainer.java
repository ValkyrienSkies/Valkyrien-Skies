package ValkyrienWarfareControl.GUI;

import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class HovercraftControllerContainer extends Container{

	public TileEntityHoverController tile;
	
	public HovercraftControllerContainer(){}
	
	public HovercraftControllerContainer(TileEntityHoverController tileEntity){
		tile = tileEntity;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

}