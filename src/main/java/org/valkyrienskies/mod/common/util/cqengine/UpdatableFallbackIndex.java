package org.valkyrienskies.mod.common.util.cqengine;

import com.googlecode.cqengine.index.fallback.FallbackIndex;
import com.googlecode.cqengine.persistence.support.ObjectSet;
import com.googlecode.cqengine.query.option.QueryOptions;

public class UpdatableFallbackIndex<O> extends FallbackIndex<O> implements UpdatableIndex<O> {

    /**
     * No-op
     */
    @Override
    public void updateAll(ObjectSet<O> objectSet, QueryOptions queryOptions) {
        // No-op
    }

}
