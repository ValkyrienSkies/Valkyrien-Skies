package ValkyrienWarfareControl.Proxy;

import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.Client.Renderer.BasicNodeTileEntityRenderer;
import ValkyrienWarfareControl.Client.Renderer.ShipHelmTileEntityRenderer;
import ValkyrienWarfareControl.Client.Renderer.ShipTelegraphTileEntityRenderer;
import ValkyrienWarfareControl.TileEntity.ThrustRelayTileEntity;
import ValkyrienWarfareControl.TileEntity.TileEntityShipHelm;
import ValkyrienWarfareControl.TileEntity.TileEntityShipTelegraph;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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

//		Item item = Item.getItemFromBlock(ValkyrienWarfareControlMod.instance.pilotsChair);
//        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(ValkyrienWarfareControlMod.MODID.toLowerCase() + ":" + ValkyrienWarfareControlMod.instance.pilotsChair.unlocalizedName, "inventory"));
		// Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
		// .register(ValkyrienWarfareControlMod.instance.systemLinker, 0, new ModelResourceLocation(ValkyrienWarfareMod.MODID+":systemLinker", "inventory"));
	}

	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		registerBlockItemModels();
		registerItemModels();
		registerTileEntityRenderers();
	}

	private static void registerBlockItem(Block toRegister) {
		Item item = Item.getItemFromBlock(toRegister);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(ValkyrienWarfareControlMod.MODID + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	private static void registerItemModel(Item toRegister) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(toRegister, 0, new ModelResourceLocation(ValkyrienWarfareControlMod.MODID + ":" + toRegister.getUnlocalizedName().substring(5), "inventory"));
	}

	private static void registerBlockItemModels() {
		registerBlockItem(ValkyrienWarfareControlMod.instance.basicEngine);
		registerBlockItem(ValkyrienWarfareControlMod.instance.advancedEngine);
		registerBlockItem(ValkyrienWarfareControlMod.instance.eliteEngine);
		registerBlockItem(ValkyrienWarfareControlMod.instance.ultimateEngine);
		registerBlockItem(ValkyrienWarfareControlMod.instance.redstoneEngine);

		registerBlockItem(ValkyrienWarfareControlMod.instance.basicHoverController);

		registerBlockItem(ValkyrienWarfareControlMod.instance.antigravityEngine);
		registerBlockItem(ValkyrienWarfareControlMod.instance.advancedEtherCompressor);
		registerBlockItem(ValkyrienWarfareControlMod.instance.eliteEtherCompressor);
		registerBlockItem(ValkyrienWarfareControlMod.instance.ultimateEtherCompressor);
		registerBlockItem(ValkyrienWarfareControlMod.instance.creativeEtherCompressor);

		registerBlockItem(ValkyrienWarfareControlMod.instance.pilotsChair);
		registerBlockItem(ValkyrienWarfareControlMod.instance.passengerChair);

		registerBlockItem(ValkyrienWarfareControlMod.instance.shipHelm);
		registerBlockItem(ValkyrienWarfareControlMod.instance.shipWheel);
		registerBlockItem(ValkyrienWarfareControlMod.instance.shipTelegraph);

		registerBlockItem(ValkyrienWarfareControlMod.instance.dopedEtherium);
		registerBlockItem(ValkyrienWarfareControlMod.instance.balloonBurner);

		registerBlockItem(ValkyrienWarfareControlMod.instance.thrustRelay);
		registerBlockItem(ValkyrienWarfareControlMod.instance.thrustModulator);
	}

	private static void registerItemModels() {
		registerItemModel(ValkyrienWarfareControlMod.instance.systemLinker);
		registerItemModel(ValkyrienWarfareControlMod.instance.airshipStealer);
		registerItemModel(ValkyrienWarfareControlMod.instance.relayWire);
	}

	private static void registerTileEntityRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(ThrustRelayTileEntity.class, new BasicNodeTileEntityRenderer(ThrustRelayTileEntity.class));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShipHelm.class, new ShipHelmTileEntityRenderer(TileEntityShipHelm.class));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShipTelegraph.class, new ShipTelegraphTileEntityRenderer(TileEntityShipTelegraph.class));
	}

}
