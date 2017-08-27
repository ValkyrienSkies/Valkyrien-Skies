package valkyrienwarfare.fixes;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.List;

public interface IInventoryPlayerFix {

	static IInventoryPlayerFix getFixFromInventory(InventoryPlayer toWrap) {
		return IInventoryPlayerFix.class.cast(toWrap);
	}

	List<NonNullList<ItemStack>> getAllInventories();
}
