package org.valkyrienskies.mod.common.util.cqengine;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.support.Factory;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.persistence.support.ObjectSet;
import com.googlecode.cqengine.query.option.QueryOptions;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UpdatableUniqueIndex<A, O> extends UniqueIndex<A, O> implements UpdatableIndex<O> {

    ConcurrentMap<O, A> reverseMap;

    /**
     * {@inheritDoc}
     */
    protected UpdatableUniqueIndex(Factory<ConcurrentMap<A, O>> indexMapFactory,
                                   Factory<ConcurrentMap<O, A>> reverseMapFactory,
                                   Attribute<O, A> attribute) {

        super(indexMapFactory, attribute);
        reverseMap = reverseMapFactory.create();
    }


    @Override
    public boolean addAll(ObjectSet<O> objectSet, QueryOptions queryOptions) {
        for (O object : objectSet) {
            Iterable<A> attributeValues = getAttribute().getValues(object, queryOptions);
            for (A attributeValue : attributeValues) {
                reverseMap.put(object, attributeValue);
            }
        }

        return super.addAll(objectSet, queryOptions);
    }

    @Override
    public boolean removeAll(ObjectSet<O> objectSet, QueryOptions queryOptions) {
        for (O object : objectSet) {
            reverseMap.remove(object);
        }

        return super.removeAll(objectSet, queryOptions);
    }

    @Override
    public void updateAll(ObjectSet<O> objectSet, QueryOptions queryOptions) {
        try {
            for (O object : objectSet) {
                A oldAttributeValue = reverseMap.get(object);
                if (oldAttributeValue == null) { // The value is not in the collection
                    super.addAll(objectSet, queryOptions);
                    continue;
                }
                // Since this is a unique index, we're guaranteed that this is a one item iterator
                Iterable<A> attributeValues = getAttribute().getValues(object, queryOptions);
                for (A newAttributeValue : attributeValues) {
                    super.indexMap.remove(oldAttributeValue);
                    O existingValue = indexMap.put(newAttributeValue, object);
                    if (existingValue != null && !existingValue.equals(object)) {
                        throw new UniqueConstraintViolatedException(
                                "The application has attempted to add a duplicate object to the UniqueIndex on attribute '"
                                        + attribute.getAttributeName() +
                                        "', potentially causing inconsistencies between indexes. " +
                                        "UniqueIndex should not be used with attributes which do not uniquely identify objects. " +
                                        "Problematic attribute value: '" + newAttributeValue + "', " +
                                        "problematic duplicate object: " + object);
                    }
                }
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
     * Creates a new {@link UpdatableUniqueIndex} on the specified attribute.
     * <p/>
     *
     * @param attribute The attribute on which the index will be built
     * @param <O>       The type of the object containing the attribute
     * @return A {@link UpdatableUniqueIndex} on this attribute
     */
    public static <A, O> UpdatableUniqueIndex<A, O> onAttribute(Attribute<O, A> attribute) {
        return onAttribute(new DefaultIndexMapFactory<A, O>(), attribute);
    }

    /**
     * Creates a new {@link UpdatableUniqueIndex} on the specified attribute.
     * <p/>
     *
     * @param indexMapFactory A factory used to create the main map-based data structure used by the index
     * @param attribute       The attribute on which the index will be built
     * @param <O>             The type of the object containing the attribute
     * @return A {@link UpdatableUniqueIndex} on this attribute
     */
    public static <A, O> UpdatableUniqueIndex<A, O> onAttribute(
        Factory<ConcurrentMap<A, O>> indexMapFactory, Attribute<O, A> attribute) {
        return new UpdatableUniqueIndex<A, O>(indexMapFactory, new DefaultReverseMapFactory<O, A>(), attribute);
    }

    /**
     * Creates a new {@link UpdatableUniqueIndex} on the specified attribute.
     * <p/>
     *
     * @param indexMapFactory   A factory used to create the main map-based data structure used by the index
     * @param reverseMapFactory A factory used to create the object-attribute mapping data structure used by the index
     * @param attribute         The attribute on which the index will be built
     * @param <O>               The type of the object containing the attribute
     * @return A {@link UpdatableUniqueIndex} on this attribute
     */
    public static <A, O> UpdatableUniqueIndex<A, O> onAttribute(
        Factory<ConcurrentMap<A, O>> indexMapFactory,
                                                                Factory<ConcurrentMap<O, A>> reverseMapFactory,
                                                                Attribute<O, A> attribute) {
        return new UpdatableUniqueIndex<A, O>(indexMapFactory, reverseMapFactory, attribute);
    }

}
