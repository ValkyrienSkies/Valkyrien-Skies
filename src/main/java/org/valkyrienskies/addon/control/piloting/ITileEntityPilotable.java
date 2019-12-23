package org.valkyrienskies.addon.control.piloting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;

public interface ITileEntityPilotable {

    void onPilotControlsMessage(PilotControlsMessage message, EntityPlayerMP sender);

    EntityPlayer getPilotEntity();

    void setPilotEntity(EntityPlayer newPilot);

    void playerWantsToStopPiloting(EntityPlayer player);

    PhysicsWrapperEntity getParentPhysicsEntity();

    default void onStartTileUsage(EntityPlayer player) {
    }

    default void onStopTileUsage() {
    }

}
