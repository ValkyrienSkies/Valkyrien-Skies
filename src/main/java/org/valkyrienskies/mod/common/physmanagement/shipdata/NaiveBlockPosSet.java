package org.valkyrienskies.mod.common.physmanagement.shipdata;

import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

/**
 * Naive implementation of IBlockPosSet, basically just a wrapper around a Set<BlockPos>.
 */
public class NaiveBlockPosSet implements IBlockPosSet {

    private final Set<BlockPos> blockPosSet;

    public NaiveBlockPosSet() {
        this.blockPosSet = new HashSet<>();
    }

    @Override
    public boolean addPos(int x, int y, int z) {
        return blockPosSet.add(new BlockPos(x, y, z));
    }

    @Override
    public boolean removePos(int x, int y, int z) {
        return blockPosSet.remove(new BlockPos(x, y, z));
    }

    @Override
    public boolean hasPos(int x, int y, int z) {
        return blockPosSet.contains(new BlockPos(x, y, z));
    }

    @Override
    public boolean canStorePos(int x, int y, int z) {
        return true;
    }

    @Override
    public int size() {
        return blockPosSet.size();
    }
}
