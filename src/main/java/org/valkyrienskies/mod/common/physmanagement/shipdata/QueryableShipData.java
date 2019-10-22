package org.valkyrienskies.mod.common.physmanagement.shipdata;

import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.startsWith;

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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.physics.management.physo.ShipIndexedData;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

/**
 * A class that keeps track of ship data
 */
@MethodsReturnNonnullByDefault
@Log4j2
@SuppressWarnings("WeakerAccess")
public class QueryableShipData implements Iterable<ShipIndexedData> {

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
    private ConcurrentIndexedCollection<ShipIndexedData> allShips = new ConcurrentIndexedCollection<>();

    public QueryableShipData() {
        allShips.addIndex(HashIndex.onAttribute(ShipIndexedData.NAME));
        allShips.addIndex(UniqueIndex.onAttribute(ShipIndexedData.UUID));
        allShips.addIndex(UniqueIndex.onAttribute(ShipIndexedData.CHUNKS));
    }

    /**
     * @see ValkyrienUtils#getQueryableData(World)
     */
    public static QueryableShipData get(World world) {
        return ValkyrienUtils.getQueryableData(world);
    }

    /**
     * @param oldData    The ship to be renamed
     * @param newName The new name of the ship
     * @return True of the rename was successful, false if it wasn't.
     */
    public boolean renameShip(ShipIndexedData oldData, String newName) {
        Query<ShipIndexedData> query = equal(ShipIndexedData.NAME, newName);
        if (allShips.retrieve(query).isEmpty()) {
            ShipIndexedData newData = oldData.withName(newName);

            allShips.remove(oldData);
            allShips.add(newData);

            return true;
        }
        return false;
    }

    public Stream<ShipIndexedData> getShipsFromNameStartingWith(String startsWith) {
        Query<ShipIndexedData> query = startsWith(ShipIndexedData.NAME, startsWith);

        return allShips.retrieve(query).stream();
    }

    /**
     * Retrieves a list of all ships.
     */
    public List<ShipIndexedData> getShips() {
        return ImmutableList.copyOf(allShips);
    }

    public Optional<ShipIndexedData> getShipFromChunk(int chunkX, int chunkZ) {
        return getShipFromChunk(ChunkPos.asLong(chunkX, chunkZ));
    }

    public Optional<ShipIndexedData> getShipFromChunk(long chunkLong) {
        Query<ShipIndexedData> query = equal(ShipIndexedData.CHUNKS, chunkLong);
        ResultSet<ShipIndexedData> resultSet = allShips.retrieve(query);

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

    public Optional<ShipIndexedData> getShip(UUID uuid) {
        Query<ShipIndexedData> query = equal(ShipIndexedData.UUID, uuid);
        ResultSet<ShipIndexedData> resultSet = allShips.retrieve(query);

        if (resultSet.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(resultSet.uniqueResult());
        }
    }

    public Optional<ShipIndexedData> getShip(PhysicsWrapperEntity wrapperEntity) {
        return getShip(wrapperEntity.getPersistentID());
    }

    public ShipIndexedData getOrCreateShip(PhysicsWrapperEntity wrapperEntity) {
        Optional<ShipIndexedData> data = getShip(wrapperEntity.getPersistentID());
        return data.orElseGet(() -> {
            ShipIndexedData shipData = ShipIndexedData.fromWrapperEntity(wrapperEntity).build();
            allShips.add(shipData);
            return shipData;
        });
    }

    public Optional<ShipIndexedData> getShipFromName(String name) {
        Query<ShipIndexedData> query = equal(ShipIndexedData.NAME, name);
        ResultSet<ShipIndexedData> shipDataResultSet = allShips.retrieve(query);

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
        getShip(uuid).ifPresent(ship -> allShips.remove(ship));
    }

    public void removeShip(ShipIndexedData data) {
        allShips.remove(data);
    }

    public void addShip(ShipIndexedData ship) {
        allShips.add(ship);
    }

    @Deprecated
    public void addShip(PhysicsWrapperEntity wrapperEntity) {
        Query<ShipIndexedData> query = equal(ShipIndexedData.UUID, wrapperEntity.getPersistentID());

        // If this ship is already added, don't add it again?
        if (allShips.retrieve(query).isEmpty()) {
            addShip(ShipIndexedData.fromWrapperEntity(wrapperEntity).build());
        }
    }

    // TODO: this isn't threadsafe? Use TransactionalIndexedCollection
    public void updateShip(ShipIndexedData oldData, ShipIndexedData newData) {
        allShips.remove(oldData);
        allShips.add(newData);
    }

    public void updateShipPosition(PhysicsWrapperEntity wrapper) {
        getOrCreateShip(wrapper).setPositionData(new ShipPositionData(wrapper));
    }

    @Override
    public Iterator<ShipIndexedData> iterator() {
        return allShips.iterator();
    }

    public Stream<ShipIndexedData> stream() {
        return allShips.stream();
    }
}
