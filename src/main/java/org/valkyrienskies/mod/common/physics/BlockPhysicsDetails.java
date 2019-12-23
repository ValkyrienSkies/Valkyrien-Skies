package org.valkyrienskies.mod.common.physics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.block.IBlockMassProvider;
import org.valkyrienskies.mod.common.block.IBlockTorqueProvider;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

public class BlockPhysicsDetails {

    static final String BLOCK_MASS_VERSION = "v0.1";
    // A 1x1x1 cube of DEFAULT is 500kg.
    private final static double DEFAULT_MASS = 500D;

    /**
     * Blocks mapped to their mass.
     */
    private static final HashMap<Block, Double> blockToMass = new HashMap<>();
    /**
     * Material.mapped to their mass.
     */
    private static final HashMap<Material, Double> materialMass = new HashMap<>();
    /**
     * Blocks that should not be infused with physics.
     */
    public static final ArrayList<Block> blocksToNotPhysicsInfuse = new ArrayList<>();

    static {
        generateBlockMasses();
        generateMaterialMasses();
        generateBlocksToNotPhysicsInfuse();

        VSConfig.registerSyncEvent(BlockPhysicsDetails::onSync);
        onSync();
    }

    private static void onSync() {
        Arrays.stream(VSConfig.blockMass)
            .map(str -> str.split("="))
            .filter(arr -> arr.length == 2)
            .forEach(arr ->
                blockToMass.put(Block.getBlockFromName(arr[0]), Double.parseDouble(arr[1])));
    }

    private static void generateMaterialMasses() {
        materialMass.put(Material.AIR, 0D);
        materialMass.put(Material.ANVIL, 8000D);
        materialMass.put(Material.BARRIER, 0D);
        materialMass.put(Material.CACTUS, 400D);
        materialMass.put(Material.CAKE, 100D);
        materialMass.put(Material.CARPET, 100D);
        materialMass.put(Material.CIRCUITS, 200D);
        materialMass.put(Material.CLAY, 2000D);
        materialMass.put(Material.CLOTH, 300D);
        materialMass.put(Material.CORAL, 2000D);
        materialMass.put(Material.CRAFTED_SNOW, 500D);
        materialMass.put(Material.DRAGON_EGG, 500D);
        materialMass.put(Material.FIRE, 0D);
        materialMass.put(Material.GLASS, 2000D);
        materialMass.put(Material.GOURD, 1500D);
        materialMass.put(Material.GRASS, 1500D);
        materialMass.put(Material.GROUND, 1500D);
        materialMass.put(Material.ICE, 500D);
        materialMass.put(Material.IRON, 8000D);
        materialMass.put(Material.LAVA, 2500D);
        materialMass.put(Material.LEAVES, 100D);
        materialMass.put(Material.PACKED_ICE, 500D);
        materialMass.put(Material.PISTON, 3000D);
        materialMass.put(Material.PLANTS, 300D);
        materialMass.put(Material.PORTAL, 0D);
        materialMass.put(Material.REDSTONE_LIGHT, 100D);
        materialMass.put(Material.ROCK, 3000D);
        materialMass.put(Material.SAND, 2000D);
        materialMass.put(Material.SNOW, 500D);
        materialMass.put(Material.SPONGE, 100D);
        materialMass.put(Material.STRUCTURE_VOID, 0D);
        materialMass.put(Material.TNT, 2000D);
        materialMass.put(Material.VINE, 300D);
        materialMass.put(Material.WATER, 1000D);
        materialMass.put(Material.WEB, 100D);
        materialMass.put(Material.WOOD, 500D);
    }

    private static void generateBlockMasses() {
        blockToMass.put(Blocks.AIR, 0D);
        blockToMass.put(Blocks.FIRE, 0D);
        blockToMass.put(Blocks.FLOWING_WATER, 0D);
        blockToMass.put(Blocks.FLOWING_LAVA, 0D);
        blockToMass.put(Blocks.WATER, 0D);
        blockToMass.put(Blocks.LAVA, 0D);
    }

    private static void generateBlocksToNotPhysicsInfuse() {
        blocksToNotPhysicsInfuse.add(Blocks.AIR);
        blocksToNotPhysicsInfuse.add(Blocks.WATER);
        blocksToNotPhysicsInfuse.add(Blocks.FLOWING_WATER);
        blocksToNotPhysicsInfuse.add(Blocks.LAVA);
        blocksToNotPhysicsInfuse.add(Blocks.FLOWING_LAVA);
    }

    /**
     * Get block mass, in kg.
     */
    public static double getMassFromState(IBlockState state, BlockPos pos, World world) {
        Block block = state.getBlock();
        if (block instanceof IBlockMassProvider) {
            return ((IBlockMassProvider) block).getBlockMass(world, pos, state);
        } else {
            return getMassOfBlock(block);
        }
    }

    private static double getMassOfMaterial(Material material) {
        return materialMass.getOrDefault(material, DEFAULT_MASS);
    }

    private static double getMassOfBlock(Block block) {
        if (block instanceof BlockLiquid) {
            return 0D;
        } else if (blockToMass.get(block) != null) {
            return blockToMass.get(block);
        } else {
            return getMassOfMaterial(block.material);
        }
    }

    /**
     * Assigns the output parameter of toSet to be the force Vector for the given IBlockState.
     */
    static void getForceFromState(IBlockState state, BlockPos pos, World world,
        double secondsToApply,
        PhysicsObject obj, Vector toSet) {
        Block block = state.getBlock();
        if (block instanceof IBlockForceProvider) {
            Vector forceVector = ((IBlockForceProvider) block)
                .getBlockForceInWorldSpace(world, pos, state,
                    obj, secondsToApply);
            if (forceVector == null) {
                toSet.zero();
            } else {
                toSet.x = forceVector.x;
                toSet.y = forceVector.y;
                toSet.z = forceVector.z;
            }
        }
    }

    /**
     * Returns true if the given IBlockState can create force; otherwise it returns false.
     */
    public static boolean isBlockProvidingForce(IBlockState state, BlockPos pos, World world) {
        Block block = state.getBlock();
        return block instanceof IBlockForceProvider || block instanceof IBlockTorqueProvider;
    }

}
