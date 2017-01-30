package ValkyrienWarfareControl.Proxy;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxyControl extends CommonProxyControl {

	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		OBJLoader.INSTANCE.addDomain(ValkyrienWarfareControlMod.MODID.toLowerCase());
	}

	public void init(FMLInitializationEvent event) {
		super.init(event);
		// Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
		// .register(ValkyrienWarfareControlMod.instance.systemLinker, 0, new ModelResourceLocation(ValkyrienWarfareMod.MODID+":systemLinker", "inventory"));
	}

	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		registerBlockItem(ValkyrienWarfareControlMod.instance.basicEngine);
		registerBlockItem(ValkyrienWarfareControlMod.instance.advancedEngine);
		registerBlockItem(ValkyrienWarfareControlMod.instance.eliteEngine);
		registerBlockItem(ValkyrienWarfareControlMod.instance.redstoneEngine);
		registerBlockItem(ValkyrienWarfareControlMod.instance.ultimateEngine);

		registerBlockItem(ValkyrienWarfareControlMod.instance.basicHoverController);
		registerBlockItem(ValkyrienWarfareControlMod.instance.antigravityEngine);
		
		registerBlockItem(ValkyrienWarfareControlMod.instance.pilotsChair);

		registerItemModel(ValkyrienWarfareControlMod.instance.systemLinker);
		registerItemModel(ValkyrienWarfareControlMod.instance.airshipStealer);
	}

	private void registerBlockItem(Block toRegister) {
		Item item = Item.getItemFromBlock(toRegister);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(ValkyrienWarfareMod.MODID + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	private void registerItemModel(Item toRegister) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(toRegister, 0, new ModelResourceLocation(ValkyrienWarfareControlMod.MODID + ":" + toRegister.getUnlocalizedName().substring(5), "inventory"));
	}
}
