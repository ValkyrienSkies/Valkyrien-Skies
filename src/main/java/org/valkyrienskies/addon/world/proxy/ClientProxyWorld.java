package org.valkyrienskies.addon.world.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderFallingBlock;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import org.valkyrienskies.addon.world.EntityFallingUpBlock;
import org.valkyrienskies.addon.world.ValkyrienSkiesWorld;

public class ClientProxyWorld extends CommonProxyWorld {

    @Override
    public void preInit(FMLStateEvent e) {
        super.preInit(e);
        RenderingRegistry
            .registerEntityRenderingHandler(EntityFallingUpBlock.class, RenderFallingBlock::new);
    }

    @Override
    public void init(FMLStateEvent e) {
        super.init(e);
        registerBlockItem(ValkyrienSkiesWorld.INSTANCE.valkyriumOre);
    }

    @Override
    public void postInit(FMLStateEvent e) {
        super.postInit(e);
        registerItemModel(ValkyrienSkiesWorld.INSTANCE.valkyriumCrystal);
    }

    private void registerBlockItem(Block toRegister) {
        Item item = Item.getItemFromBlock(toRegister);
        Minecraft.getMinecraft()
            .getRenderItem()
            .getItemModelMesher()
            .register(item, 0, new ModelResourceLocation(
                ValkyrienSkiesWorld.MOD_ID + ":" + item.getTranslationKey()
                    .substring(5), "inventory"));
    }

    private void registerItemModel(Item toRegister) {
        RenderItem renderItem = Minecraft.getMinecraft()
            .getRenderItem();
        renderItem.getItemModelMesher()
            .register(toRegister, 0, new ModelResourceLocation(
                ValkyrienSkiesWorld.MOD_ID + ":" + toRegister.getTranslationKey()
                    .substring(5), "inventory"));
    }

}
