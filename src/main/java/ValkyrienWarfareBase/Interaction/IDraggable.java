package ValkyrienWarfareBase.Interaction;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.MoverType;

public interface IDraggable {

    public void tickAddedVelocity();

    public PhysicsWrapperEntity getWorldBelowFeet();

    public void setWorldBelowFeet(PhysicsWrapperEntity toSet);

    public Vector getVelocityAddedToPlayer();

    public void setVelocityAddedToPlayer(Vector toSet);

    public double getYawDifVelocity();

    public void setYawDifVelocity(double toSet);

    public void setCancelNextMove(boolean toSet);

    public void move(MoverType type, double dx, double dy, double dz);
}
