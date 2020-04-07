package org.valkyrienskies.mod.common.physmanagement.shipdata;

import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.util.VSIterationUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Acts just like a Set<BlockPos>, but it can store the data however it wants to.
 */
public interface IBlockPosSet extends Set<BlockPos> {

    boolean add(int x, int y, int z) throws IllegalArgumentException;

    boolean remove(int x, int y, int z);

    boolean contains(int x, int y, int z);

    /**
     * The IBlockPosSet is not guaranteed to be able to store everything. Call canStore() to know what can be stored
     * and what cannot.
     */
    boolean canStore(int x, int y, int z);

    void clear();

    /**
     * Fast way to iterate over all BlockPos in this Set that does not require us to create BlockPos objects.
     * Although this default implementation is still slow, it should be accelerated by the data structure.
     */
    default void forEach(@Nonnull VSIterationUtils.IntTernaryConsumer action) {
        forEach(blockPos -> action.accept(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    default boolean add(@Nonnull BlockPos pos) throws IllegalArgumentException {
        return add(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean remove(@Nonnull BlockPos pos) {
        return remove(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean contains(@Nonnull BlockPos pos) {
        return contains(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean canStore(@Nonnull BlockPos pos) {
        return canStore(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean containsAll(@Nonnull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    default boolean addAll(@Nonnull Collection<? extends BlockPos> c) throws IllegalArgumentException {
        boolean modified = false;
        for (BlockPos pos : c) {
            modified |= add(pos);
        }
        return modified;
    }

    default boolean removeAll(@Nonnull Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            modified |= remove(o);
        }
        return modified;
    }

    default boolean retainAll(@Nonnull Collection<?> c) {
        boolean modified = false;
        for (BlockPos pos : this) {
            if (!c.contains(pos)) {
                remove(pos);
                modified = true;
            }
        }
        return modified;
    }

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean remove(@Nonnull Object o) throws IllegalArgumentException {
        if (o instanceof BlockPos) {
            return remove((BlockPos) o);
        } else {
            throw new IllegalArgumentException("Object " + o + " is not a BlockPos!");
        }
    }

    default boolean contains(@Nonnull Object o) {
        if (o instanceof BlockPos) {
            return contains((BlockPos) o);
        } else {
            return false;
        }
    }

    /**
     * Not recommended, this forces the IBlockPosSet to create a ton of BlockPos objects.
     */
    @Nonnull
    default Object[] toArray() {
        BlockPos[] arr = new BlockPos[size()];
        Iterator<BlockPos> iter = iterator();
        for (int i = 0; i < size(); i++) {
            arr[i] = iter.next();
        }
        return arr;
    }

    /**
     * Not recommended, this forces the IBlockPosSet to create a ton of BlockPos objects.
     */
    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    default <T> T[] toArray(@Nonnull T[] a) {
        return (T[]) toArray();
    }

}
