package org.valkyrienskies.mod.common.util.cqengine;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.support.CloseableIterator;
import com.googlecode.cqengine.index.support.CloseableRequestResources;
import com.googlecode.cqengine.persistence.Persistence;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import com.googlecode.cqengine.persistence.support.ObjectSet;
import com.googlecode.cqengine.persistence.support.ObjectStore;
import com.googlecode.cqengine.persistence.support.ObjectStoreAsSet;
import com.googlecode.cqengine.persistence.support.PersistenceFlags;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.FlagsEnabled;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import com.googlecode.cqengine.resultset.closeable.CloseableResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ConcurrentUpdatableIndexedCollection<O> implements IndexedCollection<O> {

    protected final Persistence<O, ?> persistence;
    protected final ObjectStore<O> objectStore;
    protected final UpdatableQueryEngine<O> indexEngine;

    protected final Set<Consumer<Collection<O>>> addListeners = new HashSet<>();
    protected final Set<Consumer<Collection<O>>> removeListeners = new HashSet<>();
    protected final Set<BiConsumer<Iterable<O>, Iterable<O>>> updateListeners = new HashSet<>();


    /**
     * @param addListener To be executed whenever {@link #add} is called. Does not trigger if the
     *                    collection was not modified (e.g. duplicate item)
     */
    public void registerAddListener(Consumer<Collection<O>> addListener) {
        addListeners.add(addListener);
    }

    /**
     * @param addListener To be executed whenever {@link #remove} is called. Does not trigger if the
     *                    collection was not modified (e.g. duplicate item)
     */
    public void registerRemoveListener(Consumer<Collection<O>> addListener) {
        removeListeners.add(addListener);
    }

    /**
     * @param updateListener To be executed whenever {@link #remove}, {@link #add}, or {@link
     *                       #update} is called. Does not trigger if the collection was not modified
     *                       (e.g. duplicate item)
     */
    public void registerUpdateListener(BiConsumer<Iterable<O>, Iterable<O>> updateListener) {
        updateListeners.add(updateListener);
    }

    /**
     * @param c         The objects to update indices for
     * @param attribute The attribute for which to update indices
     */
    public void updateObjectIndices(Collection<? extends O> c, Attribute<O, ?> attribute) {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            @SuppressWarnings({"unchecked"})
            Collection<O> objects = (Collection<O>) c;
            objectStore.addAll(objects, queryOptions);
            indexEngine.updateAll(ObjectSet.fromCollection(objects), attribute, queryOptions);
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * Updates the indices on an object
     */
    public void updateObjectIndices(O object, Attribute<O, ?> attribute) {
        this.updateObjectIndices(Collections.singleton(object), attribute);
    }

    // region == Old stuff below ==

    /**
     * Creates a new {@link ConcurrentIndexedCollection} with default settings, using {@link
     * OnHeapPersistence}.
     */
    public ConcurrentUpdatableIndexedCollection() {
        this(OnHeapPersistence.withoutPrimaryKey());
    }

    /**
     * Creates a new {@link ConcurrentIndexedCollection} which will use the given persistence to
     * create the backing set.
     *
     * @param persistence The {@link Persistence} implementation which will create a concurrent
     *                    {@link java.util.Set} in which objects added to the indexed collection
     *                    will be stored, and which will provide access to the underlying storage of
     *                    indexes.
     */
    public ConcurrentUpdatableIndexedCollection(Persistence<O, ? extends Comparable> persistence) {
        this.persistence = persistence;
        this.objectStore = persistence.createObjectStore();
        UpdatableQueryEngine<O> queryEngine = new UpdatableCollectionQueryEngine<>();
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            queryEngine.init(objectStore, queryOptions);
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
        this.indexEngine = queryEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Persistence<O, ?> getPersistence() {
        return persistence;
    }

    // ----------- Query Engine Methods -------------

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet<O> retrieve(Query<O> query) {
        return retrieve(query, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet<O> retrieve(Query<O> query, QueryOptions queryOptions) {
        final QueryOptions finalQueryOptions = openRequestScopeResourcesIfNecessary(queryOptions);
        flagAsReadRequest(finalQueryOptions);
        ResultSet<O> results = indexEngine.retrieve(query, finalQueryOptions);
        return new CloseableResultSet<O>(results, query, finalQueryOptions) {
            @Override
            public void close() {
                super.close();
                closeRequestScopeResourcesIfNecessary(finalQueryOptions);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(Iterable<O> objectsToRemove, Iterable<O> objectsToAdd) {
        return update(objectsToRemove, objectsToAdd, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(
        Iterable<O> objectsToRemove, Iterable<O> objectsToAdd, QueryOptions queryOptions) {
        queryOptions = openRequestScopeResourcesIfNecessary(queryOptions);
        try {
            boolean modified = doRemoveAll(objectsToRemove, queryOptions);
            return doAddAll(objectsToAdd, queryOptions) || modified;
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addIndex(Index<O> index) {
        addIndex(index, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addIndex(Index<O> index, QueryOptions queryOptions) {
        if (!(index instanceof UpdatableIndex)) {
            throw new IllegalArgumentException("Must be updatable index");
        }
        queryOptions = openRequestScopeResourcesIfNecessary(queryOptions);
        try {
            indexEngine.addIndex(index, queryOptions);
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeIndex(Index<O> index) {
        removeIndex(index, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeIndex(Index<O> index, QueryOptions queryOptions) {
        queryOptions = openRequestScopeResourcesIfNecessary(queryOptions);
        try {
            indexEngine.removeIndex(index, queryOptions);
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    @Override
    public Iterable<Index<O>> getIndexes() {
        return indexEngine.getIndexes();
    }

    // ----------- Collection Accessor Methods -------------

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            return objectStore.size(queryOptions);
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            return objectStore.isEmpty(queryOptions);
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            return objectStore.contains(o, queryOptions);
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            return getObjectStoreAsSet(queryOptions).toArray();
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(T[] a) {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            //noinspection SuspiciousToArrayCall
            return getObjectStoreAsSet(queryOptions).toArray(a);
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            return objectStore.containsAll(c, queryOptions);
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    // ----------- Collection Mutator Methods -------------

    /**
     * {@inheritDoc}
     */
    @Override
    public CloseableIterator<O> iterator() {
        return new CloseableIterator<O>() {
            final QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);

            private final CloseableIterator<O> collectionIterator = objectStore
                .iterator(queryOptions);
            boolean autoClosed = false;

            @Override
            public boolean hasNext() {
                boolean hasNext = collectionIterator.hasNext();
                if (!hasNext) {
                    close();
                    autoClosed = true;
                }
                return hasNext;
            }

            private O currentObject = null;

            @Override
            public O next() {
                O next = collectionIterator.next();
                currentObject = next;
                return next;
            }

            @Override
            public void remove() {
                if (currentObject == null) {
                    throw new IllegalStateException();
                }
                // Handle an edge case where we might have retrieved the last object and called close() automatically,
                // but then the application calls remove() so we have to reopen request-scope resources temporarily
                // to remove the last object...
                if (autoClosed) {
                    ConcurrentUpdatableIndexedCollection.this
                        .remove(currentObject); // reopens resources temporarily
                } else {
                    doRemoveAll(Collections.singleton(currentObject),
                        queryOptions); // uses existing resources
                }
                currentObject = null;
            }

            @Override
            public void close() {
                CloseableRequestResources.closeQuietly(collectionIterator);
                closeRequestScopeResourcesIfNecessary(queryOptions);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(O o) {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            Set<O> oSet = singleton(o);

            // Add the object to the index.
            // Indexes handle gracefully the case that the objects supplied already exist in the index...
            boolean modified = objectStore.add(o, queryOptions);
            indexEngine.addAll(ObjectSet.fromCollection(oSet), queryOptions);

            if (modified) {
                addListeners.forEach(consumer -> consumer.accept(oSet));
                updateListeners.forEach(consumer -> consumer.accept(emptyList(), oSet));
            }

            return modified;
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object object) {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            @SuppressWarnings({"unchecked"})
            O o = (O) object;
            Set<O> oSet = singleton(o);

            boolean modified = objectStore.remove(o, queryOptions);
            indexEngine.removeAll(ObjectSet.fromCollection(oSet), queryOptions);

            if (modified) {
                removeListeners.forEach(consumer -> consumer.accept(oSet));
                updateListeners.forEach(consumer -> consumer.accept(oSet, emptyList()));
            }

            return modified;
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection<? extends O> c) {
        return this.addAll(c, false);
    }

    /**
     * @param silent Whether or not this operation triggers listeners
     */
    public boolean addAll(Collection<? extends O> c, boolean silent) {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            @SuppressWarnings({"unchecked"})
            Collection<O> objects = (Collection<O>) c;
            boolean modified = objectStore.addAll(objects, queryOptions);
            indexEngine.addAll(ObjectSet.fromCollection(objects), queryOptions);

            if (!silent && modified) {
                addListeners.forEach(consumer -> consumer.accept(objects));
                updateListeners.forEach(consumer -> consumer.accept(emptyList(), objects));
            }

            return modified;
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return this.removeAll(c, false);
    }

    public boolean removeAll(Collection<?> c, boolean silent) {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            @SuppressWarnings({"unchecked"})
            Collection<O> objects = (Collection<O>) c;

            boolean modified = objectStore.removeAll(objects, queryOptions);
            indexEngine.removeAll(ObjectSet.fromCollection(objects), queryOptions);

            if (!silent && modified) {
                removeListeners.forEach(consumer -> consumer.accept(objects));
                updateListeners.forEach(consumer -> consumer.accept(objects, emptyList()));
            }

            return modified;
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        CloseableIterator<O> iterator = null;
        try {
            boolean modified = false;
            iterator = objectStore.iterator(queryOptions);
            while (iterator.hasNext()) {
                O next = iterator.next();
                if (!c.contains(next)) {
                    doRemoveAll(Collections.singleton(next), queryOptions);
                    modified = true;
                }
            }
            return modified;
        } finally {
            CloseableRequestResources.closeQuietly(iterator);
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            objectStore.clear(queryOptions);
            indexEngine.clear(queryOptions);
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    protected boolean doAddAll(Iterable<O> objects, QueryOptions queryOptions) {
        if (objects instanceof Collection) {
            Collection<O> c = (Collection<O>) objects;
            boolean modified = objectStore.addAll(c, queryOptions);
            indexEngine.addAll(ObjectSet.fromCollection(c), queryOptions);
            return modified;
        } else {
            boolean modified = false;
            for (O object : objects) {
                boolean added = objectStore.add(object, queryOptions);
                indexEngine.addAll(ObjectSet.fromCollection(singleton(object)), queryOptions);
                modified = added || modified;
            }
            return modified;
        }
    }

    protected boolean doRemoveAll(Iterable<O> objects, QueryOptions queryOptions) {
        if (objects instanceof Collection) {
            Collection<O> c = (Collection<O>) objects;
            boolean modified = objectStore.removeAll(c, queryOptions);
            indexEngine.removeAll(ObjectSet.fromCollection(c), queryOptions);
            return modified;
        } else {
            boolean modified = false;
            for (O object : objects) {
                boolean removed = objectStore.remove(object, queryOptions);
                indexEngine.removeAll(ObjectSet.fromCollection(singleton(object)), queryOptions);
                modified = removed || modified;
            }
            return modified;
        }
    }

    protected QueryOptions openRequestScopeResourcesIfNecessary(QueryOptions queryOptions) {
        if (queryOptions == null) {
            queryOptions = new QueryOptions();
        }
        if (!(persistence instanceof OnHeapPersistence)) {
            persistence.openRequestScopeResources(queryOptions);
        }
        queryOptions.put(Persistence.class, persistence);
        return queryOptions;
    }

    protected void closeRequestScopeResourcesIfNecessary(QueryOptions queryOptions) {
        if (!(persistence instanceof OnHeapPersistence)) {
            persistence.closeRequestScopeResources(queryOptions);
        }
    }


    protected ObjectStoreAsSet<O> getObjectStoreAsSet(QueryOptions queryOptions) {
        return new ObjectStoreAsSet<O>(objectStore, queryOptions);
    }

    @Override
    public boolean equals(Object o) {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Set)) {
                return false;
            }
            Set that = (Set) o;

            if (!getObjectStoreAsSet(queryOptions).equals(that)) {
                return false;
            }

            return true;
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    @Override
    public int hashCode() {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            return getObjectStoreAsSet(queryOptions).hashCode();
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    @Override
    public String toString() {
        QueryOptions queryOptions = openRequestScopeResourcesIfNecessary(null);
        try {
            return getObjectStoreAsSet(queryOptions).toString();
        } finally {
            closeRequestScopeResourcesIfNecessary(queryOptions);
        }
    }

    /**
     * Sets a flag into the given query options to record that this request will read from the
     * collection but will not modify it. This is used to facilitate locking in some persistence
     * implementations.
     *
     * @param queryOptions The query options for the request
     */
    protected static void flagAsReadRequest(QueryOptions queryOptions) {
        FlagsEnabled.forQueryOptions(queryOptions).add(PersistenceFlags.READ_REQUEST);
    }

    // endregion

}
