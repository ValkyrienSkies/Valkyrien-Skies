package org.valkyrienskies.addon.world.util;

import org.valkyrienskies.addon.world.ValkyrienSkiesWorld;
import org.valkyrienskies.mod.client.BaseModel;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

import net.minecraft.item.Item;

// Addons need to provide their own copies of this class.
public class BaseItem extends Item implements BaseModel {
    public BaseItem(String name, boolean creativeTab) {
        //this.setUnlocalizedName(name);
        this.setRegistryName(name);

        if (creativeTab) {
            this.setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        }

        ValkyrienSkiesWorld.ITEMS.add(this);
    }

    // No need to change this for addons
    @Override
    public void registerModels() {
        ValkyrienSkiesMod.proxy.registerItemRender(this, 0);
    }
}
