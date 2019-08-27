package org.valkyrienskies.addon.control.block.torque;

import net.minecraft.tileentity.TileEntity;

import java.util.Optional;

public interface IRotationNodeProvider<T extends TileEntity> {

    /**
     * @return Optional.empty() if the rotation node isn't ready yet.
     */
    default Optional<IRotationNode> getRotationNode() {
        T tileEntity = (T) this;
        return null;
    }

}
