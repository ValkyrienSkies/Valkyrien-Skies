package ValkyrienWarfareControl.GUI;

import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

public class HovercraftControllerContainer extends Container{

	public TileEntityHoverController tile;
	
	public HovercraftControllerContainer(){}
	
	public HovercraftControllerContainer(TileEntityHoverController tileEntity){
		tile = tileEntity;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn){
		return true;
	}
	
	@Override
	public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        if(playerIn instanceof EntityPlayerMP){
        	EntityPlayerMP playerMP = (EntityPlayerMP)playerIn;
        	listeners.remove(playerMP);
        	playerMP.currentWindowId = playerMP.inventoryContainer.windowId;
        }
//        this.lowerChestInventory.closeInventory(playerIn);
    }

}