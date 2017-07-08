package ValkyrienWarfareBase.Fixes;

import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IInventoryPlayerFix {

	List<NonNullList<ItemStack>> getAllInventories();

	static IInventoryPlayerFix getFixFromInventory(InventoryPlayer toWrap) {
		return IInventoryPlayerFix.class.cast(toWrap);
	}
}
