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

package valkyrienwarfare.addon.control;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import valkyrienwarfare.addon.control.block.BlockDopedEthereum;
import valkyrienwarfare.addon.control.block.BlockGearbox;
import valkyrienwarfare.addon.control.block.BlockGyroscopeDampener;
import valkyrienwarfare.addon.control.block.BlockGyroscopeStabilizer;
import valkyrienwarfare.addon.control.block.BlockLiftControl;
import valkyrienwarfare.addon.control.block.BlockLiftValve;
import valkyrienwarfare.addon.control.block.BlockNetworkDisplay;
import valkyrienwarfare.addon.control.block.BlockRotationTrainAxle;
import valkyrienwarfare.addon.control.block.BlockShipHelm;
import valkyrienwarfare.addon.control.block.BlockShipPassengerChair;
import valkyrienwarfare.addon.control.block.BlockShipTelegraph;
import valkyrienwarfare.addon.control.block.BlockShipWheel;
import valkyrienwarfare.addon.control.block.BlockTelegraphDummy;
import valkyrienwarfare.addon.control.block.BlockThrustModulator;
import valkyrienwarfare.addon.control.block.BlockThrustRelay;
import valkyrienwarfare.addon.control.block.engine.BlockNormalEngine;
import valkyrienwarfare.addon.control.block.engine.BlockRedstoneEngine;
import valkyrienwarfare.addon.control.block.multiblocks.BlockEtherCompressorPart;
import valkyrienwarfare.addon.control.block.multiblocks.BlockEthereumEnginePart;
import valkyrienwarfare.addon.control.block.multiblocks.BlockGiantPropellerPart;
import valkyrienwarfare.addon.control.block.multiblocks.BlockRudderAxlePart;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;

public class BlocksValkyrienWarfareControl {

    public final BlockNormalEngine basicEngine;
    public final BlockNormalEngine advancedEngine;
    public final BlockNormalEngine eliteEngine;
    public final BlockNormalEngine ultimateEngine;
    public final BlockRedstoneEngine redstoneEngine;
    public final Block dopedEthereum;
    public final Block passengerChair;
    public final Block shipHelm;
    public final Block shipWheel;
    public final Block shipTelegraph;
    public final Block shipTelegraphDummy;
    public final Block thrustRelay;
    public final Block thrustModulator;
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
    private final ValkyrienWarfareControl mod_vwcontrol;

    public BlocksValkyrienWarfareControl(ValkyrienWarfareControl mod_vwcontrol) {
        this.mod_vwcontrol = mod_vwcontrol;

        basicEngine = (BlockNormalEngine) new BlockNormalEngine(Material.WOOD, 2000).setHardness(5f)
                .setTranslationKey("basicengine")
                .setRegistryName(getModID(), "basicengine")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        advancedEngine = (BlockNormalEngine) new BlockNormalEngine(Material.ROCK, 2500).setHardness(6f)
                .setTranslationKey("advancedengine")
                .setRegistryName(getModID(), "advancedengine")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        eliteEngine = (BlockNormalEngine) new BlockNormalEngine(Material.IRON, 5000).setHardness(8f)
                .setTranslationKey("eliteengine")
                .setRegistryName(getModID(), "eliteengine")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        ultimateEngine = (BlockNormalEngine) new BlockNormalEngine(Material.GROUND, 10000).setHardness(10f)
                .setTranslationKey("ultimateengine")
                .setRegistryName(getModID(), "ultimateengine")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        redstoneEngine = (BlockRedstoneEngine) new BlockRedstoneEngine(Material.REDSTONE_LIGHT, 500).setHardness(7.0f)
                .setTranslationKey("redstoneengine")
                .setRegistryName(getModID(), "redstoneengine")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);

        dopedEthereum = new BlockDopedEthereum(Material.GLASS).setHardness(4f)
                .setTranslationKey("dopedethereum")
                .setRegistryName(getModID(), "dopedethereum")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);

        passengerChair = new BlockShipPassengerChair(Material.IRON).setHardness(4f)
                .setTranslationKey("shippassengerchair")
                .setRegistryName(getModID(), "shippassengerchair")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        shipHelm = new BlockShipHelm(Material.WOOD).setHardness(4f)
                .setTranslationKey("shiphelm")
                .setRegistryName(getModID(), "shiphelm")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        shipWheel = new BlockShipWheel(Material.WOOD).setHardness(5f)
                .setTranslationKey("shiphelmwheel")
                .setRegistryName(getModID(), "shiphelmwheel");
        shipTelegraph = new BlockShipTelegraph(Material.WOOD).setHardness(5f)
                .setTranslationKey("shiptelegraph")
                .setRegistryName(getModID(), "shiptelegraph")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);

        thrustRelay = new BlockThrustRelay(Material.IRON).setHardness(5f)
                .setTranslationKey("thrustrelay")
                .setRegistryName(getModID(), "thrustrelay")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        thrustModulator = new BlockThrustModulator(Material.IRON).setHardness(8f)
                .setTranslationKey("thrustmodulator")
                .setRegistryName(getModID(), "thrustmodulator")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);

        gyroscopeStabilizer = new BlockGyroscopeStabilizer(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_gyroscope_stabilizer")
                .setRegistryName(getModID(), "vw_gyroscope_stabilizer")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);

        liftValve = new BlockLiftValve(Material.IRON).setHardness(7f)
                .setTranslationKey("vw_liftvalve")
                .setRegistryName(getModID(), "vw_liftvalve")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        networkDisplay = new BlockNetworkDisplay(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_networkdisplay")
                .setRegistryName(getModID(), "vw_networkdisplay")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        liftControl = new BlockLiftControl(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_liftcontrol")
                .setRegistryName(getModID(), "vw_liftcontrol")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);

        etherCompressorPanel = new BlockEtherCompressorPart(Material.IRON).setHardness(6f)
                .setTranslationKey("vw_ethercompressorpanel")
                .setRegistryName(getModID(), "vw_ethercompressorpanel")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);

        gyroscopeDampener = new BlockGyroscopeDampener(Material.IRON).setHardness(6f)
                .setTranslationKey("vw_gyroscope_dampener")
                .setRegistryName(getModID(), "vw_gyroscope_dampener")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        ethereumEnginePart = new BlockEthereumEnginePart(Material.IRON).setHardness(6f)
                .setTranslationKey("vw_ethereum_enginepart")
                .setRegistryName(getModID(), "vw_ethereum_enginepart")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        gearbox = new BlockGearbox(Material.IRON).setHardness(6f)
                .setTranslationKey("vw_gearbox")
                .setRegistryName(getModID(), "vw_gearbox")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        shipTelegraphDummy = new BlockTelegraphDummy(Material.WOOD).setHardness(5f)
                .setTranslationKey("shiptelegraph_dummy")
                .setRegistryName(getModID(), "shiptelegraph_dummy");
        rudderAxelPart = new BlockRudderAxlePart(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_rudder_axle_part")
                .setRegistryName(getModID(), "vw_rudder_axle_part")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        giantPropellerPart = new BlockGiantPropellerPart(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_giant_propeller_part")
                .setRegistryName(getModID(), "vw_giant_propeller_part")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        rotationTrainAxle = new BlockRotationTrainAxle(Material.IRON).setHardness(5f)
                .setTranslationKey("vw_block_rotation_train_axle")
                .setRegistryName(getModID(), "vw_block_rotation_train_axle")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
    }

    protected void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(basicEngine);
        event.getRegistry().register(advancedEngine);
        event.getRegistry().register(eliteEngine);
        event.getRegistry().register(ultimateEngine);
        event.getRegistry().register(redstoneEngine);

        event.getRegistry().register(dopedEthereum);
        event.getRegistry().register(passengerChair);

        event.getRegistry().register(shipHelm);
        event.getRegistry().register(shipWheel);
        event.getRegistry().register(shipTelegraph);
        event.getRegistry().register(thrustRelay);
        event.getRegistry().register(thrustModulator);

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

    protected void registerBlockItems(RegistryEvent.Register<Item> event) {
        registerItemBlock(event, basicEngine);
        registerItemBlock(event, advancedEngine);
        registerItemBlock(event, eliteEngine);
        registerItemBlock(event, ultimateEngine);
        registerItemBlock(event, redstoneEngine);

        registerItemBlock(event, dopedEthereum);
        registerItemBlock(event, passengerChair);

        registerItemBlock(event, shipHelm);
        registerItemBlock(event, shipWheel);
        registerItemBlock(event, shipTelegraph);
        registerItemBlock(event, thrustRelay);
        registerItemBlock(event, thrustModulator);

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

    private String getModID() {
        return mod_vwcontrol.getModID();
    }
}
