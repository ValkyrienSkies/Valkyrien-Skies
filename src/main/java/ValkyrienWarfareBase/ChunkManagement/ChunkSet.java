package ValkyrienWarfareBase.ChunkManagement;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * This stores the chunk claims for a PhysicsObject; not the chunks themselves
 * 
 * @author thebest108
 *
 */
public class ChunkSet {

	public World world;
	public int centerX, centerZ;
	public int radius;
	public int minX, maxX, minZ, maxZ;
	public boolean[][] chunkOccupiedInLocal;

	public ChunkSet(int x, int z, int size) {
		centerX = x;
		centerZ = z;
		radius = size;
		minX = centerX - radius;
		maxX = centerX + radius;
		minZ = centerZ - radius;
		maxZ = centerZ + radius;
		chunkOccupiedInLocal = new boolean[(radius * 2) + 1][(radius * 2) + 1];
	}

	public ChunkSet(NBTTagCompound readFrom) {
		this(readFrom.getInteger("centerX"), readFrom.getInteger("centerZ"), readFrom.getInteger("radius"));
	}

	public void writeToNBT(NBTTagCompound toSave) {
		toSave.setInteger("centerX", centerX);
		toSave.setInteger("centerZ", centerZ);
		toSave.setInteger("radius", radius);
	}

	public boolean isChunkEnclosedInMaxSet(int chunkX, int chunkZ) {
		boolean inX = (chunkX >= centerX - 12) && (chunkX <= centerX + 12);
		boolean inZ = (chunkZ >= centerZ - 12) && (chunkZ <= centerZ + 12);
		return inX && inZ;
	}

	public boolean isChunkEnclosedInSet(int chunkX, int chunkZ) {
		boolean inX = (chunkX >= minX) && (chunkX <= maxX);
		boolean inZ = (chunkZ >= minZ) && (chunkZ <= maxZ);
		return inX && inZ;
	}

	@Override
	public String toString() {
		return centerX + ":" + centerZ + ":" + radius;
	}

}
