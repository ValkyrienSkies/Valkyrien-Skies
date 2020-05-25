package org.valkyrienskies.addon.world.worldgen;

import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.addon.world.ValkyrienSkiesWorld;

/**
 * Created by joeyr on 4/18/2017.
 */
public class ValkyrienSkiesWorldGen implements IWorldGenerator {

    public WorldGenMinable genValkyriumOre = null;

    public ValkyrienSkiesWorldGen() {
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
        IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (ValkyrienSkiesWorld.OREGEN_ENABLED && VSConfig.valkyriumSpawnRate > 0) {
            if (this.genValkyriumOre == null) {
                this.genValkyriumOre = new WorldGenMinable(
                    ValkyrienSkiesWorld.INSTANCE.valkyriumOre.getDefaultState(), 8);
            }
            switch (world.provider.getDimension()) {
                case 0: //Overworld
                    this.runValkyriumGenerator(this.genValkyriumOre, world, random, chunkX, chunkZ, VSConfig.valkyriumSpawnRate,
                        0, 25);
                    // runDungeonGenerator(world, random, chunkX, chunkZ, 1);
                    break;
                case -1: //Nvalkyrium
                    break;
                case 1: //End
                    break;
            }
        }
    }

    private void runValkyriumGenerator(WorldGenerator generator, World world, Random rand,
        int chunk_X, int chunk_Z, int chancesToSpawn, int minHeight, int maxHeight) {
        if (minHeight < 0 || maxHeight > 256 || minHeight > maxHeight) {
            throw new IllegalArgumentException("Illegal Height Arguments for WorldGenerator");
        }

        int heightDiff = maxHeight - minHeight + 1;
        for (int i = 0; i < chancesToSpawn; i++) {
            int x = chunk_X * 16 + rand.nextInt(16);
            int y = minHeight + rand.nextInt(heightDiff);
            int z = chunk_Z * 16 + rand.nextInt(16);
            generator.generate(world, rand, new BlockPos(x, y, z));
        }
    }

}
