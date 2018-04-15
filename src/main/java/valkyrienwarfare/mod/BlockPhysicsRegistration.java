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

package valkyrienwarfare.mod;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.data.BlockForce;
import valkyrienwarfare.physics.data.BlockMass;

public class BlockPhysicsRegistration {

    public static BlockMass blockMass = BlockMass.basicMass;
    public static BlockForce blockForces = BlockForce.basicForces;
    public static ArrayList<Block> blocksToNotPhysicise = new ArrayList<>();

    public static void registerCustomBlockMasses() {
        BlockMass.registerBlockMass(Blocks.AIR, 0D);
        BlockMass.registerBlockMass(Blocks.FIRE, 0D);
        BlockMass.registerBlockMass(Blocks.FLOWING_WATER, 0D);
        BlockMass.registerBlockMass(Blocks.FLOWING_LAVA, 0D);
        BlockMass.registerBlockMass(Blocks.WATER, 0D);
        BlockMass.registerBlockMass(Blocks.LAVA, 0D);
        // blockMass.registerBlockMass(Blocks.WOOL, 10D);
        // blockMass.registerBlockMass(Blocks.PLANKS, 50D);
        // blockMass.registerBlockMass(Blocks.SAND, 120D);
        // blockMass.registerBlockMass(Blocks.COBBLESTONE, 180D);
        // blockMass.registerBlockMass(Blocks.STONE, 180D);
        // blockMass.registerBlockMass(Blocks.IRON_BLOCK, 250D);
        // blockMass.registerBlockMass(Blocks.OBSIDIAN, 500D);
        BlockMass.registerBlockMass(Blocks.BEDROCK, 5000D);
    }

    public static void registerVanillaBlockForces() {
        BlockForce.registerBlockForce(ValkyrienWarfareControl.INSTANCE.blocks.dopedEtherium, new Vector(0, 10000D, 0), false);
    }

    public static void registerBlocksToNotPhysicise() {
        blocksToNotPhysicise.add(Blocks.AIR);
        blocksToNotPhysicise.add(Blocks.WATER);
        blocksToNotPhysicise.add(Blocks.FLOWING_WATER);
        blocksToNotPhysicise.add(Blocks.LAVA);
        blocksToNotPhysicise.add(Blocks.FLOWING_LAVA);
    }
}
