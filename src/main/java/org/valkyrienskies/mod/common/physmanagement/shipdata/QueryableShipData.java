package org.valkyrienskies.mod.common.physmanagement.shipdata;

import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.startsWith;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

/**
 * A class that keeps track of ship data
 */
@MethodsReturnNonnullByDefault
@Log4j2
@SuppressWarnings("WeakerAccess")
public class QueryableShipData implements Iterable<ShipData> {

    /**
     * The key used to store/read the allShips collection from Kryo
     */
    private static final String NBT_KEY_KRYO = ValkyrienSkiesMod.MOD_ID + "QueryableShipDataNBT";
    /**
     * The key used to store/read allShips from jackson protobuf
     */
    private static final String NBT_KEY_JACKSON_PROTOBUF = ValkyrienSkiesMod.MOD_ID +
        "QueryableShipDataNBT-JacksonProtobuf";

    // Where every ship data instance is stored, regardless if the corresponding PhysicsObject is
    // loaded in the World or not.
    private ConcurrentIndexedCollection<ShipData> allShips = new ConcurrentIndexedCollection<>();

    public QueryableShipData() {
        allShips.addIndex(HashIndex.onAttribute(ShipData.NAME));
        allShips.addIndex(UniqueIndex.onAttribute(ShipData.UUID));
        allShips.addIndex(UniqueIndex.onAttribute(ShipData.CHUNKS));
    }

    /**
     * @see ValkyrienUtils#getQueryableData(World)
     */
    public static QueryableShipData get(World world) {
        return ValkyrienUtils.getQueryableData(world);
    }

    /**
     * @param data    The ship to be renamed
     * @param newName The new name of the ship
     * @return True of the rename was successful, false if it wasn't.
     */
    public boolean renameShip(ShipData data, String newName) {
        Query<ShipData> query = equal(ShipData.NAME, newName);
        if (allShips.retrieve(query).isEmpty()) {
            ShipData newData = new ShipData.Builder(data)
                .setName(newName)
                .build();

            allShips.remove(data);
            allShips.add(newData);

            return true;
        }
        return false;
    }

    public Stream<ShipData> getShipsFromNameStartingWith(String startsWith) {
        Query<ShipData> query = startsWith(ShipData.NAME, startsWith);

        return allShips.retrieve(query).stream();
    }

    /**
     * Retrieves a list of all ships.
     */
    public List<ShipData> getShips() {
        return ImmutableList.copyOf(allShips);
    }

    public Optional<ShipData> getShipFromChunk(int chunkX, int chunkZ) {
        return getShipFromChunk(ChunkPos.asLong(chunkX, chunkZ));
    }

    public Optional<ShipData> getShipFromChunk(long chunkLong) {
        Query<ShipData> query = equal(ShipData.CHUNKS, chunkLong);
        ResultSet<ShipData> resultSet = allShips.retrieve(query);

        if (resultSet.size() > 1) {
            throw new IllegalStateException(
                "How the heck did we get 2 or more ships both managing the chunk at " + chunkLong);
        }
        if (resultSet.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(resultSet.uniqueResult());
        }
    }

    public Optional<ShipData> getShip(UUID uuid) {
        Query<ShipData> query = equal(ShipData.UUID, uuid);
        ResultSet<ShipData> resultSet = allShips.retrieve(query);

        if (resultSet.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(resultSet.uniqueResult());
        }
    }

    public Optional<ShipData> getShip(PhysicsWrapperEntity wrapperEntity) {
        return getShip(wrapperEntity.getPersistentID());
    }

    public ShipData getOrCreateShip(PhysicsWrapperEntity wrapperEntity) {
        Optional<ShipData> data = getShip(wrapperEntity.getPersistentID());
        return data.orElseGet(() -> {
            ShipData shipData = new ShipData.Builder(wrapperEntity).build();
            allShips.add(shipData);
            return shipData;
        });
    }

    public Optional<ShipData> getShipFromName(String name) {
        Query<ShipData> query = equal(ShipData.NAME, name);
        ResultSet<ShipData> shipDataResultSet = allShips.retrieve(query);

        if (shipDataResultSet.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(shipDataResultSet.uniqueResult());
        }
    }

    public void removeShip(PhysicsWrapperEntity wrapper) {
        removeShip(wrapper.getPersistentID());
    }

    public void removeShip(UUID uuid) {
        Optional<ShipData> shipOptional = getShip(uuid);

        shipOptional.ifPresent(ship -> allShips.remove(ship));
    }

    public void addShip(ShipData ship) {
        allShips.add(ship);
    }

    public void addShip(PhysicsWrapperEntity wrapperEntity) {
        Query<ShipData> query = equal(ShipData.UUID, wrapperEntity.getPersistentID());

        // If this ship is already added, don't add it again?
        if (allShips.retrieve(query).isEmpty()) {
            addShip(new ShipData.Builder(wrapperEntity).build());
        }
    }

    public void updateShipPosition(PhysicsWrapperEntity wrapper) {
        ShipData shipData = getOrCreateShip(wrapper);
        if (shipData.positionData == null) {
            shipData.positionData = new ShipPositionData(wrapper);
        }
        shipData.positionData.updateData(wrapper);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        long start = System.currentTimeMillis();

        try {
            ObjectMapper mapper = new ObjectMapper();

            mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(Visibility.ANY)
                .withGetterVisibility(Visibility.NONE)
                .withIsGetterVisibility(Visibility.NONE)
                .withSetterVisibility(Visibility.NONE));

            byte[] value = nbt.getByteArray(NBT_KEY_JACKSON_PROTOBUF);
            if (value.length != 0) {
                this.allShips.clear();
                this.allShips.addAll(mapper.readValue(
                    value, ShipDataCollectionWrapper.class).collection);
            } else {
                allShips = new ConcurrentIndexedCollection<>();
            }
        } catch (Exception e) {
            // Error reading allShips from memory, just make a new empty one.
            e.printStackTrace();
            allShips = new ConcurrentIndexedCollection<>();
        }

        if (allShips == null) {
            // This should NEVER EVER happen! So I don't feel bad crashing the game, for now.
            throw new IllegalStateException(
                "Kryo read allships as null! Making a new empty allships instance");
        }

        System.out.println("Price of read: " + (System.currentTimeMillis() - start) + "ms");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        long start = System.currentTimeMillis();

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(Visibility.ANY)
                .withGetterVisibility(Visibility.NONE)
                .withIsGetterVisibility(Visibility.NONE)
                .withSetterVisibility(Visibility.NONE));

            byte[] value = mapper.writeValueAsBytes(
                new ShipDataCollectionWrapper(allShips));
            compound.setByteArray(NBT_KEY_JACKSON_PROTOBUF, value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("Price of write: " + (System.currentTimeMillis() - start) + "ms");

        return compound;
    }

    @Override
    public Iterator<ShipData> iterator() {
        return allShips.iterator();
    }

    public Stream<ShipData> stream() {
        return allShips.stream();
    }
}
