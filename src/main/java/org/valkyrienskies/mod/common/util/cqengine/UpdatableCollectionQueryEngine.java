package org.valkyrienskies.mod.common.util.cqengine;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.engine.CollectionQueryEngine;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.compound.CompoundIndex;
import com.googlecode.cqengine.index.compound.support.CompoundAttribute;
import com.googlecode.cqengine.persistence.support.ObjectSet;
import com.googlecode.cqengine.query.option.QueryOptions;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class UpdatableCollectionQueryEngine<O> extends CollectionQueryEngine<O> implements UpdatableQueryEngine<O> {

	private ConcurrentMap<CompoundAttribute<O>, CompoundIndex<O>> compoundIndexes;
	private ConcurrentMap<Attribute<O, ?>, Set<UpdatableIndex<O>>> attributeIndexes;
	private UpdatableFallbackIndex<O> fallbackIndex = new UpdatableFallbackIndex<>();

	@Override
	public void updateAll(
        ObjectSet<O> objectSet, Attribute<O, ?> onAttribute, QueryOptions queryOptions) {
		forEachIndexOnAttributeDo(index -> {
			UpdatableIndex<O> updatableIndex = (UpdatableIndex<O>) index;
			updatableIndex.updateAll(objectSet, queryOptions);
			return true;
		}, onAttribute);
	}

	void forEachIndexOnAttributeDo(IndexOperation<O> indexOperation, Attribute<O, ?> attribute) {
		// Perform the operation if the attribute is a compound index
		if (attribute instanceof CompoundAttribute) {
			CompoundIndex<O> compoundIndex = this.getCompoundIndexes().get(attribute);
			indexOperation.perform(compoundIndex);
		} else {
			// Perform the operation on all attribute indexes...
			Set<UpdatableIndex<O>> indexesForAttribute = this.getAttributeIndexes().get(attribute);

			for (Index<O> index : indexesForAttribute) {
				boolean continueIterating = indexOperation.perform(index);
				if (!continueIterating) {
					return;
				}
			}
		}

		// Perform the operation on the fallback index...
		indexOperation.perform(this.fallbackIndex);
	}

	// Ugly reflection code below for accessing private fields

	@SuppressWarnings("unchecked")
	private ConcurrentMap<Attribute<O, ?>, Set<UpdatableIndex<O>>> getAttributeIndexes() {
		try {
			if (attributeIndexes == null) {
				Field attributeIndexes = CollectionQueryEngine.class.getDeclaredField("attributeIndexes");
				attributeIndexes.setAccessible(true);
				this.attributeIndexes = (ConcurrentMap<Attribute<O, ?>, Set<UpdatableIndex<O>>>) attributeIndexes.get(this);
			}
			return this.attributeIndexes;
		} catch (IllegalAccessException | NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private ConcurrentMap<CompoundAttribute<O>, CompoundIndex<O>> getCompoundIndexes() {
		try {
			if (this.compoundIndexes == null) {
				Field compoundIndexesField = CollectionQueryEngine.class.getDeclaredField("compoundIndexes");
				compoundIndexesField.setAccessible(true);
				this.compoundIndexes = (ConcurrentMap<CompoundAttribute<O>, CompoundIndex<O>>) compoundIndexesField.get(this);
			}
			return this.compoundIndexes;
		} catch (IllegalAccessException | NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}
	}

	interface IndexOperation<O> {
		/**
		 * @param index The index to be processed
		 * @return Operation can return true to continue iterating through all indexes, false to stop iterating
		 */
		boolean perform(Index<O> index);
	}
}
