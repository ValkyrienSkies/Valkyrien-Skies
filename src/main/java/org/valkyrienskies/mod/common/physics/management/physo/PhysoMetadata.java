package org.valkyrienskies.mod.common.physics.management.physo;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

import com.google.common.collect.ImmutableSet;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import java.util.Set;
import java.util.UUID;
import org.valkyrienskies.mod.common.physmanagement.chunk.VSChunkClaim;

public class PhysoMetadata {

    private ImmutableSet<Long> chunkLongs;
    private VSChunkClaim chunkClaim;
    private UUID uuid;
    private boolean isPhysicsEnabled;

    static final Attribute<PhysoMetadata, UUID> UUID = attribute(physoMeta -> physoMeta.uuid);
    static final Attribute<PhysoMetadata, Long> CHUNKS = new MultiValueAttribute<PhysoMetadata, Long>() {
        @Override
        public Set<Long> getValues(PhysoMetadata physoMeta, QueryOptions queryOptions) {
            return physoMeta.chunkLongs;
        }
    };

}
