package org.valkyrienskies.addon.control.piloting;

import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;

/**
 * Todo: Convert to a forge capability.
 */
@Deprecated
public interface IShipPilot {

    PhysicsWrapperEntity getPilotedShip();

    void setPilotedShip(PhysicsWrapperEntity wrapper);

    boolean isPilotingShip();

    boolean isPilotingATile();

    boolean isPiloting();

    BlockPos getPosBeingControlled();

    void setPosBeingControlled(BlockPos pos);

    ControllerInputType getControllerInputEnum();

    void setControllerInputEnum(ControllerInputType type);

    void stopPilotingEverything();
}
