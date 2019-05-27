package valkyrienwarfare.mod.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import valkyrienwarfare.mod.tileentity.TileEntityPhysicsInfuser;

public class ContainerPhysicsInfuser extends Container {

    private static final int SIZE_INVENTORY = 5;
    private final TileEntityPhysicsInfuser te;
    private final IItemHandler handler;

    public ContainerPhysicsInfuser(EntityPlayer player, TileEntityPhysicsInfuser te) {
        this.te = te;
        this.handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        // Add physics infuser slots
        this.addSlotToContainer(new SlotItemHandler(handler, 0, 45, 22));
        this.addSlotToContainer(new SlotItemHandler(handler, 1, 45, 54));
        this.addSlotToContainer(new SlotItemHandler(handler, 2, 79, 54));
        this.addSlotToContainer(new SlotItemHandler(handler, 3, 112, 54));
        this.addSlotToContainer(new SlotItemHandler(handler, 4, 112, 22));

        // Add player inventory slots
        InventoryPlayer playerInventory = player.inventory;
        // note that the slot numbers are within the player inventory so can
        // be same as the tile entity inventory
        int i;
        for (i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(playerInventory, j+i*9+9,
                        8+j*18, 84+i*18));
            }
        }

        // add hotbar slots
        for (i = 0; i < 9; ++i) {
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18,
                    142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return te.isUseableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex) {
        ItemStack itemStack1 = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack1 = itemStack2.copy();

            if (slotIndex >= SIZE_INVENTORY) {
                // check if there is a grinding recipe for the stack
                if (slotIndex >= SIZE_INVENTORY && slotIndex < SIZE_INVENTORY+27) {
                    // player inventory slots
                    if (!mergeItemStack(itemStack2, 0, SIZE_INVENTORY, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex >= SIZE_INVENTORY+27
                        && slotIndex < SIZE_INVENTORY+36
                        && !mergeItemStack(itemStack2, SIZE_INVENTORY+1,
                        SIZE_INVENTORY+27, false)) {
                    // hotbar slots
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(itemStack2, SIZE_INVENTORY,
                    SIZE_INVENTORY+36, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.stackSize == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemStack2.stackSize == itemStack1.stackSize) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemStack2);
        }

        return itemStack1;
    }

}
