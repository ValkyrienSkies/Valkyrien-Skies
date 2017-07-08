package ValkyrienWarfareBase.Mixin.entity.player;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import ValkyrienWarfareBase.Fixes.IInventoryPlayerFix;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

@Mixin(InventoryPlayer.class)
public class MixinInventoryPlayer implements IInventoryPlayerFix {

	@Override
	public List<NonNullList<ItemStack>> getAllInventories() {
		return allInventories;
	}

	@Shadow @Final
    public List<NonNullList<ItemStack>> allInventories;

}
