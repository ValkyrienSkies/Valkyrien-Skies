package valkyrienwarfare.mod.coordinates;

public interface ISubspacedEntityRecord {

	ISubspacedEntity getParentEntity();
	ISubspace getParentSubspace();
	VectorImmutable getPosition();
	VectorImmutable getLookDirection();
}
