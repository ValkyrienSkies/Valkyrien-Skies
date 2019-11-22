package org.valkyrienskies.mod.common.physmanagement.shipdata;

import java.util.HashSet;
import java.util.Set;
import lombok.experimental.Delegate;
import net.minecraft.util.math.BlockPos;

/**
 * Naive implementation of IBlockPosSet, basically just a wrapper around a HashSet<BlockPos>.
 */
public class NaiveBlockPosSet implements IBlockPosSet {

    @Delegate
    private final Set<BlockPos> blockPosSet;

    public NaiveBlockPosSet() {
        this.blockPosSet = new HashSet<>();
    }

    @Override
    public boolean add(int x, int y, int z) {
        return blockPosSet.add(new BlockPos(x, y, z));
    }

    @Override
    public boolean remove(int x, int y, int z) {
        return blockPosSet.remove(new BlockPos(x, y, z));
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return blockPosSet.contains(new BlockPos(x, y, z));
    }

    @Override
    public boolean canStore(int x, int y, int z) {
        return true;
    }

}
