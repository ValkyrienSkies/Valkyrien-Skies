package org.valkyrienskies.addon.control.piloting;

import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

/**
 * Todo: Convert to a forge capability.
 */
@Deprecated
public interface IShipPilot {

    PhysicsObject getPilotedShip();

    void setPilotedShip(PhysicsObject physicsObject);

    boolean isPilotingShip();

    boolean isPilotingATile();

    boolean isPiloting();

    BlockPos getPosBeingControlled();

    void setPosBeingControlled(BlockPos pos);

    ControllerInputType getControllerInputEnum();

    void setControllerInputEnum(ControllerInputType type);

    void stopPilotingEverything();
}
