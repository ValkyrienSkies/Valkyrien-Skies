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
