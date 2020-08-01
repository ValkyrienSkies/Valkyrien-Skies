package org.valkyrienskies.addon.control.nodenetwork;

import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.ships.block_relocation.IRelocationAwareTile;

public interface IVSNodeProvider extends IRelocationAwareTile {

    IVSNode getNode();

    /**
     * Shifts all of the internal state data, like connections to other nodes.
     */
    default void shiftInternalData(BlockPos offset) {
        getNode().shiftConnections(offset);
    }

    /**
     * This is the result from all of the graphing code. We specifically want nothing external to
     * interact with anything but this, to so we can easily replacing graph code.
     */
    Iterable<IVSNode> getNetworkedConnections();

}
