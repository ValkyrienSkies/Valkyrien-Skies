package org.valkyrienskies.mod.common.util.cqengine;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.engine.QueryEngineInternal;
import com.googlecode.cqengine.persistence.support.ObjectSet;
import com.googlecode.cqengine.query.option.QueryOptions;

public interface UpdatableQueryEngine<O> extends QueryEngineInternal<O> {

    public void updateAll(final ObjectSet<O> objectSet, final Attribute<O, ?> onAttribute,
        final QueryOptions queryOptions);

}
