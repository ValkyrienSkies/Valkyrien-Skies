package ValkyrienWarfareBase.Interaction;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class ShipNameUUIDData extends WorldSavedData{

	private static final String key = "ShipNameUUIDData";
	
	public HashMap<String, Long> ShipNameToLongMap = new HashMap<String, Long>();
	
	public ShipNameUUIDData(String name) {
		super(name);
	}
	
	public ShipNameUUIDData(){
		this(key);
	}

	/**
	 * Only run this for the initial creation of a Ship; doesnt have checks for duplicate names
	 * @param wrapper
	 * @param defaultName
	 */
	public void placeShipInRegistry(PhysicsWrapperEntity wrapper, String defaultName){
		ShipNameToLongMap.put(defaultName, wrapper.getPersistentID().getMostSignificantBits());
		markDirty();
	}
	
	/**
	 * Returns true if successfully renamed the ship, false if there was a duplicate
	 * @param wrapper
	 * @param newName
	 * @param oldName
	 * @return
	 */
	public boolean renameShipInRegsitry(PhysicsWrapperEntity wrapper, String newName, String oldName){
		if(ShipNameToLongMap.containsKey(newName)){
			return false;
		}

		ShipNameToLongMap.put(newName, wrapper.getPersistentID().getMostSignificantBits());
		ShipNameToLongMap.remove(oldName);
		
		markDirty();
		return true;
	}
	
	public void removeShipFromRegistry(PhysicsWrapperEntity wrapper){
		String customName = wrapper.getCustomNameTag();
		ShipNameToLongMap.remove(customName);
		markDirty();
	}

	public static ShipNameUUIDData get(World world) {
		MapStorage storage = world.getPerWorldStorage();
		ShipNameUUIDData data = (ShipNameUUIDData) storage.getOrLoadData(ShipNameUUIDData.class, key);
		if (data == null) {
			data = new ShipNameUUIDData();
			world.setItemData(key, data);
		}
		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		ByteBuffer buffer = NBTUtils.getByteBuf("NameToUUIDMap", nbt);
		while(buffer.hasRemaining()){
			byte stringByteLength = buffer.get();
			byte[] stringBytes = new byte[stringByteLength];
			for(int i = 0; i < stringByteLength; i++){
				stringBytes[i] = buffer.get();
			}
			String shipName = new String(stringBytes, StandardCharsets.UTF_8);
			long shipUUIDMostSig = buffer.getLong();
			ShipNameToLongMap.put(shipName, shipUUIDMostSig);
		}
	}

	//Inefficient, but the Ship name map shouldn't update often anyways
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		Set<Entry<String, Long>> entryMap = ShipNameToLongMap.entrySet();
		int stringEntriesSize = 0;
		for(Entry<String, Long> entry : entryMap){
			byte[] stringBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
			stringEntriesSize += (stringBytes.length) + 9;
		}

		ByteBuffer buffer = ByteBuffer.allocate(stringEntriesSize);
		for(Entry<String, Long> entry : entryMap){
			byte[] stringBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
			//Array length
			buffer.put((byte)stringBytes.length);
			for(byte b: stringBytes){
				buffer.put(b);
			}
			buffer.putLong(entry.getValue());
		}
		NBTUtils.setByteBuf("NameToUUIDMap", buffer, compound);
		return compound;
	}
	
}
