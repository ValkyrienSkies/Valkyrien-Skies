package org.valkyrienskies.mod.common.physics.management.physo;

import static com.googlecode.cqengine.query.QueryFactory.attribute;
import static com.googlecode.cqengine.query.QueryFactory.nullableAttribute;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.physmanagement.chunk.VSChunkClaim;

/**
 * One of these objects will represent a ship. You can obtain a physics object for that ship (if one
 * is available), by calling {@link #getPhyso()}.
 * <p>
 * Fields not contained inside of {@link #mut} are going to be IMMUTABLE. If you need to store
 * mutable data or don't need your data to be INDEXED, store it in {@link #mut}. All methods
 * (including Getters/Setters) from {@link #mut} are automatically delegated by Lombok.
 */
@Getter
@With
@Accessors(fluent = false)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true) // For Jackson
public class ShipIndexedData {

    /**
     * The PhysicsObject which is linked with this data. This isn't serialized
     */
    @Nullable
    private transient PhysicsObject physo;

    /**
     * The {@link ShipSerializedData} that goes with this physo. This field is
     * <strong>NOT INDEXED</strong> and contains values that do not require the creation of a new
     * object when changed. This field <strong>>IS SERIALIZED</strong.
     */
    @JsonManagedReference
    @Delegate(excludes = Excludes.class)
    private final ShipSerializedData mut;

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
    @Nullable
    private final String name;

    /**
     * The transform of this physo, e.g., the position and rotation
     */
    private final ShipTransform transform;


    @Deprecated
    public static ShipIndexedDataBuilder fromWrapperEntity(PhysicsWrapperEntity entity) {
        return ShipIndexedData.builder()
            .name(entity.getCustomNameTag())
            .physo(entity.getPhysicsObject())
            .chunkClaim(entity.getPhysicsObject().getOwnedChunks())
            .uuid(entity.getPersistentID());
    }

    public static final Attribute<ShipIndexedData, Boolean> HAS_PHYSO = attribute(
        data -> data.physo != null);
    public static final Attribute<ShipIndexedData, String> NAME = nullableAttribute(
        ShipIndexedData::getName);
    public static final Attribute<ShipIndexedData, UUID> UUID = attribute(ShipIndexedData::getUuid);
    public static final Attribute<ShipIndexedData, Long> CHUNKS = new MultiValueAttribute<ShipIndexedData, Long>() {
        @Override
        public Set<Long> getValues(ShipIndexedData physo, QueryOptions queryOptions) {
            return physo.getChunkClaim().getChunkLongs();
        }
    };
    public static final Attribute<ShipIndexedData, Double> X_POS = attribute(
        data -> data.transform.getPosX());
    public static final Attribute<ShipIndexedData, Double> Y_POS = attribute(
        data -> data.transform.getPosY());
    public static final Attribute<ShipIndexedData, Double> Z_POS = attribute(
        data -> data.transform.getPosZ());


    private interface Excludes {

        ShipIndexedData getIndexed();
    }
}

