package ValkyrienWarfareWorld.WorldGen;

import java.util.Random;

import ValkyrienWarfareBase.ChunkManagement.PhysicsChunkManager;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.Schematics.SchematicReader;
import ValkyrienWarfareBase.Schematics.SchematicReader.Schematic;
import ValkyrienWarfareWorld.ValkyrienWarfareWorldMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

/**
 * Created by joeyr on 4/18/2017.
 */
public class ValkyrienWarfareWorldGen implements IWorldGenerator {

    public WorldGenMinable genEtheriumOre;

    public ValkyrienWarfareWorldGen() {
        this.genEtheriumOre = new WorldGenMinable(ValkyrienWarfareWorldMod.etheriumOre.getDefaultState(), 8);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        switch (world.provider.getDimension()) {
            case 0: //Overworld
                this.runEtheriumGenerator(this.genEtheriumOre, world, random, chunkX, chunkZ, 2, 0, 25);
                runDungeonGenerator(world, random, chunkX, chunkZ, 1);
                break;
            case -1: //Nether
                break;
            case 1: //End
                break;
        }
    }

    private void runEtheriumGenerator(WorldGenerator generator, World world, Random rand, int chunk_X, int chunk_Z, int chancesToSpawn, int minHeight, int maxHeight) {
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

    private void runDungeonGenerator(World world, Random rand, int chunk_X, int chunk_Z, int chancesToSpawn) {
    	boolean isLikelyShipChunk = PhysicsChunkManager.isLikelyShipChunk(chunk_X, chunk_Z);

    	//TODO: Enable this
    	if(!isLikelyShipChunk && false) {
	    	double random = Math.random();
	    	if(random < 0.1) {
	    		//do it
	    		System.out.println("Generating a VW temple");

	    		Schematic lootGet = SchematicReader.get("flying_temple.schemat");

	    		if(lootGet == null) {
	    			System.out.println("fuck");
	    			return;
	    		}

	    		PhysicsWrapperEntity wrapperEntity = new PhysicsWrapperEntity(world, Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ, lootGet);

	    		//do it
	    		wrapperEntity.forceSpawn = true;

	    		System.out.println(world.restoringBlockSnapshots);

	    		world.spawnEntity(wrapperEntity);

	    		wrapperEntity.posY = 150D;
	    	}
    	}
    }
}
