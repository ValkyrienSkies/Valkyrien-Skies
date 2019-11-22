package org.valkyrienskies.mod.common.util.cqengine;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.support.Factory;
import com.googlecode.cqengine.persistence.support.ObjectSet;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.stored.StoredResultSet;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UpdatableHashIndex<A, O> extends HashIndex<A, O> implements UpdatableIndex<O> {

    ConcurrentMap<O, Iterable<A>> reverseMap;

    /**
     * {@inheritDoc}
     */
    protected UpdatableHashIndex(Factory<ConcurrentMap<A, StoredResultSet<O>>> indexMapFactory,
                                 Factory<StoredResultSet<O>> valueSetFactory,
                                 Factory<ConcurrentMap<O, Iterable<A>>> reverseMapFactory,
                                 Attribute<O, A> attribute) {

        super(indexMapFactory, valueSetFactory, attribute);
        reverseMap = reverseMapFactory.create();
    }

    @Override
    public boolean addAll(ObjectSet<O> objectSet, QueryOptions queryOptions) {
        for (O object : objectSet) {
            Iterable<A> attributeValues = getAttribute().getValues(object, queryOptions);
            reverseMap.put(object, attributeValues);
        }

        return super.addAll(objectSet, queryOptions);
    }

    @Override
    public boolean removeAll(ObjectSet<O> objectSet, QueryOptions queryOptions) {
        try {
            boolean modified = false;
            ConcurrentMap<A, StoredResultSet<O>> indexMap = this.indexMap;
            for (O object : objectSet) {
                Iterable<A> attributeValues = reverseMap.getOrDefault(object, Collections.emptyList());
                for (A attributeValue : attributeValues) {
                    // Replace attributeValue with quantized value if applicable...
                    attributeValue = getQuantizedValue(attributeValue);

                    StoredResultSet<O> valueSet = indexMap.get(attributeValue);
                    if (valueSet == null) {
                        continue;
                    }
                    modified |= valueSet.remove(object);
                    if (valueSet.isEmpty()) {
                        indexMap.remove(attributeValue);
                    }
                }
                // Remove from reverseMap
                reverseMap.remove(object);
            }
            return modified;
        }
        finally {
            objectSet.close();
        }
    }

    @Override
    public void updateAll(ObjectSet<O> objectSet, QueryOptions queryOptions) {
        try {
            for (O object : objectSet) {
                Iterable<A> oldAttributeValues = reverseMap.get(object);
                for (A oldAttributeValue : oldAttributeValues) {
                    StoredResultSet<O> oldAttribute = super.indexMap.get(oldAttributeValue);
                    oldAttribute.remove(object);
                }

                Iterable<A> newAttributeValues = getAttribute().getValues(object, queryOptions);
                for (A newAttributeValue : newAttributeValues) {
                    StoredResultSet<O> newAttribute = super.indexMap.computeIfAbsent(newAttributeValue, k -> valueSetFactory.create());
                    newAttribute.add(object);
                }
                reverseMap.put(object, newAttributeValues);
            }
        } finally {
            objectSet.close();
        }
    }

    /**
     * Creates an index map using default settings.
     */
    public static class DefaultReverseMapFactory<O, A> implements Factory<ConcurrentMap<O, A>> {
        @Override
        public ConcurrentMap<O, A> create() {
            return new ConcurrentHashMap<O, A>();
        }
    }

    // ---------- Static factory methods to create UpdatableUniqueIndexes ----------

    /**
     * Creates a new {@link UpdatableHashIndex} on the specified attribute.
     * <p/>
     *
     * @param attribute The attribute on which the index will be built
     * @param <O>       The type of the object containing the attribute
     * @return A {@link UpdatableHashIndex} on this attribute
     */
    public static <A, O> UpdatableHashIndex<A, O> onAttribute(Attribute<O, A> attribute) {
        return onAttribute(new DefaultIndexMapFactory<A, O>(), attribute);
    }

    /**
     * Creates a new {@link UpdatableHashIndex} on the specified attribute.
     * <p/>
     * @param indexMapFactory A factory used to create the main map-based data structure used by the index
     * @param valueSetFactory A factory used to create sets to store values in the index
     * @param attribute The attribute on which the index will be built
     * @param <O> The type of the object containing the attribute
     * @return A {@link UpdatableHashIndex} on this attribute
     */
    public static <A, O> UpdatableHashIndex<A, O> onAttribute(
            Factory<ConcurrentMap<A, StoredResultSet<O>>> indexMapFactory,
            Factory<StoredResultSet<O>> valueSetFactory,
            Attribute<O, A> attribute) {
        return new UpdatableHashIndex<A, O>(indexMapFactory, valueSetFactory,
                new DefaultReverseMapFactory<O, Iterable<A>>(), attribute);
    }

    /**
     * Creates a new {@link UpdatableHashIndex} on the specified attribute.
     * <p/>
     * @param indexMapFactory A factory used to create the main map-based data structure used by the index
     * @param attribute The attribute on which the index will be built
     * @param <O> The type of the object containing the attribute
     * @return A {@link UpdatableHashIndex} on this attribute
     */
    public static <A, O> UpdatableHashIndex<A, O> onAttribute(
            Factory<ConcurrentMap<A, StoredResultSet<O>>> indexMapFactory,
            Attribute<O, A> attribute) {
        return onAttribute(indexMapFactory, new DefaultValueSetFactory<O>(),
                new DefaultReverseMapFactory<O, Iterable<A>>(), attribute);
    }

    /**
     * Creates a new {@link UpdatableHashIndex} on the specified attribute.
     * <p/>
     * @param indexMapFactory A factory used to create the main map-based data structure used by the index
     * @param valueSetFactory A factory used to create sets to store values in the index
     * @param reverseMapFactory A factory used to create the object-attribute mapping data structure used by the index
     * @param attribute The attribute on which the index will be built
     * @param <O> The type of the object containing the attribute
     * @return A {@link UpdatableHashIndex} on this attribute
     */
    public static <A, O> UpdatableHashIndex<A, O> onAttribute(
        Factory<ConcurrentMap<A, StoredResultSet<O>>> indexMapFactory,
                                                              Factory<StoredResultSet<O>> valueSetFactory,
                                                              Factory<ConcurrentMap<O, Iterable<A>>> reverseMapFactory,
                                                              Attribute<O, A> attribute) {
        return new UpdatableHashIndex<A, O>(indexMapFactory, valueSetFactory, reverseMapFactory, attribute);
    }

}
