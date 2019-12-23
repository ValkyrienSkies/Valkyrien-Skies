package org.valkyrienskies.mod.common.physmanagement.shipdata;

import static com.googlecode.cqengine.query.QueryFactory.equal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import org.valkyrienskies.mod.common.util.cqengine.ConcurrentUpdatableIndexedCollection;
import org.valkyrienskies.mod.common.util.cqengine.UpdatableHashIndex;
import org.valkyrienskies.mod.common.util.cqengine.UpdatableUniqueIndex;

/**
 * A class that keeps track of ship data
 */
@MethodsReturnNonnullByDefault
@Log4j2
@SuppressWarnings("WeakerAccess")
public class QueryableShipData implements Iterable<ShipData> {

    // Where every ship data instance is stored, regardless if the corresponding PhysicsObject is
    // loaded in the World or not.
    private ConcurrentUpdatableIndexedCollection<ShipData> allShips;

    public QueryableShipData() {
        this(new ConcurrentUpdatableIndexedCollection<>());
    }

    @JsonCreator // This tells Jackson to pass in allShips when serializing
    // The default thing that is passed in will be 'null' if none exists
    public QueryableShipData(
        @JsonProperty("allShips") ConcurrentUpdatableIndexedCollection<ShipData> ships) {

        if (ships == null) {
            ships = new ConcurrentUpdatableIndexedCollection<>();
        }

        this.allShips = ships;

        // For every ship data, set the 'owner' field to us -- kinda hacky but what can I do
        // I don't want to serialize a billion references to this
        // This probably only needs to be done once per world, so this is fine
        this.allShips.forEach(data -> {
            try {
                Field owner = ShipData.class.getDeclaredField("owner");
                owner.setAccessible(true);
                owner.set(data, this.allShips);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });

        this.allShips.addIndex(UpdatableHashIndex.onAttribute(ShipData.NAME));
        this.allShips.addIndex(UpdatableUniqueIndex.onAttribute(ShipData.UUID));
        this.allShips.addIndex(UpdatableUniqueIndex.onAttribute(ShipData.CHUNKS));

    }

    /**
     * @see ValkyrienUtils#getQueryableData(World)
     */
    public static QueryableShipData get(World world) {
        return ValkyrienUtils.getQueryableData(world);
    }

    /**
     * @deprecated Do not use -- thinking of better API choices
     */
    @Deprecated
    public ConcurrentUpdatableIndexedCollection<ShipData> getAllShips() {
        return allShips;
    }

    /**
     * Retrieves a list of all ships.
     */
    public ImmutableList<ShipData> getShips() {
        return ImmutableList.copyOf(allShips);
    }

    /**
     * @return All {@link ShipData} with a nonnull {@link ShipData#getPhyso()}, mapped
     * by {@link ShipData#getPhyso()}
     *
     * @see #getLoadedShips()
     */
    public ImmutableList<PhysicsObject> getLoadedPhysos() {
        return allShips.stream()
            .filter(ship -> ship.getPhyso() != null)
            .map(ShipData::getPhyso)
            .collect(ImmutableList.toImmutableList());
    }

    /**
     * @return All {@link ShipData} with a nonnull {@link ShipData#getPhyso()}
     *
     * @see #getLoadedPhysos()
     */
    public ImmutableList<ShipData> getLoadedShips() {
        return allShips.stream()
            .filter(ship -> ship.getPhyso() != null)
            .collect(ImmutableList.toImmutableList());
    }

    public Optional<ShipData> getShipFromChunk(int chunkX, int chunkZ) {
        return getShipFromChunk(ChunkPos.asLong(chunkX, chunkZ));
    }

    public Optional<ShipData> getShipFromBlock(BlockPos pos) {
        return getShipFromChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public Optional<ShipData> getShipFromChunk(long chunkLong) {
        Query<ShipData> query = equal(ShipData.CHUNKS, chunkLong);
        try (ResultSet<ShipData> resultSet = allShips.retrieve(query)) {
            if (resultSet.size() > 1) {
                throw new IllegalStateException(
                    "How the heck did we get 2 or more ships both managing the chunk at "
                        + chunkLong);
            }
            if (resultSet.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(resultSet.uniqueResult());
            }
        }
    }

    public Optional<ShipData> getShip(UUID uuid) {
        Query<ShipData> query = equal(ShipData.UUID, uuid);
        try (ResultSet<ShipData> resultSet = allShips.retrieve(query)) {
            if (resultSet.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(resultSet.uniqueResult());
            }
        }
    }

    public Optional<ShipData> getShipFromName(String name) {
        Query<ShipData> query = equal(ShipData.NAME, name);
        try (ResultSet<ShipData> shipDataResultSet = allShips.retrieve(query)) {
            if (shipDataResultSet.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(shipDataResultSet.uniqueResult());
            }
        }
    }

    public void removeShip(UUID uuid) {
        getShip(uuid).ifPresent(ship -> allShips.remove(ship));
    }

    public void removeShip(ShipData data) {
        allShips.remove(data);
    }

    public void addShip(ShipData ship) {
        System.out.println(ship.getName());
        allShips.add(ship);
    }

    /**
     * @return All loaded ships whose AABB intersects with the specified one
     */
    public ImmutableList<PhysicsObject> getNearbyLoadedShips(AxisAlignedBB toCheck) {
        return allShips.stream()
            .map(ShipData::getPhyso)
            .filter(Objects::nonNull)
            .filter(obj -> obj.getShipBB().intersects(toCheck))
            .collect(ImmutableList.toImmutableList());
    }

    /**
     * Adds the ship data if it doesn't exist, or replaces the old ship data with the new ship data,
     * while preserving the physics object attached to the old data if there was one.
     */
    public void addOrUpdateShipPreservingPhysObj(ShipData ship) {
        Optional<ShipData> old = getShip(ship.getUuid());
        if (old.isPresent()) {
            old.get().setShipTransform(ship.getShipTransform());
            old.get().setPhysInfuserPos(ship.getPhysInfuserPos());
            old.get().setShipBB(ship.getShipBB());
            old.get().setPhysicsEnabled(ship.isPhysicsEnabled());
        } else {
            this.allShips.add(ship);
        }
    }

    public void registerUpdateListener(
        BiConsumer<Iterable<ShipData>, Iterable<ShipData>> updateListener) {
        allShips.registerUpdateListener(updateListener);
    }

    /**
     * Atomically updates ShipData. It must be true that <code>!oldData.equals(newData)</code>
     *
     * @param oldData The old data object(s) to replace
     * @param newData The new data object(s)
     */
    public void updateShipData(Iterable<ShipData> oldData, Iterable<ShipData> newData) {
        this.allShips.update(oldData, newData);
    }

    @Override
    public Iterator<ShipData> iterator() {
        return allShips.iterator();
    }

    public Stream<ShipData> stream() {
        return allShips.stream();
    }
}
