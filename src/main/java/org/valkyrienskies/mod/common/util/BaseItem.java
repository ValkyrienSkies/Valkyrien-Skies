package org.valkyrienskies.mod.common.util;

import net.minecraft.item.Item;
import org.valkyrienskies.mod.common.item.BaseModel;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

// Addons need to provide their own copies of this class.
public class BaseItem extends Item implements BaseModel {
	public BaseItem(String name, boolean creativeTab) {
		this.setUnlocalizedName(name);
		this.setRegistryName(name);

		if (creativeTab) {
			// No need to change this for addons
			this.setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
		}

		ValkyrienSkiesMod.ITEMS.add(this);
	}

	// No need to change this for addons
	@Override
	public void registerModels() {
		ValkyrienSkiesMod.proxy.registerItemRender(this, 0);
	}
}
