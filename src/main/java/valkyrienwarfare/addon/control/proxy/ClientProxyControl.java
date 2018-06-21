/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.addon.control.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.controlsystems.controlgui.ThrustModulatorGui;
import valkyrienwarfare.addon.control.renderer.BasicNodeTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.PropellerEngineTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.ShipHelmTileEntityRenderer;
import valkyrienwarfare.addon.control.renderer.ShipTelegraphTileEntityRenderer;
import valkyrienwarfare.addon.control.tileentity.TileEntityNodeRelay;
import valkyrienwarfare.addon.control.tileentity.TileEntityPropellerEngine;
import valkyrienwarfare.addon.control.tileentity.TileEntityShipHelm;
import valkyrienwarfare.addon.control.tileentity.TileEntityShipTelegraph;
import valkyrienwarfare.addon.control.tileentity.TileEntityThrustModulator;

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
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.basicEngine);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.advancedEngine);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.eliteEngine);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.ultimateEngine);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.redstoneEngine);

        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.antigravityEngine);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.advancedEtherCompressor);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.eliteEtherCompressor);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.ultimateEtherCompressor);

        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.creativeEtherCompressor);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.pilotsChair);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.passengerChair);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.shipHelm);

        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.shipTelegraph);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.dopedEtherium);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.thrustRelay);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.thrustModulator);
        
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.gyroscope);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.liftValve);
        registerBlockItem(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.networkDisplay);
    }

    private static void registerItemModels() {
        registerItemModel(ValkyrienWarfareControl.INSTANCE.relayWire);
    }

    private static void registerTileEntityRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNodeRelay.class, new BasicNodeTileEntityRenderer(TileEntityNodeRelay.class));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShipHelm.class, new ShipHelmTileEntityRenderer(TileEntityShipHelm.class));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShipTelegraph.class, new ShipTelegraphTileEntityRenderer(TileEntityShipTelegraph.class));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPropellerEngine.class, new PropellerEngineTileEntityRenderer());
    }

    public static void checkForTextFieldUpdate(TileEntityThrustModulator entity) {
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
    }

    @Override
    public void postInit(FMLStateEvent event) {
        registerBlockItemModels();
        registerItemModels();
        registerTileEntityRenderers();
    }

}
