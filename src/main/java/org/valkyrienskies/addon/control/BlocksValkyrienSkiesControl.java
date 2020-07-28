package org.valkyrienskies.addon.control;

import net.minecraft.item.ItemBlock;
import org.valkyrienskies.addon.control.block.*;
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

    public final BlockPhysicsInfuser physicsInfuser;
    public final BlockPhysicsInfuserDummy physicsInfuserDummy;
    public final BlockPhysicsInfuserCreative physicsInfuserCreative;
    public final BlockNormalEngine basicEngine;
    public final BlockNormalEngine advancedEngine;
    public final BlockNormalEngine eliteEngine;
    public final BlockNormalEngine ultimateEngine;
    public final BlockRedstoneEngine redstoneEngine;
    public final Block compactedValkyrium;
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
        physicsInfuser = registerBlock(new BlockPhysicsInfuser("physics_infuser"));
        physicsInfuserCreative = registerBlock(new BlockPhysicsInfuserCreative());
        physicsInfuserDummy = registerBlock(new BlockPhysicsInfuserDummy());

        basicEngine = registerBlock(new BlockNormalEngine("basic", Material.WOOD,
            VSConfig.ENGINE_POWER.basicEnginePower, 5.0F));
        advancedEngine = registerBlock(new BlockNormalEngine("advanced", Material.ROCK,
            VSConfig.ENGINE_POWER.advancedEnginePower, 6.0F));
        eliteEngine = registerBlock(new BlockNormalEngine("elite", Material.IRON,
            VSConfig.ENGINE_POWER.eliteEnginePower, 8.0F));
        ultimateEngine = registerBlock(new BlockNormalEngine("ultimate", Material.GROUND,
            VSConfig.ENGINE_POWER.ultimateEnginePower, 10.0F));
        redstoneEngine = registerBlock(new BlockRedstoneEngine());

        compactedValkyrium = registerBlock(new BlockCompactedValkyrium());
        shipHelm = registerBlock(new BlockShipHelm());
        shipWheel = registerBlock(new BlockShipWheel());
        speedTelegraph = registerBlock(new BlockSpeedTelegraph());
        dummyTelegraph = registerBlock(new BlockDummyTelegraph());

        networkRelay = registerBlock(new BlockNetworkRelay());
        networkDisplay = registerBlock(new BlockNetworkDisplay());

        gyroscopeStabilizer = registerBlock(new BlockGyroscopeStabilizer());
        gyroscopeDampener = registerBlock(new BlockGyroscopeDampener());

        liftValve = registerBlock(new BlockLiftValve());
        liftLever = registerBlock(new BlockLiftLever());

        valkyriumCompressorPart = registerBlock(new BlockValkyriumCompressorPart());
        valkyriumEnginePart = registerBlock(new BlockValkyriumEnginePart());
        rudderPart = registerBlock(new BlockRudderPart());
        giantPropellerPart = registerBlock(new BlockGiantPropellerPart());
        rotationAxle = registerBlock(new BlockRotationAxle());
        gearbox = registerBlock(new BlockGearbox());
    }

    private <T extends Block> T registerBlock(T block) {
        ValkyrienSkiesControl.BLOCKS.add(block);
        ValkyrienSkiesControl.ITEMS.add(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        return block;
    }
}
