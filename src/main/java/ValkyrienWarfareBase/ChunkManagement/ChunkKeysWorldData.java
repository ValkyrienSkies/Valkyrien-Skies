package ValkyrienWarfareBase.ChunkManagement;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class ChunkKeysWorldData extends WorldSavedData {

	private static final String key = "ChunkKeys";
	public int chunkKey;
	
	public ChunkKeysWorldData() {
		super(key);
	}
	
	public ChunkKeysWorldData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		chunkKey = nbt.getInteger("chunkKey");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("chunkKey", chunkKey);
		return nbt;
	}

	public static ChunkKeysWorldData get(World world) {
		ChunkKeysWorldData data = (ChunkKeysWorldData)world.loadItemData(ChunkKeysWorldData.class, key);
		if (data == null) {
			data = new ChunkKeysWorldData();
			world.setItemData(key, data);
		}
		return data;
	}
	
}
