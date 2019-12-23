package org.valkyrienskies.addon.control.capability;

import net.minecraft.util.math.BlockPos;

public class ImplCapabilityLastRelay implements ICapabilityLastRelay {

    private BlockPos lastRelay;

    @Override
    public BlockPos getLastRelay() {
        return lastRelay;
    }

    @Override
    public void setLastRelay(BlockPos pos) {
        lastRelay = pos;
    }

    @Override
    public boolean hasLastRelay() {
        return lastRelay != null;
    }

}
