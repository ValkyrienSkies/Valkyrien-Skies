package org.valkyrienskies.mod.common.piloting;

import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

import java.util.UUID;

/**
 * Todo: Convert to a forge capability.
 */
@Deprecated
public interface IShipPilot {

    PhysicsObject getPilotedShip();

    void setPilotedShip(PhysicsObject physicsObject);

    boolean isPilotingShip();

    boolean isPilotingATile();

    /**
     * Want to delete his, but can't because its used in vs-control
     */
    @Deprecated
    boolean isPiloting();

    BlockPos getPosBeingControlled();

    void setPosBeingControlled(BlockPos pos);

    UUID getShipIDBeingControlled();

    void setShipIDBeingControlled(UUID shipID);

    ControllerInputType getControllerInputEnum();

    void setControllerInputEnum(ControllerInputType type);

    void stopPilotingEverything();
}
