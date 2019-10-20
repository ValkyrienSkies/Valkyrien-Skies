package org.valkyrienskies.mod.common.physmanagement.shipdata;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

import com.google.common.collect.ImmutableSet;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.util.math.ChunkPos;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.PhysoData;
import org.valkyrienskies.mod.common.physmanagement.chunk.VSChunkClaim;

/**
 * @deprecated Use {@link PhysoData}
 */
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public final class ShipData {

    // WARNING: Mutable! This field is NEVER indexed. DO NOT INDEX!
    @Nullable
    public ShipPositionData positionData;
    private String name;
    private UUID uuid;
    /**
     * Unmodifiable set
     */
    private Set<Long> chunkLongs;
    private VSChunkClaim chunkClaim;

    public static class Builder {

        private ShipData shipData;

        public Builder(ShipData data) {
            shipData = data;
        }

        public Builder(PhysicsWrapperEntity wrapperEntity) {
            shipData = new ShipData();
            shipData.name = wrapperEntity.getCustomNameTag();
            shipData.uuid = wrapperEntity.getPersistentID();
            shipData.chunkLongs = ImmutableSet
                .copyOf(getChunkLongs(wrapperEntity.getPhysicsObject()));
            shipData.chunkClaim = wrapperEntity.getPhysicsObject().getOwnedChunks();
        }

        public Builder(PhysicsObject physo) {
            shipData = new ShipData();
            //shipData.uuid = .getPersistentID();
            shipData.chunkLongs = ImmutableSet.copyOf(getChunkLongs(physo));
            shipData.chunkClaim = physo.getOwnedChunks();
        }

        public Builder() {
            shipData = new ShipData();
        }

        public ShipData build() {
            return shipData;
        }

        public Builder setName(String name) {
            shipData.name = name;
            return this;
        }

        public Builder setUUID(UUID uuid) {
            shipData.uuid = uuid;
            return this;
        }

        public Builder setChunkLongs(Set<Long> chunkLongs) {
            shipData.chunkLongs = Collections.unmodifiableSet(chunkLongs);
            return this;
        }

        public Builder setChunkClaim(VSChunkClaim chunkClaim) {
            shipData.chunkClaim = chunkClaim;
            return this;
        }

        /**
         * @return Every Chunk that this physo owns/claims represented as a long; for indexing
         * purposes
         */
        private static Set<Long> getChunkLongs(PhysicsObject physo) {
            Set<Long> chunkLongs = new HashSet<>();
            VSChunkClaim ownedChunks = physo.getOwnedChunks();

            int centerX = ownedChunks.getCenterX();
            int centerZ = ownedChunks.getCenterZ();
            int radius = ownedChunks.getRadius();

            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    long chunkPos = ChunkPos.asLong(x, z);
                    chunkLongs.add(chunkPos);
                }
            }

            return chunkLongs;
        }
    }

    @Nullable
    public ShipPositionData getPositionData() {
        return positionData;
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Set<Long> getChunkLongs() {
        return chunkLongs;
    }

    public VSChunkClaim getChunkClaim() {
        return chunkClaim;
    }

    static final Attribute<ShipData, String> NAME = attribute(ship -> ship.name);
    static final Attribute<ShipData, UUID> UUID = attribute(ship -> ship.uuid);
    static final Attribute<ShipData, Long> CHUNKS = new MultiValueAttribute<ShipData, Long>() {
        @Override
        public Set<Long> getValues(ShipData ship, QueryOptions queryOptions) {
            return ship.chunkLongs;
        }
    };

}
