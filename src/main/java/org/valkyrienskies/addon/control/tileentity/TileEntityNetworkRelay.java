package org.valkyrienskies.addon.control.tileentity;

import net.minecraft.util.math.AxisAlignedBB;
import org.valkyrienskies.addon.control.nodenetwork.BasicNodeTileEntity;
import org.valkyrienskies.mod.common.config.VSConfig;

public class TileEntityNetworkRelay extends BasicNodeTileEntity {

    public TileEntityNetworkRelay() {
        super();
    }

    @Override
    protected int getMaximumConnections() {
        return VSConfig.networkRelayLimit;
    }

    // TODO: Not the best solution, but it works for now.
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}
