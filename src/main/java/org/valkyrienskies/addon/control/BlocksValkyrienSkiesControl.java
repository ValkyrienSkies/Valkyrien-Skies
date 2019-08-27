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

package org.valkyrienskies.addon.control;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import org.valkyrienskies.addon.control.block.BlockDopedEthereum;
import org.valkyrienskies.addon.control.block.BlockGearbox;
import org.valkyrienskies.addon.control.block.BlockGyroscopeDampener;
import org.valkyrienskies.addon.control.block.BlockGyroscopeStabilizer;
import org.valkyrienskies.addon.control.block.BlockLiftControl;
import org.valkyrienskies.addon.control.block.BlockLiftValve;
import org.valkyrienskies.addon.control.block.BlockNetworkDisplay;
import org.valkyrienskies.addon.control.block.BlockRotationTrainAxle;
import org.valkyrienskies.addon.control.block.BlockShipHelm;
import org.valkyrienskies.addon.control.block.BlockShipPassengerChair;
import org.valkyrienskies.addon.control.block.BlockShipPilotsChair;
import org.valkyrienskies.addon.control.block.BlockShipTelegraph;
import org.valkyrienskies.addon.control.block.BlockShipWheel;
import org.valkyrienskies.addon.control.block.BlockTelegraphDummy;
import org.valkyrienskies.addon.control.block.BlockThrustRelay;
import org.valkyrienskies.addon.control.block.engine.BlockNormalEngine;
import org.valkyrienskies.addon.control.block.engine.BlockRedstoneEngine;
import org.valkyrienskies.addon.control.block.multiblocks.BlockEtherCompressorPart;
import org.valkyrienskies.addon.control.block.multiblocks.BlockEthereumEnginePart;
import org.valkyrienskies.addon.control.block.multiblocks.BlockGiantPropellerPart;
import org.valkyrienskies.addon.control.block.multiblocks.BlockRudderAxlePart;
import org.valkyrienskies.api.addons.Module;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.config.VWConfig;

public class BlocksValkyrienSkiesControl {

    public final BlockNormalEngine basicEngine;
    public final BlockNormalEngine advancedEngine;
    public final BlockNormalEngine eliteEngine;
    public final BlockNormalEngine ultimateEngine;
    public final BlockRedstoneEngine redstoneEngine;
    public final Block dopedEthereum;
    public final Block pilotsChair;
    public final Block passengerChair;
    public final Block shipHelm;
    public final Block shipWheel;
    public final Block shipTelegraph;
    public final Block shipTelegraphDummy;
    public final Block thrustRelay;
    public final Block gyroscopeStabilizer;
    public final Block liftValve;
    public final Block networkDisplay;
    public final Block liftControl;
    public final Block etherCompressorPanel;
    public final Block gyroscopeDampener;
    public final Block ethereumEnginePart;
    public final Block gearbox;
    public final Block rudderAxelPart;
    public final Block giantPropellerPart;
    public final Block rotationTrainAxle;

    public BlocksValkyrienSkiesControl() {

        basicEngine = (BlockNormalEngine) new BlockNormalEngine(Material.WOOD, VWConfig.ENGINE_POWER.basicEnginePower).setHardness(5f)
                .setTranslationKey("basicengine")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "basicengine")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        advancedEngine = (BlockNormalEngine) new BlockNormalEngine(Material.ROCK, VWConfig.ENGINE_POWER.advancedEnginePower).setHardness(6f)
                .setTranslationKey("advancedengine")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "advancedengine")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        eliteEngine = (BlockNormalEngine) new BlockNormalEngine(Material.IRON, VWConfig.ENGINE_POWER.eliteEnginePower).setHardness(8f)
                .setTranslationKey("eliteengine")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "eliteengine")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        ultimateEngine = (BlockNormalEngine) new BlockNormalEngine(Material.GROUND, VWConfig.ENGINE_POWER.ultimateEnginePower).setHardness(10f)
                .setTranslationKey("ultimateengine")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "ultimateengine")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        redstoneEngine = (BlockRedstoneEngine) new BlockRedstoneEngine(Material.REDSTONE_LIGHT, VWConfig.ENGINE_POWER.redstoneEnginePower).setHardness(7.0f)
                .setTranslationKey("redstoneengine")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "redstoneengine")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);

        dopedEthereum = new BlockDopedEthereum(Material.GLASS).setHardness(4f)
                .setTranslationKey("dopedethereum")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "dopedethereum")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        pilotsChair = new BlockShipPilotsChair(Material.IRON).setHardness(4f)
                .setTranslationKey("shippilotschair")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "shippilotschair")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);

        passengerChair = new BlockShipPassengerChair(Material.IRON).setHardness(4f)
                .setTranslationKey("shippassengerchair")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "shippassengerchair")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        shipHelm = new BlockShipHelm(Material.WOOD).setHardness(4f)
                .setTranslationKey("shiphelm")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "shiphelm")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        shipWheel = new BlockShipWheel(Material.WOOD).setHardness(5f)
                .setTranslationKey("shiphelmwheel")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "shiphelmwheel");
        shipTelegraph = new BlockShipTelegraph(Material.WOOD).setHardness(5f)
                .setTranslationKey("shiptelegraph")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "shiptelegraph")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);

        thrustRelay = new BlockThrustRelay(Material.IRON).setHardness(5f)
                .setTranslationKey("thrustrelay")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "thrustrelay")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);

        gyroscopeStabilizer = new BlockGyroscopeStabilizer(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_gyroscope_stabilizer")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "vw_gyroscope_stabilizer")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);

        liftValve = new BlockLiftValve(Material.IRON).setHardness(7f)
                .setTranslationKey("vw_liftvalve")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "vw_liftvalve")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        networkDisplay = new BlockNetworkDisplay(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_networkdisplay")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "vw_networkdisplay")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        liftControl = new BlockLiftControl(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_liftcontrol")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "vw_liftcontrol")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);

        etherCompressorPanel = new BlockEtherCompressorPart(Material.IRON).setHardness(6f)
                .setTranslationKey("vw_ethercompressorpanel")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "vw_ethercompressorpanel")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);

        gyroscopeDampener = new BlockGyroscopeDampener(Material.IRON).setHardness(6f)
                .setTranslationKey("vw_gyroscope_dampener")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "vw_gyroscope_dampener")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        ethereumEnginePart = new BlockEthereumEnginePart(Material.IRON).setHardness(6f)
                .setTranslationKey("vw_ethereum_enginepart")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "vw_ethereum_enginepart")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        gearbox = new BlockGearbox(Material.IRON).setHardness(6f)
                .setTranslationKey("vw_gearbox")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "vw_gearbox")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        shipTelegraphDummy = new BlockTelegraphDummy(Material.WOOD).setHardness(5f)
                .setTranslationKey("shiptelegraph_dummy")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "shiptelegraph_dummy");
        rudderAxelPart = new BlockRudderAxlePart(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_rudder_axle_part")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "vw_rudder_axle_part")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        giantPropellerPart = new BlockGiantPropellerPart(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_giant_propeller_part")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "vw_giant_propeller_part")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        rotationTrainAxle = new BlockRotationTrainAxle(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_block_rotation_train_axle")
                .setRegistryName(ValkyrienSkiesControl.MOD_ID, "vw_block_rotation_train_axle")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
    }

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(basicEngine);
        event.getRegistry().register(advancedEngine);
        event.getRegistry().register(eliteEngine);
        event.getRegistry().register(ultimateEngine);
        event.getRegistry().register(redstoneEngine);

        event.getRegistry().register(dopedEthereum);
        event.getRegistry().register(pilotsChair);
        event.getRegistry().register(passengerChair);

        event.getRegistry().register(shipHelm);
        event.getRegistry().register(shipWheel);
        event.getRegistry().register(shipTelegraph);
        event.getRegistry().register(thrustRelay);

        event.getRegistry().register(gyroscopeStabilizer);
        event.getRegistry().register(liftValve);
        event.getRegistry().register(networkDisplay);
        event.getRegistry().register(liftControl);

        event.getRegistry().register(etherCompressorPanel);
        event.getRegistry().register(gyroscopeDampener);
        event.getRegistry().register(ethereumEnginePart);
        event.getRegistry().register(gearbox);
        event.getRegistry().register(shipTelegraphDummy);
        event.getRegistry().register(rudderAxelPart);
        event.getRegistry().register(giantPropellerPart);
        event.getRegistry().register(rotationTrainAxle);
    }

    public void registerBlockItems(RegistryEvent.Register<Item> event) {
        registerItemBlock(event, basicEngine);
        registerItemBlock(event, advancedEngine);
        registerItemBlock(event, eliteEngine);
        registerItemBlock(event, ultimateEngine);
        registerItemBlock(event, redstoneEngine);

        registerItemBlock(event, dopedEthereum);
        registerItemBlock(event, pilotsChair);
        registerItemBlock(event, passengerChair);

        registerItemBlock(event, shipHelm);
        registerItemBlock(event, shipWheel);
        registerItemBlock(event, shipTelegraph);
        registerItemBlock(event, thrustRelay);

        registerItemBlock(event, gyroscopeStabilizer);
        registerItemBlock(event, liftValve);
        registerItemBlock(event, networkDisplay);
        registerItemBlock(event, liftControl);
        registerItemBlock(event, etherCompressorPanel);
        registerItemBlock(event, gyroscopeDampener);

        registerItemBlock(event, ethereumEnginePart);
        registerItemBlock(event, gearbox);
        registerItemBlock(event, rudderAxelPart);
        registerItemBlock(event, giantPropellerPart);
        registerItemBlock(event, rotationTrainAxle);
    }

    private void registerItemBlock(RegistryEvent.Register<Item> event, Block block) {
        Module.registerItemBlock(event, block);
    }
}
