package org.valkyrienskies.mod.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import org.valkyrienskies.mod.common.item.BaseModel;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

// Addons need to provide their own copies of this class.
public class BaseBlock extends Block implements BaseModel {
	public BaseBlock(String name, Material mat, float light, boolean creativeTab) {
		super(mat);
		this.setUnlocalizedName(name);
		this.setRegistryName(name);
		this.setLightLevel(light);

		if (creativeTab) {
			// No need to change this for addons
			this.setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
		}

		ValkyrienSkiesMod.BLOCKS.add(this);
		ValkyrienSkiesMod.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
	}

	// No need to change this for addons
	@Override
	public void registerModels() {
		ValkyrienSkiesMod.proxy.registerItemRender(Item.getItemFromBlock(this), 0);
	}
}