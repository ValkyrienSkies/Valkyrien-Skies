package org.valkyrienskies.mod.common.physics.management.physo;

import static com.googlecode.cqengine.query.QueryFactory.attribute;
import static com.googlecode.cqengine.query.QueryFactory.nullableAttribute;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.physmanagement.chunk.VSChunkClaim;
import org.valkyrienskies.mod.common.physmanagement.shipdata.IBlockPosSet;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.SmallBlockPosSet;
import org.valkyrienskies.mod.common.util.cqengine.ConcurrentUpdatableIndexedCollection;
import org.valkyrienskies.mod.common.util.jackson.annotations.PacketIgnore;

/**
 * One of these objects will represent a ship. You can obtain a physics object for that ship (if one
 * is available), by calling {@link #getPhyso()}.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true) // For Jackson
public class ShipData {

    /**
     * The PhysicsObject which is linked with this data. This isn't serialized
     */
    @Nullable
    @Setter
    private transient PhysicsObject physo;

    /**
     * The {@link QueryableShipData} that manages this
     */
    @Getter(AccessLevel.NONE)
    private final transient ConcurrentUpdatableIndexedCollection<ShipData> owner;

    // region Data Fields

    /**
     * Physics information -- mutable but final. References to this <strong>should be guaranteed to
     * never change</strong> for the duration of a game.
     */
    private final ShipPhysicsData physicsData;

    private final ShipInertiaData inertiaData;

    /**
     * Has to be concurrent, only exists properly on the server. Do not use this for anything client
     * side! Contains all of the non-air block positions on the ship. This is used for generating
     * AABBs and deconstructing the ship.
     */
    @PacketIgnore
    @Nullable
    @JsonSerialize(as = SmallBlockPosSet.class)
    @JsonDeserialize(as = SmallBlockPosSet.class)
    IBlockPosSet blockPositions;

    @Setter
    private ShipTransform shipTransform;

    @Setter
    private AxisAlignedBB shipBB;

    /**
     * Whether or not physics are enabled on this physo
     */
    @Setter
    private boolean physicsEnabled;

    /**
     * The position of the physics infuser this ship has.
     */
    @Setter
    private BlockPos physInfuserPos;

    /**
     * The chunks claimed by this physo
     */
    private final VSChunkClaim chunkClaim;

    /**
     * This ships UUID
     */
    private final UUID uuid;

    /**
     * The (unique) name of the physo as displayed to players
     */
    private String name;

    // endregion

    public ShipData(@Nullable PhysicsObject physo,
                    @NonNull ConcurrentUpdatableIndexedCollection<ShipData> owner,
                    ShipPhysicsData physicsData, @Nonnull ShipInertiaData inertiaData, @NonNull ShipTransform shipTransform, @NonNull AxisAlignedBB shipBB,
                    boolean physicsEnabled, @NonNull BlockPos physInfuserPos, @NonNull VSChunkClaim chunkClaim, @NonNull UUID uuid,
                    @NonNull String name) {
        this.physo = physo;
        this.owner = owner;
        this.physicsData = physicsData;
        this.inertiaData = inertiaData;
        this.shipTransform = shipTransform;
        this.shipBB = shipBB;
        this.physicsEnabled = physicsEnabled;
        this.physInfuserPos = physInfuserPos;
        this.chunkClaim = chunkClaim;
        this.uuid = uuid;
        this.name = name;
        this.blockPositions = new SmallBlockPosSet(chunkClaim.getCenterX() * 16,
            chunkClaim.getCenterZ() * 16);
    }

    public static ShipData createData(ConcurrentUpdatableIndexedCollection<ShipData> owner,
        String name, VSChunkClaim chunkClaim, UUID shipID,
        ShipTransform shipTransform,
        AxisAlignedBB aabb, BlockPos physInfuserPos) {

        return new ShipData(null, owner, new ShipPhysicsData(), new ShipInertiaData(), shipTransform, aabb,
            true, physInfuserPos, chunkClaim, shipID, name);
    }

    // region Setters

    public ShipData setName(String name) {
        this.name = name;
        owner.updateObjectIndices(this, NAME);
        return this;
    }

    // endregion

    // region Attributes

    public static final Attribute<ShipData, String> NAME = nullableAttribute(ShipData::getName);
    public static final Attribute<ShipData, UUID> UUID = attribute(ShipData::getUuid);
    public static final Attribute<ShipData, Long> CHUNKS = new MultiValueAttribute<ShipData, Long>() {
        @Override
        public Set<Long> getValues(ShipData physo, QueryOptions queryOptions) {
            return physo.getChunkClaim().getChunkLongs();
        }
    };

    // endregion
}

