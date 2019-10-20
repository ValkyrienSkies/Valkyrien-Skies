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
import org.valkyrienskies.mod.common.physics.management.physo.PhysoData;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

/**
 * A class that keeps track of ship data
 */
@MethodsReturnNonnullByDefault
@Log4j2
@SuppressWarnings("WeakerAccess")
public class QueryableShipData implements Iterable<PhysoData> {

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
    private ConcurrentIndexedCollection<PhysoData> allShips = new ConcurrentIndexedCollection<>();

    public QueryableShipData() {
        allShips.addIndex(HashIndex.onAttribute(PhysoData.NAME));
        allShips.addIndex(UniqueIndex.onAttribute(PhysoData.UUID));
        allShips.addIndex(UniqueIndex.onAttribute(PhysoData.CHUNKS));
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
    public boolean renameShip(PhysoData oldData, String newName) {
        Query<PhysoData> query = equal(PhysoData.NAME, newName);
        if (allShips.retrieve(query).isEmpty()) {
            PhysoData newData = oldData.withName(newName);

            allShips.remove(oldData);
            allShips.add(newData);

            return true;
        }
        return false;
    }

    public Stream<PhysoData> getShipsFromNameStartingWith(String startsWith) {
        Query<PhysoData> query = startsWith(PhysoData.NAME, startsWith);

        return allShips.retrieve(query).stream();
    }

    /**
     * Retrieves a list of all ships.
     */
    public List<PhysoData> getShips() {
        return ImmutableList.copyOf(allShips);
    }

    public Optional<PhysoData> getShipFromChunk(int chunkX, int chunkZ) {
        return getShipFromChunk(ChunkPos.asLong(chunkX, chunkZ));
    }

    public Optional<PhysoData> getShipFromChunk(long chunkLong) {
        Query<PhysoData> query = equal(PhysoData.CHUNKS, chunkLong);
        ResultSet<PhysoData> resultSet = allShips.retrieve(query);

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

    public Optional<PhysoData> getShip(UUID uuid) {
        Query<PhysoData> query = equal(PhysoData.UUID, uuid);
        ResultSet<PhysoData> resultSet = allShips.retrieve(query);

        if (resultSet.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(resultSet.uniqueResult());
        }
    }

    public Optional<PhysoData> getShip(PhysicsWrapperEntity wrapperEntity) {
        return getShip(wrapperEntity.getPersistentID());
    }

    public PhysoData getOrCreateShip(PhysicsWrapperEntity wrapperEntity) {
        Optional<PhysoData> data = getShip(wrapperEntity.getPersistentID());
        return data.orElseGet(() -> {
            PhysoData shipData = PhysoData.fromWrapperEntity(wrapperEntity).build();
            allShips.add(shipData);
            return shipData;
        });
    }

    public Optional<PhysoData> getShipFromName(String name) {
        Query<PhysoData> query = equal(PhysoData.NAME, name);
        ResultSet<PhysoData> shipDataResultSet = allShips.retrieve(query);

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

    public void addShip(PhysoData ship) {
        allShips.add(ship);
    }

    @Deprecated
    public void addShip(PhysicsWrapperEntity wrapperEntity) {
        Query<PhysoData> query = equal(PhysoData.UUID, wrapperEntity.getPersistentID());

        // If this ship is already added, don't add it again?
        if (allShips.retrieve(query).isEmpty()) {
            addShip(PhysoData.fromWrapperEntity(wrapperEntity).build());
        }
    }

    public void updateShipPosition(PhysicsWrapperEntity wrapper) {
        PhysoData shipData = getOrCreateShip(wrapper);
        PhysoData newData = shipData.withPositionData(new ShipPositionData(wrapper));

        // TODO: this isn't threadsafe? Use TransactionalIndexedCollection
        allShips.remove(shipData);
        allShips.add(newData);
    }

    @Override
    public Iterator<PhysoData> iterator() {
        return allShips.iterator();
    }

    public Stream<PhysoData> stream() {
        return allShips.stream();
    }
}
