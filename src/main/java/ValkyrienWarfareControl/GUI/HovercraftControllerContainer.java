package ValkyrienWarfareControl.GUI;

import java.util.ArrayList;

import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class HovercraftControllerContainer extends Container {

	public TileEntityHoverController tile;
	private IInventory inputSlots;

	public HovercraftControllerContainer() {
	}

	public HovercraftControllerContainer(InventoryPlayer playerInventory, TileEntityHoverController tileEntity) {
		tile = tileEntity;
		this.inputSlots = new InventoryBasic("Repair", true, 2);

		this.addSlotToContainer(new Slot(this.inputSlots, 0, 27, 47));
		this.addSlotToContainer(new Slot(this.inputSlots, 1, 76, 47));
		playerInventory.player.closeScreen();

	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		if (playerIn instanceof EntityPlayerMP) {
			EntityPlayerMP playerMP = (EntityPlayerMP) playerIn;
			playerMP.sendContainerToPlayer(playerMP.inventoryContainer);
		}
	}

}