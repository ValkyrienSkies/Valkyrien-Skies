package org.valkyrienskies.addon.control.capability;

import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;

public interface ICapabilityLastRelay {

    @Nullable
    BlockPos getLastRelay();

    void setLastRelay(BlockPos pos);

    boolean hasLastRelay();
}
