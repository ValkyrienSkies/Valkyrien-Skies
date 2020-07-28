package org.valkyrienskies.addon.control.container;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.tileentity.TileEntityPhysicsInfuser;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerPhysicsInfuser extends Container {

    private static final int SIZE_INVENTORY = 5;
    private final TileEntityPhysicsInfuser tileEntity;

    public ContainerPhysicsInfuser(EntityPlayer player, TileEntityPhysicsInfuser tileEntity) {
        this.tileEntity = tileEntity;
        IItemHandler handler = tileEntity
            .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        // Add physics infuser slots
        for (TileEntityPhysicsInfuser.EnumInfuserCore infuserCore : TileEntityPhysicsInfuser.EnumInfuserCore
            .values()) {
            this.addSlotToContainer(
                new SlotPhysicsInfuser(handler, infuserCore.coreSlotIndex, infuserCore.guiXPos,
                    infuserCore.guiYPos));
        }

        // Add player inventory slots
        InventoryPlayer playerInventory = player.inventory;
        // note that the slot numbers are within the player inventory so can
        // be same as the tile entity inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9,
                    7 + j * 18, 83 + i * 18));
            }
        }

        // add hotbar slots
        for (int i = 0; i < 9; ++i) {
            addSlotToContainer(new Slot(playerInventory, i, 7 + i * 18,
                141));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tileEntity.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex) {
        ItemStack itemStack1 = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack1 = itemStack2.copy();

            if (slotIndex >= SIZE_INVENTORY) {
                // check if we clicked on the player inventory
                if (slotIndex < SIZE_INVENTORY + 27) {
                    // We clicked a player inventory slot
                    // First try putting the itemstack into the physics infuser
                    if (!mergeItemStack(itemStack2, 0, SIZE_INVENTORY, false)) {
                        // If that failed try putting it into the hotbar
                        if (!mergeItemStack(itemStack2, SIZE_INVENTORY + 27, SIZE_INVENTORY + 35,
                            false)) {
                            // If that failed then we are a failure
                            return ItemStack.EMPTY;
                        }
                    }
                } else if (slotIndex < SIZE_INVENTORY + 36
                    && !mergeItemStack(itemStack2, 0, SIZE_INVENTORY, false)
                    && !mergeItemStack(itemStack2, SIZE_INVENTORY + 1,
                    SIZE_INVENTORY + 27, false)) {
                    // hotbar slots
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(itemStack2, SIZE_INVENTORY,
                SIZE_INVENTORY + 36, false)) {
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

    private static class SlotPhysicsInfuser extends SlotItemHandler {

        private SlotPhysicsInfuser(IItemHandler itemHandler, int index, int xPosition,
            int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            if (stack.getItem() == ValkyrienSkiesControl.INSTANCE.physicsCore) {
                return super.isItemValid(stack);
            } else {
                return false;
            }
        }
    }
}
