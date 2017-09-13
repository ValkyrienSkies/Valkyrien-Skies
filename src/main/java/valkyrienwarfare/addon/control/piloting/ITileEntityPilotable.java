package valkyrienwarfare.addon.control.piloting;

import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

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
