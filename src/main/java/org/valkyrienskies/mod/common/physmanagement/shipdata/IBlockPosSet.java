package org.valkyrienskies.mod.common.physmanagement.shipdata;

import net.minecraft.util.math.BlockPos;

public interface IBlockPosSet {

    boolean addPos(int x, int y, int z);

    boolean removePos(int x, int y, int z);

    boolean hasPos(int x, int y, int z);

    boolean canStorePos(int x, int y, int z);

    int size();

    default boolean addPos(BlockPos pos) {
        return addPos(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean removePos(BlockPos pos) {
        return removePos(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean isEmpty() {
        return size() == 0;
    }
}
