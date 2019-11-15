package org.valkyrienskies.addon.control.block.multiblocks;

import net.minecraft.util.math.BlockPos;

public interface ITileEntityMultiblockPart<E extends IMultiblockSchematic, F extends ITileEntityMultiblockPart> {

    boolean isPartOfAssembledMultiblock();

    boolean isMaster();

    F getMaster();

    BlockPos getMultiblockOrigin();

    BlockPos getRelativePos();

    void disassembleMultiblock();

    void disassembleMultiblockLocal();

    void assembleMultiblock(E schematic, BlockPos relativePos);

    E getMultiBlockSchematic();

}
