package org.valkyrienskies.addon.control.block.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class BlockPosBlockPair {

    private final BlockPos pos;
    private final Block block;

    public BlockPosBlockPair(BlockPos pos, Block block) {
        this.pos = pos;
        this.block = block;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Block getBlock() {
        return block;
    }
}
