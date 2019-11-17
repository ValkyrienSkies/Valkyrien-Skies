package org.valkyrienskies.mod.common.util.cqengine;

import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.persistence.support.ObjectSet;
import com.googlecode.cqengine.query.option.QueryOptions;

public interface UpdatableIndex<O> extends Index<O> {

	public void updateAll(ObjectSet<O> objectSet, QueryOptions queryOptions);

}
