package org.valkyrienskies.addon.opencomputers.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import org.valkyrienskies.addon.opencomputers.ValkyrienSkiesOpenComputers;
import org.valkyrienskies.mod.client.BaseModel;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

public class BaseBlock extends Block implements BaseModel {
    public BaseBlock(String name, Material mat, float light, boolean creativeTab) {
        super(mat);
        this.setTranslationKey(name);
        this.setRegistryName(name);
        this.setLightLevel(light);

        if (creativeTab) {
            this.setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        }

        ValkyrienSkiesOpenComputers.BLOCKS.add(this);
        ValkyrienSkiesOpenComputers.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
    }

    @Override
    public void registerModels() {
        ValkyrienSkiesMod.proxy.registerItemRender(Item.getItemFromBlock(this), 0);
    }
}