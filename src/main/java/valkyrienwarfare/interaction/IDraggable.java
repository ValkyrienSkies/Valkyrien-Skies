package valkyrienwarfare.interaction;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import net.minecraft.entity.MoverType;

public interface IDraggable {
	
	PhysicsWrapperEntity getWorldBelowFeet();
	
	void setWorldBelowFeet(PhysicsWrapperEntity toSet);
	
	Vector getVelocityAddedToPlayer();
	
	void setVelocityAddedToPlayer(Vector toSet);
	
	double getYawDifVelocity();
	
	void setYawDifVelocity(double toSet);
	
	void setCancelNextMove(boolean toSet);
	
	void move(MoverType type, double dx, double dy, double dz);
}
