package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.physics.management.PhysicsObject;

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
