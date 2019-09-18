/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.addon.control;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import org.valkyrienskies.addon.control.block.BlockCaptainsChair;
import org.valkyrienskies.addon.control.block.BlockCompactedValkyrium;
import org.valkyrienskies.addon.control.block.BlockDummyTelegraph;
import org.valkyrienskies.addon.control.block.BlockGearbox;
import org.valkyrienskies.addon.control.block.BlockGyroscopeDampener;
import org.valkyrienskies.addon.control.block.BlockGyroscopeStabilizer;
import org.valkyrienskies.addon.control.block.BlockLiftLever;
import org.valkyrienskies.addon.control.block.BlockLiftValve;
import org.valkyrienskies.addon.control.block.BlockNetworkDisplay;
import org.valkyrienskies.addon.control.block.BlockNetworkRelay;
import org.valkyrienskies.addon.control.block.BlockPassengerChair;
import org.valkyrienskies.addon.control.block.BlockRotationAxle;
import org.valkyrienskies.addon.control.block.BlockShipHelm;
import org.valkyrienskies.addon.control.block.BlockShipWheel;
import org.valkyrienskies.addon.control.block.BlockSpeedTelegraph;
import org.valkyrienskies.addon.control.block.engine.BlockNormalEngine;
import org.valkyrienskies.addon.control.block.engine.BlockRedstoneEngine;
import org.valkyrienskies.addon.control.block.multiblocks.BlockGiantPropellerPart;
import org.valkyrienskies.addon.control.block.multiblocks.BlockRudderPart;
import org.valkyrienskies.addon.control.block.multiblocks.BlockValkyriumCompressorPart;
import org.valkyrienskies.addon.control.block.multiblocks.BlockValkyriumEnginePart;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienwarfare.api.addons.Module;

public class BlocksValkyrienSkiesControl {

    public final BlockNormalEngine basicEngine;
    public final BlockNormalEngine advancedEngine;
    public final BlockNormalEngine eliteEngine;
    public final BlockNormalEngine ultimateEngine;
    public final BlockRedstoneEngine redstoneEngine;
    public final Block compactedValkyrium;
    public final Block captainsChair;
    public final Block passengerChair;
    public final Block shipHelm;
    public final Block shipWheel;
    public final Block speedTelegraph;
    public final Block dummyTelegraph;
    public final Block networkRelay;
    public final Block gyroscopeStabilizer;
    public final Block liftValve;
    public final Block networkDisplay;
    public final Block liftLever;
    public final Block valkyriumCompressorPart;
    public final Block gyroscopeDampener;
    public final Block valkyriumEnginePart;
    public final Block gearbox;
    public final Block rudderPart;
    public final Block giantPropellerPart;
    public final Block rotationAxle;

    public BlocksValkyrienSkiesControl() {

        basicEngine = (BlockNormalEngine) new BlockNormalEngine(Material.WOOD,
            VSConfig.ENGINE_POWER.basicEnginePower).setHardness(5f)
            .setTranslationKey("basic_engine")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "basic_engine")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        advancedEngine = (BlockNormalEngine) new BlockNormalEngine(Material.ROCK,
            VSConfig.ENGINE_POWER.advancedEnginePower).setHardness(6f)
            .setTranslationKey("advanced_engine")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "advanced_engine")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        eliteEngine = (BlockNormalEngine) new BlockNormalEngine(Material.IRON,
            VSConfig.ENGINE_POWER.eliteEnginePower).setHardness(8f)
            .setTranslationKey("elite_engine")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "elite_engine")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        ultimateEngine = (BlockNormalEngine) new BlockNormalEngine(Material.GROUND,
            VSConfig.ENGINE_POWER.ultimateEnginePower).setHardness(10f)
            .setTranslationKey("ultimate_engine")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "ultimate_engine")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        redstoneEngine = (BlockRedstoneEngine) new BlockRedstoneEngine(Material.REDSTONE_LIGHT,
            VSConfig.ENGINE_POWER.redstoneEnginePower).setHardness(7.0f)
            .setTranslationKey("redstone_engine")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "redstone_engine")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);

        compactedValkyrium = new BlockCompactedValkyrium(Material.GLASS).setHardness(4f)
            .setTranslationKey("compacted_valkyrium")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "compacted_valkyrium")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        captainsChair = new BlockCaptainsChair(Material.IRON).setHardness(4f)
            .setTranslationKey("captains_chair")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "captains_chair")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);

        passengerChair = new BlockPassengerChair(Material.IRON).setHardness(4f)
            .setTranslationKey("passenger_chair")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "passenger_chair")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        shipHelm = new BlockShipHelm(Material.WOOD).setHardness(4f)
            .setTranslationKey("ship_helm")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "ship_helm")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        shipWheel = new BlockShipWheel(Material.WOOD).setHardness(5f)
            .setTranslationKey("ship_helm_wheel")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "ship_helm_wheel");
        speedTelegraph = new BlockSpeedTelegraph(Material.WOOD).setHardness(5f)
            .setTranslationKey("speed_telegraph")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "speed_telegraph")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);

        networkRelay = new BlockNetworkRelay(Material.IRON).setHardness(5f)
            .setTranslationKey("network_relay")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "network_relay")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);

        gyroscopeStabilizer = new BlockGyroscopeStabilizer(Material.IRON).setHardness(5f)
            .setTranslationKey("gyroscope_stabilizer")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "gyroscope_stabilizer")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);

        liftValve = new BlockLiftValve(Material.IRON).setHardness(7f)
            .setTranslationKey("lift_valve")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "lift_valve")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        networkDisplay = new BlockNetworkDisplay(Material.IRON).setHardness(5f)
            .setTranslationKey("network_display")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "network_display")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        liftLever = new BlockLiftLever(Material.IRON).setHardness(5f)
            .setTranslationKey("lift_lever")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "lift_lever")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);

        valkyriumCompressorPart = new BlockValkyriumCompressorPart(Material.IRON).setHardness(6f)
            .setTranslationKey("valkyrium_compressor_part")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "valkyrium_compressor_part")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);

        gyroscopeDampener = new BlockGyroscopeDampener(Material.IRON).setHardness(6f)
            .setTranslationKey("gyroscope_dampener")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "gyroscope_dampener")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        valkyriumEnginePart = new BlockValkyriumEnginePart(Material.IRON).setHardness(6f)
            .setTranslationKey("valkyrium_engine_part")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "valkyrium_engine_part")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        gearbox = new BlockGearbox(Material.IRON).setHardness(6f)
            .setTranslationKey("gearbox")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "gearbox")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        dummyTelegraph = new BlockDummyTelegraph(Material.WOOD).setHardness(5f)
            .setTranslationKey("dummy_telegraph")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "dummy_telegraph");
        rudderPart = new BlockRudderPart(Material.IRON).setHardness(5f)
            .setTranslationKey("rudder_axle_part")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "rudder_axle_part")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        giantPropellerPart = new BlockGiantPropellerPart(Material.IRON).setHardness(5f)
            .setTranslationKey("giant_propeller_part")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "giant_propeller_part")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        rotationAxle = new BlockRotationAxle(Material.IRON).setHardness(5f)
            .setTranslationKey("rotation_axle")
            .setRegistryName(ValkyrienSkiesControl.MOD_ID, "rotation_axle")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
    }

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(basicEngine);
        event.getRegistry().register(advancedEngine);
        event.getRegistry().register(eliteEngine);
        event.getRegistry().register(ultimateEngine);
        event.getRegistry().register(redstoneEngine);

        event.getRegistry().register(compactedValkyrium);
        event.getRegistry().register(captainsChair);
        event.getRegistry().register(passengerChair);

        event.getRegistry().register(shipHelm);
        event.getRegistry().register(shipWheel);
        event.getRegistry().register(speedTelegraph);
        event.getRegistry().register(dummyTelegraph);
        event.getRegistry().register(networkRelay);

        event.getRegistry().register(liftValve);
        event.getRegistry().register(liftLever);
        event.getRegistry().register(networkDisplay);
        event.getRegistry().register(gyroscopeStabilizer);
        event.getRegistry().register(gyroscopeDampener);

        event.getRegistry().register(valkyriumCompressorPart);
        event.getRegistry().register(valkyriumEnginePart);
        event.getRegistry().register(rudderPart);
        event.getRegistry().register(giantPropellerPart);
        event.getRegistry().register(gearbox);
        event.getRegistry().register(rotationAxle);
    }

    public void registerBlockItems(RegistryEvent.Register<Item> event) {
        registerItemBlock(event, basicEngine);
        registerItemBlock(event, advancedEngine);
        registerItemBlock(event, eliteEngine);
        registerItemBlock(event, ultimateEngine);
        registerItemBlock(event, redstoneEngine);

        registerItemBlock(event, compactedValkyrium);
        registerItemBlock(event, captainsChair);
        registerItemBlock(event, passengerChair);

        registerItemBlock(event, shipHelm);
        registerItemBlock(event, shipWheel);
        registerItemBlock(event, speedTelegraph);
        registerItemBlock(event, dummyTelegraph);
        registerItemBlock(event, networkRelay);

        registerItemBlock(event, liftValve);
        registerItemBlock(event, liftLever);
        registerItemBlock(event, networkDisplay);
        registerItemBlock(event, valkyriumCompressorPart);
        registerItemBlock(event, gyroscopeStabilizer);
        registerItemBlock(event, gyroscopeDampener);

        registerItemBlock(event, valkyriumEnginePart);
        registerItemBlock(event, gearbox);
        registerItemBlock(event, rudderPart);
        registerItemBlock(event, giantPropellerPart);
        registerItemBlock(event, rotationAxle);
    }

    private void registerItemBlock(RegistryEvent.Register<Item> event, Block block) {
        Module.registerItemBlock(event, block);
    }
}
