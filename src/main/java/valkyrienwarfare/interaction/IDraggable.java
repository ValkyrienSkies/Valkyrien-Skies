package valkyrienwarfare.interaction;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import net.minecraft.entity.MoverType;

public interface IDraggable {
	
	public PhysicsWrapperEntity getWorldBelowFeet();
	
	public void setWorldBelowFeet(PhysicsWrapperEntity toSet);
	
	public Vector getVelocityAddedToPlayer();
	
	public void setVelocityAddedToPlayer(Vector toSet);
	
	public double getYawDifVelocity();
	
	public void setYawDifVelocity(double toSet);
	
	public void setCancelNextMove(boolean toSet);
	
	public void move(MoverType type, double dx, double dy, double dz);
}
