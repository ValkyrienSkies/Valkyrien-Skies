package ValkyrienWarfareWorld.Proxy;

import ValkyrienWarfareWorld.EntityFallingUpBlock;
import ValkyrienWarfareWorld.ValkyrienWarfareWorldMod;
import ValkyrienWarfareWorld.Render.EntityFallingUpBlockRenderFactory;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxyWorld extends CommonProxyWorld {

	@Override
	public void preInit(FMLPreInitializationEvent e) {
		RenderingRegistry.registerEntityRenderingHandler(EntityFallingUpBlock.class, new EntityFallingUpBlockRenderFactory());
	}

	@Override
	public void init(FMLInitializationEvent e) {
		registerBlockItem(ValkyrienWarfareWorldMod.etheriumOre);
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent e) {
		registerItemModel(ValkyrienWarfareWorldMod.etheriumCrystal);
	}

	private void registerBlockItem(Block toRegister) {
		Item item = Item.getItemFromBlock(toRegister);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(ValkyrienWarfareWorldMod.MODID + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	private void registerItemModel(Item toRegister) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(toRegister, 0, new ModelResourceLocation(ValkyrienWarfareWorldMod.MODID + ":" + toRegister.getUnlocalizedName().substring(5), "inventory"));
	}

}