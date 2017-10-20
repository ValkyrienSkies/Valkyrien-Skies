package valkyrienwarfare.mixin.entity.player;

import valkyrienwarfare.fixes.IInventoryPlayerFix;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer implements IInventoryPlayerFix {

	@Shadow
	@Final
	public List<NonNullList<ItemStack>> allInventories;

	@Override
	public List<NonNullList<ItemStack>> getAllInventories() {
		return allInventories;
	}

}
