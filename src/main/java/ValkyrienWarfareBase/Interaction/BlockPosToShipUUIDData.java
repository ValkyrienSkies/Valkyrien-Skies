package ValkyrienWarfareBase.Interaction;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.ChunkManagement.ChunkSet;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class BlockPosToShipUUIDData extends WorldSavedData{

	private static final String key = "BlockPosToShipUUIDData";
	//Not the persistent map, used for performance reasons
	private HashMap<Long, UUID> chunkposToShipUUID = new HashMap<Long, UUID>();
	private HashMap<UUID, ChunkSet> UUIDToChunkSet = new HashMap<UUID, ChunkSet>();

	public BlockPosToShipUUIDData(String name) {
		super(name);
	}

	public BlockPosToShipUUIDData() {
		super(key);
	}

	public UUID getShipUUIDFromPos(int chunkX, int ChunkZ){
		long chunkPos = ChunkPos.chunkXZ2Int(chunkX, ChunkZ);

		return chunkposToShipUUID.get(chunkPos);
	}

	public void addShipToPersistantMap(PhysicsWrapperEntity toAdd){
		UUID shipID = toAdd.getPersistentID();

		int centerX = toAdd.wrapping.ownedChunks.centerX;
		int centerZ = toAdd.wrapping.ownedChunks.centerZ;
		int radius = toAdd.wrapping.ownedChunks.radius;

		for(int x = centerX - radius; x <= centerX + radius; x++){
			for(int z = centerZ - radius; z <= centerZ + radius; z++){
				long chunkPos = ChunkPos.chunkXZ2Int(x, z);
				chunkposToShipUUID.put(chunkPos, shipID);
			}
		}
		UUIDToChunkSet.put(toAdd.getPersistentID(), toAdd.wrapping.ownedChunks);
		markDirty();
	}

	public void removeShipFromPersistantMap(PhysicsWrapperEntity toRemove){
		int centerX = toRemove.wrapping.ownedChunks.centerX;
		int centerZ = toRemove.wrapping.ownedChunks.centerZ;
		int radius = toRemove.wrapping.ownedChunks.radius;

		for(int x = centerX - radius; x <= centerX + radius; x++){
			for(int z = centerZ - radius; z <= centerZ + radius; z++){
				long chunkPos = ChunkPos.chunkXZ2Int(x, z);
				chunkposToShipUUID.remove(chunkPos);
			}
		}
		UUIDToChunkSet.remove(toRemove.getPersistentID());
		markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		ByteBuffer buffer = NBTUtils.getByteBuf("WorldChunkSetUUIDMix", compound);

//		buffer.flip();
		while(buffer.hasRemaining()){

			int centerX = buffer.getInt();
			int centerZ = buffer.getInt();
			byte radius = buffer.get();
			long mostBits = buffer.getLong();
			long leastBits = buffer.getLong();

//			System.out.println("Loaded a ChunkSet at " + centerX + ":" + centerZ);

			UUID persistantID = new UUID(mostBits, leastBits);
			ChunkSet set = new ChunkSet(centerX, centerZ, radius);

			UUIDToChunkSet.put(persistantID, set);

			for(int x = centerX - radius; x <= centerX + radius; x++){
				for(int z = centerZ - radius; z <= centerZ + radius; z++){
					chunkposToShipUUID.put(ChunkPos.chunkXZ2Int(x, z), persistantID);
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		Set<Entry<UUID, ChunkSet>> entries = UUIDToChunkSet.entrySet();

		//2 ints, 1 byte (radius), and 2 longs for each ship, that comes out to 25 bytes per entry
		int byteArraySize = entries.size() * 25;
		ByteBuffer buffer = ByteBuffer.allocate(byteArraySize);
		for(Entry<UUID, ChunkSet> entry: entries){
			int centerX = entry.getValue().centerX;
			int centerZ = entry.getValue().centerZ;
			byte radius = (byte) entry.getValue().radius;
			long mostBits = entry.getKey().getMostSignificantBits();
			long leastBits = entry.getKey().getLeastSignificantBits();

			buffer.putInt(centerX);
			buffer.putInt(centerZ);
			buffer.put(radius);
			buffer.putLong(mostBits);
			buffer.putLong(leastBits);
		}

		NBTUtils.setByteBuf("WorldChunkSetUUIDMix", buffer, compound);
		return compound;
	}

	public static BlockPosToShipUUIDData get(World world) {
		MapStorage storage = world.getPerWorldStorage();
		BlockPosToShipUUIDData data = (BlockPosToShipUUIDData) storage.getOrLoadData(BlockPosToShipUUIDData.class, key);
		if (data == null) {
			data = new BlockPosToShipUUIDData();
			world.setItemData(key, data);
		}
		return data;
	}

}
