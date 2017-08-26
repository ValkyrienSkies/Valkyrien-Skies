package ValkyrienWarfareControl.Proxy;

import ValkyrienWarfareControl.Client.Renderer.BasicNodeTileEntityRenderer;
import ValkyrienWarfareControl.Client.Renderer.PropellerEngineTileEntityRenderer;
import ValkyrienWarfareControl.Client.Renderer.ShipHelmTileEntityRenderer;
import ValkyrienWarfareControl.Client.Renderer.ShipTelegraphTileEntityRenderer;
import ValkyrienWarfareControl.ControlSystems.ControlGUI.ThrustModulatorGui;
import ValkyrienWarfareControl.TileEntity.*;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
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
import net.minecraftforge.fml.common.event.FMLStateEvent;

public class ClientProxyControl extends CommonProxyControl {

	private static void registerBlockItem(Block toRegister) {
		Item item = Item.getItemFromBlock(toRegister);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(ValkyrienWarfareControlMod.INSTANCE.getModID() + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	private static void registerItemModel(Item toRegister) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(toRegister, 0, new ModelResourceLocation(ValkyrienWarfareControlMod.INSTANCE.getModID() + ":" + toRegister.getUnlocalizedName().substring(5), "inventory"));
	}

	private static void registerBlockItemModels() {
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.basicEngine);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.advancedEngine);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.eliteEngine);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.ultimateEngine);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.redstoneEngine);

		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.basicHoverController);

		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.antigravityEngine);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.advancedEtherCompressor);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.eliteEtherCompressor);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.ultimateEtherCompressor);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.creativeEtherCompressor);

		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.pilotsChair);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.passengerChair);

		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.shipHelm);
//		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.shipWheel);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.shipTelegraph);

		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.dopedEtherium);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.balloonBurner);

		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.thrustRelay);
		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.thrustModulator);

		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.shipHullSealer);

		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.airshipController_zepplin);
	}

	private static void registerItemModels() {
		registerItemModel(ValkyrienWarfareControlMod.INSTANCE.systemLinker);
		registerItemModel(ValkyrienWarfareControlMod.INSTANCE.airshipStealer);
		registerItemModel(ValkyrienWarfareControlMod.INSTANCE.relayWire);
	}

	private static void registerTileEntityRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(ThrustRelayTileEntity.class, new BasicNodeTileEntityRenderer(ThrustRelayTileEntity.class));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShipHelm.class, new ShipHelmTileEntityRenderer(TileEntityShipHelm.class));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShipTelegraph.class, new ShipTelegraphTileEntityRenderer(TileEntityShipTelegraph.class));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPropellerEngine.class, new PropellerEngineTileEntityRenderer());
	}

	public static void checkForTextFieldUpdate(ThrustModulatorTileEntity entity) {
		if (Minecraft.getMinecraft().currentScreen instanceof ThrustModulatorGui) {
			ThrustModulatorGui gui = (ThrustModulatorGui) Minecraft.getMinecraft().currentScreen;
			gui.updateTextFields();
		}
	}

	@Override
	public void preInit(FMLStateEvent event) {
		OBJLoader.INSTANCE.addDomain(ValkyrienWarfareControlMod.INSTANCE.getModID().toLowerCase());
	}

	@Override
	public void init(FMLStateEvent event) {

//		Item item = Item.getItemFromBlock(ValkyrienWarfareControlMod.INSTANCE.pilotsChair);
//        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(ValkyrienWarfareControlMod.MODID.toLowerCase() + ":" + ValkyrienWarfareControlMod.INSTANCE.pilotsChair.unlocalizedName, "inventory"));
		// Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
		// .register(ValkyrienWarfareControlMod.INSTANCE.systemLinker, 0, new ModelResourceLocation(ValkyrienWarfareMod.MODID+":systemLinker", "inventory"));
	}

	@Override
	public void postInit(FMLStateEvent event) {
		registerBlockItemModels();
		registerItemModels();
		registerTileEntityRenderers();
	}

}
