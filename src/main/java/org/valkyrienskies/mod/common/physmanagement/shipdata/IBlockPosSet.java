package org.valkyrienskies.mod.common.physmanagement.shipdata;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;

public interface IBlockPosSet extends Set<BlockPos> {

    boolean add(int x, int y, int z);

    boolean remove(int x, int y, int z);

    boolean contains(int x, int y, int z);

    boolean canStore(int x, int y, int z);

    void clear();

    default boolean add(BlockPos pos) {
        return add(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean remove(BlockPos pos) {
        return remove(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean contains(BlockPos pos) {
        return contains(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean canStore(BlockPos pos) {
        return canStore(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    default boolean addAll(Collection<? extends BlockPos> c) {
        boolean modified = false;
        for (BlockPos pos : c) {
            modified |= add(pos);
        }
        return modified;
    }

    default boolean removeAll(Collection<?> c) {
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

    default boolean remove(Object o) {
        if (o instanceof BlockPos) {
            return remove((BlockPos) o);
        } else {
            return false;
        }
    }

    default boolean contains(Object o) {
        if (o instanceof BlockPos) {
            return contains((BlockPos) o);
        } else {
            return false;
        }
    }

    @Nonnull
    default Object[] toArray() {
        BlockPos[] arr = new BlockPos[size()];
        Iterator<BlockPos> iter = iterator();
        for (int i = 0; i < size(); i++) {
            arr[i] = iter.next();
        }
        return arr;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    default <T> T[] toArray(@Nonnull T[] a) {
        return (T[]) toArray();
    }

}
