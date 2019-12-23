package org.valkyrienskies.addon.control.piloting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

public interface ITileEntityPilotable {

    void onPilotControlsMessage(PilotControlsMessage message, EntityPlayerMP sender);

    EntityPlayer getPilotEntity();

    void setPilotEntity(EntityPlayer newPilot);

    void playerWantsToStopPiloting(EntityPlayer player);

    PhysicsObject getParentPhysicsEntity();

    default void onStartTileUsage(EntityPlayer player) {
    }

    default void onStopTileUsage() {
    }

}
