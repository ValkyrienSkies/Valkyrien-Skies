package org.valkyrienskies.addon.control.capability;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface ICapabilityLastRelay {

    @Nullable
    public BlockPos getLastRelay();

    public void setLastRelay(BlockPos pos);

    public boolean hasLastRelay();
}
