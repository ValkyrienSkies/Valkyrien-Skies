package org.valkyrienskies.addon.control.util;

import net.minecraft.item.Item;

import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.mod.client.BaseModel;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

// Addons need to provide their own copies of this class.
public class BaseItem extends Item implements BaseModel {
    public BaseItem(String name, boolean creativeTab) {
        this.setTranslationKey(name);
        this.setRegistryName(name);

        if (creativeTab) {
            this.setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        }

        ValkyrienSkiesControl.ITEMS.add(this);
    }

    // No need to change this for addons
    @Override
    public void registerModels() {
        ValkyrienSkiesMod.proxy.registerItemRender(this, 0);
    }
}
