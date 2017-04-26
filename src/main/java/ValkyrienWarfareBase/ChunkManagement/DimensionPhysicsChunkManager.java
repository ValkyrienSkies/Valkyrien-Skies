package ValkyrienWarfareBase.ChunkManagement;

import java.util.HashMap;
import java.util.UUID;

import ValkyrienWarfareBase.Interaction.BlockPosToShipUUIDData;
import ValkyrienWarfareBase.Interaction.ShipNameUUIDData;
import ValkyrienWarfareBase.Interaction.ShipUUIDToPosData;
import ValkyrienWarfareBase.Interaction.ShipUUIDToPosData.ShipPositionData;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.world.World;

public class DimensionPhysicsChunkManager {

	private HashMap<World, PhysicsChunkManager> managerPerWorld;
	private PhysicsChunkManager cachedManager;

	public DimensionPhysicsChunkManager() {
		managerPerWorld = new HashMap<World, PhysicsChunkManager>();
	}

	public void initWorld(World toInit) {
		if (!managerPerWorld.containsKey(toInit)) {
			managerPerWorld.put(toInit, new PhysicsChunkManager(toInit));
		}
	}

	public PhysicsChunkManager getManagerForWorld(World world) {
		if(world == null){
			return null;
		}
		if (cachedManager == null || cachedManager.worldObj != world) {
			cachedManager = managerPerWorld.get(world);
			if (cachedManager == null) {
				initWorld(world);
				cachedManager = managerPerWorld.get(world);
			}
		}
		return cachedManager;
	}

	public void removeWorld(World world) {
		managerPerWorld.remove(world);
	}

	public void registerChunksForShip(PhysicsWrapperEntity wrapper){
		World shipWorld = wrapper.worldObj;
		BlockPosToShipUUIDData data = BlockPosToShipUUIDData.get(shipWorld);
		data.addShipToPersistantMap(wrapper);
	}

	public void removeRegistedChunksForShip(PhysicsWrapperEntity wrapper){
		World shipWorld = wrapper.worldObj;
		BlockPosToShipUUIDData data = BlockPosToShipUUIDData.get(shipWorld);

		data.removeShipFromPersistantMap(wrapper);
	}

	public UUID getShipIDManagingPos_Persistant(World worldFor, int chunkX, int chunkZ){
		BlockPosToShipUUIDData data = BlockPosToShipUUIDData.get(worldFor);

		return data.getShipUUIDFromPos(chunkX, chunkZ);
	}

	public ShipPositionData getShipPosition_Persistant(World worldFor, UUID shipID){
		ShipUUIDToPosData data = ShipUUIDToPosData.get(worldFor);

		return data.getShipPositionData(shipID);
	}

	public void updateShipPosition(PhysicsWrapperEntity wrapper){
		World shipWorld = wrapper.worldObj;
		ShipUUIDToPosData data = ShipUUIDToPosData.get(shipWorld);

		data.updateShipPosition(wrapper);
	}

	public void removeShipPosition(PhysicsWrapperEntity wrapper){
		World shipWorld = wrapper.worldObj;
		ShipUUIDToPosData data = ShipUUIDToPosData.get(shipWorld);

		data.removeShipFromMap(wrapper);
	}

	public void removeShipNameRegistry(PhysicsWrapperEntity wrapper) {
		World shipWorld = wrapper.worldObj;
		ShipNameUUIDData data = ShipNameUUIDData.get(shipWorld);
		
		data.removeShipFromRegistry(wrapper);
	}
}
