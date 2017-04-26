package ValkyrienWarfareBase.Interaction;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class ShipUUIDToPosData extends WorldSavedData{

	private static String key = "ShipUUIDToPosData";

	private HashMap<Long, ShipPositionData> dataMap = new HashMap<Long, ShipPositionData>();

	public ShipUUIDToPosData(String name) {
		super(name);
	}

	public ShipUUIDToPosData() {
		super(key);
	}

	public ShipPositionData getShipPositionData(UUID shipID){
		return dataMap.get(shipID.getMostSignificantBits());
	}
	
	public ShipPositionData getShipPositionData(long mostSigBits){
		return dataMap.get(mostSigBits);
	}

	public void updateShipPosition(PhysicsWrapperEntity wrapper){
		UUID entityID = wrapper.getPersistentID();
		long key = entityID.getMostSignificantBits();
		ShipPositionData data = dataMap.get(key);
		if(data != null){
			data.updateData(wrapper);
		}else{
			data = new ShipPositionData(wrapper);
			dataMap.put(key, data);
		}
		markDirty();
	}

	public void removeShipFromMap(PhysicsWrapperEntity wrapper){
		UUID entityID = wrapper.getPersistentID();
		dataMap.remove(entityID);
		markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		ByteBuffer buffer = NBTUtils.getByteBuf("ShipPositionByteBuf", nbt);

		while(buffer.hasRemaining()){
			long mostBits = buffer.getLong();

			ShipPositionData data = new ShipPositionData(buffer);
			dataMap.put(mostBits, data);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		Set<Entry<Long, ShipPositionData>> entries = dataMap.entrySet();

		//Each ship has 19 floats, and 1 long; that comes out (19 * 4 + 1 * 8) = 84 bytes per ship
		int byteArraySize = entries.size() * 84;

		ByteBuffer buffer = ByteBuffer.allocate(byteArraySize);

		for(Entry<Long, ShipPositionData> entry: entries){
			long mostBits = entry.getKey();

			ShipPositionData posData = entry.getValue();

			buffer.putLong(mostBits);
			posData.writeToByteBuffer(buffer);
		}

		NBTUtils.setByteBuf("ShipPositionByteBuf", buffer, compound);

		return compound;
	}

	public static ShipUUIDToPosData get(World world) {
		MapStorage storage = world.getPerWorldStorage();
		ShipUUIDToPosData data = (ShipUUIDToPosData) storage.getOrLoadData(ShipUUIDToPosData.class, key);
		if (data == null) {
			data = new ShipUUIDToPosData();
			world.setItemData(key, data);
		}
		return data;
	}
	
	public Set<Long> getShipHalfUUIDsSet(){
		return dataMap.keySet();
	}

	public class ShipPositionData{
		public Vector shipPosition;
		public float[] lToWTransform;

		public ShipPositionData(PhysicsWrapperEntity wrapper){
			shipPosition = new Vector(wrapper.posX, wrapper.posY, wrapper.posZ);
			lToWTransform = RotationMatrices.convertToFloat(wrapper.wrapping.coordTransform.lToWTransform);
		}

		public ShipPositionData(ByteBuffer buffer){
			shipPosition = new Vector(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());

			lToWTransform = new float[16];
			for(int i = 0;i < 16; i++){
				lToWTransform[i] = buffer.getFloat();
			}
		}

		public void writeToByteBuffer(ByteBuffer buffer){
			buffer.putFloat((float) shipPosition.X);
			buffer.putFloat((float) shipPosition.Y);
			buffer.putFloat((float) shipPosition.Z);

			for(int i = 0;i < 16; i++){
				buffer.putFloat(lToWTransform[i]);
			}
		}

		public void updateData(PhysicsWrapperEntity wrapper){
			shipPosition.X = wrapper.posX;
			shipPosition.Y = wrapper.posY;
			shipPosition.Z = wrapper.posZ;
			lToWTransform = RotationMatrices.convertToFloat(wrapper.wrapping.coordTransform.lToWTransform);
		}
	}

}
