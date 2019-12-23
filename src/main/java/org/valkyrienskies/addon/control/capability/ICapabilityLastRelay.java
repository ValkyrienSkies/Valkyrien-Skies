package org.valkyrienskies.addon.control.capability;

import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;

public interface ICapabilityLastRelay {

    @Nullable
    public BlockPos getLastRelay();

    public void setLastRelay(BlockPos pos);

    public boolean hasLastRelay();
}
