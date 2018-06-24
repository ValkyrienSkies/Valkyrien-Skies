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

package valkyrienwarfare.physics;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.deprecated_api.IBlockMassProvider;

import java.util.HashMap;

public class BlockMass {

    // 80N, Something like ~ 20lbs
    private final static double defaultMass = 50D;
    public static BlockMass basicMass = new BlockMass();
    public HashMap<Block, Double> blockToMass = new HashMap<Block, Double>();
    public HashMap<Material, Double> materialMass = new HashMap<Material, Double>();

    public BlockMass() {
        generateMaterialMasses();
    }

    public static void registerBlockMass(Block block, double mass) {
        basicMass.blockToMass.put(block, mass);
    }

    private void generateMaterialMasses() {
        materialMass.put(Material.AIR, 0D);
        materialMass.put(Material.ANVIL, 200D);
        materialMass.put(Material.BARRIER, 0D);
        materialMass.put(Material.CACTUS, 15D);
        materialMass.put(Material.CAKE, 10D);
        materialMass.put(Material.CARPET, 5D);
        materialMass.put(Material.CIRCUITS, 15D);
        materialMass.put(Material.CLAY, 40D);
        materialMass.put(Material.CLOTH, 20D);
        materialMass.put(Material.CORAL, 70D);
        materialMass.put(Material.CRAFTED_SNOW, 20D);
        materialMass.put(Material.DRAGON_EGG, 20D);
        materialMass.put(Material.FIRE, 0D);
        materialMass.put(Material.GLASS, 20D);
        materialMass.put(Material.GOURD, 50D);
        materialMass.put(Material.GRASS, 30D);
        materialMass.put(Material.GROUND, 70D);
        materialMass.put(Material.ICE, 50D);
        materialMass.put(Material.IRON, 250D);
        materialMass.put(Material.LAVA, 0D);
        materialMass.put(Material.LEAVES, 10D);
        materialMass.put(Material.PACKED_ICE, 40D);
        materialMass.put(Material.PISTON, 15D);
        materialMass.put(Material.PLANTS, 10D);
        materialMass.put(Material.PORTAL, 0D);
        materialMass.put(Material.REDSTONE_LIGHT, 10D);
        materialMass.put(Material.ROCK, 220D);
        materialMass.put(Material.SAND, 45D);
        materialMass.put(Material.SNOW, 20D);
        materialMass.put(Material.SPONGE, 20D);
        materialMass.put(Material.STRUCTURE_VOID, 0D);
        materialMass.put(Material.TNT, 30D);
        materialMass.put(Material.VINE, 5D);
        materialMass.put(Material.WATER, 0D);
        materialMass.put(Material.WEB, 10D);
        materialMass.put(Material.WOOD, 25D);
    }

    public double getMassFromState(IBlockState state, BlockPos pos, World world) {
        Block block = state.getBlock();
        if (block instanceof IBlockMassProvider) {
            return ((IBlockMassProvider) block).getBlockMass(world, pos, state);
        } else {
            Double fromMap = blockToMass.get(block);
            if (fromMap != null) {
                return fromMap.doubleValue();
            } else {
                Double newMass = generateMassForBlock(block);
                blockToMass.put(block, newMass);
                return newMass;
            }
        }
    }

    public double getMassFromMaterial(Material material) {
        Double mass = materialMass.get(material);
        if (mass == null) {
            mass = defaultMass;
            materialMass.put(material, mass);
        }
        return mass;
    }

    public Double generateMassForBlock(Block block) {
        if (block instanceof BlockLiquid) {
            return 0D;
        }
        Material material = block.blockMaterial;

        return getMassFromMaterial(material);
        //Old formula
//		double hardness = block.blockHardness;
//		double resistance = block.blockResistance;
//		return hardness * 50D + 2 * math.pow(resistance, 1 / 4);
    }

}
