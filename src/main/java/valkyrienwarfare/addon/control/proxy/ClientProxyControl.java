package valkyrienwarfare.addon.control.proxy;

import valkyrienwarfare.addon.control.renderer.BasicNodeTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.PropellerEngineTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.ShipHelmTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.ShipTelegraphTileEntityRenderer;
import valkyrienwarfare.addon.control.controlsystems.controlgui.ThrustModulatorGui;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import valkyrienwarfare.addon.control.tileentity.*;

public class ClientProxyControl extends CommonProxyControl {

	private static void registerBlockItem(Block toRegister) {
		Item item = Item.getItemFromBlock(toRegister);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(ValkyrienWarfareControl.INSTANCE.getModID() + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	private static void registerItemModel(Item toRegister) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(toRegister, 0, new ModelResourceLocation(ValkyrienWarfareControl.INSTANCE.getModID() + ":" + toRegister.getUnlocalizedName().substring(5), "inventory"));
	}

	private static void registerBlockItemModels() {
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.basicEngine);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.advancedEngine);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.eliteEngine);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.ultimateEngine);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.redstoneEngine);

		registerBlockItem(ValkyrienWarfareControl.INSTANCE.basicHoverController);

		registerBlockItem(ValkyrienWarfareControl.INSTANCE.antigravityEngine);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.advancedEtherCompressor);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.eliteEtherCompressor);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.ultimateEtherCompressor);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.creativeEtherCompressor);

		registerBlockItem(ValkyrienWarfareControl.INSTANCE.pilotsChair);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.passengerChair);

		registerBlockItem(ValkyrienWarfareControl.INSTANCE.shipHelm);
//		registerBlockItem(ValkyrienWarfareControlMod.INSTANCE.shipWheel);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.shipTelegraph);

		registerBlockItem(ValkyrienWarfareControl.INSTANCE.dopedEtherium);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.balloonBurner);

		registerBlockItem(ValkyrienWarfareControl.INSTANCE.thrustRelay);
		registerBlockItem(ValkyrienWarfareControl.INSTANCE.thrustModulator);

		registerBlockItem(ValkyrienWarfareControl.INSTANCE.shipHullSealer);

		registerBlockItem(ValkyrienWarfareControl.INSTANCE.airshipController_zepplin);
	}

	private static void registerItemModels() {
		registerItemModel(ValkyrienWarfareControl.INSTANCE.systemLinker);
		registerItemModel(ValkyrienWarfareControl.INSTANCE.airshipStealer);
		registerItemModel(ValkyrienWarfareControl.INSTANCE.relayWire);
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
		OBJLoader.INSTANCE.addDomain(ValkyrienWarfareControl.INSTANCE.getModID().toLowerCase());
	}

	@Override
	public void init(FMLStateEvent event) {

//		item item = item.getItemFromBlock(ValkyrienWarfareControlMod.INSTANCE.pilotsChair);
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
