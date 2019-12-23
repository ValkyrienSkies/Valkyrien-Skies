package org.valkyrienskies.addon.control;

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
import org.valkyrienskies.mod.common.config.VSConfig;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

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
        basicEngine = new BlockNormalEngine("basic", Material.WOOD,
            VSConfig.ENGINE_POWER.basicEnginePower, 5.0F);
        advancedEngine = new BlockNormalEngine("advanced", Material.ROCK,
            VSConfig.ENGINE_POWER.basicEnginePower, 6.0F);
        eliteEngine = new BlockNormalEngine("elite", Material.IRON,
            VSConfig.ENGINE_POWER.basicEnginePower, 8.0F);
        ultimateEngine = new BlockNormalEngine("ultimate", Material.GROUND,
            VSConfig.ENGINE_POWER.basicEnginePower, 10.0F);
        redstoneEngine = new BlockRedstoneEngine();

        compactedValkyrium = new BlockCompactedValkyrium();
        captainsChair = new BlockCaptainsChair();
        passengerChair = new BlockPassengerChair();
        shipHelm = new BlockShipHelm();
        shipWheel = new BlockShipWheel();
        speedTelegraph = new BlockSpeedTelegraph();
        dummyTelegraph = new BlockDummyTelegraph();

        networkRelay = new BlockNetworkRelay();
        networkDisplay = new BlockNetworkDisplay();

        gyroscopeStabilizer = new BlockGyroscopeStabilizer();
        gyroscopeDampener = new BlockGyroscopeDampener();

        liftValve = new BlockLiftValve();
        liftLever = new BlockLiftLever();

        valkyriumCompressorPart = new BlockValkyriumCompressorPart();
        valkyriumEnginePart = new BlockValkyriumEnginePart();
        rudderPart = new BlockRudderPart();
        giantPropellerPart = new BlockGiantPropellerPart();
        rotationAxle = new BlockRotationAxle();
        gearbox = new BlockGearbox();
    }
}
