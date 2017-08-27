package valkyrienwarfare.addon.world.proxy;

import valkyrienwarfare.addon.world.EntityFallingUpBlock;
import valkyrienwarfare.addon.world.render.EntityFallingUpBlockRenderFactory;
import valkyrienwarfare.addon.world.ValkyrienWarfareWorld;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLStateEvent;

public class ClientProxyWorld extends CommonProxyWorld {

	@Override
	public void preInit(FMLStateEvent e) {
		RenderingRegistry.registerEntityRenderingHandler(EntityFallingUpBlock.class, new EntityFallingUpBlockRenderFactory());
	}

	@Override
	public void init(FMLStateEvent e) {
		registerBlockItem(ValkyrienWarfareWorld.INSTANCE.etheriumOre);
		registerBlockItem(ValkyrienWarfareWorld.INSTANCE.skydungeon_controller);
	}

	@Override
	public void postInit(FMLStateEvent e) {
		registerItemModel(ValkyrienWarfareWorld.INSTANCE.etheriumCrystal);
	}

	private void registerBlockItem(Block toRegister) {
		Item item = Item.getItemFromBlock(toRegister);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(ValkyrienWarfareWorld.INSTANCE.getModID() + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	private void registerItemModel(Item toRegister) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(toRegister, 0, new ModelResourceLocation(ValkyrienWarfareWorld.INSTANCE.getModID() + ":" + toRegister.getUnlocalizedName().substring(5), "inventory"));
	}
}