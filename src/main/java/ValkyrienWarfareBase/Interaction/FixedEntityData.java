package ValkyrienWarfareBase.Interaction;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class FixedEntityData {

	public Entity fixed;
	public PhysicsObject fixedOn;
	public Vec3d positionInLocal;

	public FixedEntityData(Entity ent, PhysicsObject ship, Vec3d localPos) {
		fixed = ent;
		fixedOn = ship;
		positionInLocal = localPos;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof FixedEntityData) {
			FixedEntityData other = (FixedEntityData) o;
			return other.fixed.getEntityId() == fixed.getEntityId();
		}
		return false;
	}

}