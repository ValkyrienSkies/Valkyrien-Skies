package org.valkyrienskies.mod.common.physics.management.physo;

import static com.googlecode.cqengine.query.QueryFactory.attribute;
import static com.googlecode.cqengine.query.QueryFactory.nullableAttribute;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.experimental.Accessors;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.physmanagement.chunk.VSChunkClaim;
import org.valkyrienskies.mod.common.physmanagement.shipdata.ShipPositionData;

@Value
@With
@Accessors(fluent = false)
@Builder(toBuilder = true)
public class PhysoData {

    /**
     * The chunks claimed by this physo
     */
    VSChunkClaim chunkClaim;
    /**
     * This ships UUID
     */
    UUID uuid;
    /**
     * The (unique) name of the physo as displayed to players
     */
    @Nullable
    String name;
    /**
     * Whether or not physics are enabled on this physo
     */
    boolean isPhysicsEnabled;
    /**
     * The transform of this physo, e.g., the
     */
    ShipTransform transform;
    /**
     * The PhysicsObject which is linked with this data. This isn't serialized
     */
    @Nullable
    transient PhysicsObject physo;
    /**
     * @deprecated To be replaced by {@link #transform}
     */
    @Deprecated
    ShipPositionData positionData;

    @Deprecated
    public static PhysoDataBuilder fromWrapperEntity(PhysicsWrapperEntity entity) {
        return PhysoData.builder()
            .name(entity.getCustomNameTag())
            .physo(entity.getPhysicsObject())
            .chunkClaim(entity.getPhysicsObject().getOwnedChunks())
            .uuid(entity.getPersistentID());
    }

    public static final Attribute<PhysoData, Boolean> HAS_PHYSO = attribute(data -> data.physo != null);
    public static final Attribute<PhysoData, String> NAME = nullableAttribute(PhysoData::getName);
    public static final Attribute<PhysoData, UUID> UUID = attribute(PhysoData::getUuid);
    public static final Attribute<PhysoData, Long> CHUNKS = new MultiValueAttribute<PhysoData, Long>() {
        @Override
        public Set<Long> getValues(PhysoData physo, QueryOptions queryOptions) {
            return physo.getChunkClaim().getChunkLongs();
        }
    };
    public static final Attribute<PhysoData, Double> X_POS = attribute(data -> data.transform.getPosX());
    public static final Attribute<PhysoData, Double> Y_POS = attribute(data -> data.transform.getPosY());
    public static final Attribute<PhysoData, Double> Z_POS = attribute(data -> data.transform.getPosZ());



}
