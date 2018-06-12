package valkyrienwarfare.mod.coordinates;

import valkyrienwarfare.api.Vector;

public class ImplSubspacedEntityRecord implements ISubspacedEntityRecord {

	private final ISubspacedEntity parentEntity;
	private final ISubspace parentSubspace;
	private final VectorImmutable position;
	private final VectorImmutable positionLastTick;
	private final VectorImmutable lookVector;
	private final VectorImmutable velocity;

	public ImplSubspacedEntityRecord(ISubspacedEntity parentEntity, ISubspace parentSubspace, VectorImmutable position,
			VectorImmutable positionLastTick, VectorImmutable lookVector, VectorImmutable velocity) {
		this.parentEntity = parentEntity;
		this.parentSubspace = parentSubspace;
		this.position = position;
		this.positionLastTick = positionLastTick;
		this.lookVector = lookVector;
		this.velocity = velocity;
	}
	
	@Override
	public ISubspacedEntity getParentEntity() {
		return parentEntity;
	}

	@Override
	public ISubspace getParentSubspace() {
		return parentSubspace;
	}

	@Override
	public VectorImmutable getPosition() {
		return position;
	}

	@Override
	public VectorImmutable getPositionLastTick() {
		return positionLastTick;
	}
	
	@Override
	public VectorImmutable getLookDirection() {
		return lookVector;
	}

	@Override
	public VectorImmutable getVelocity() {
		return velocity;
	}

}
