package valkyrienwarfare.addon.control.piloting;

public enum ControllerInputType {
	
	PilotsChair(true),
	ShipHelm(true),
	Zepplin(false),
	Telegraph(true);
	
	private boolean lockPlayerMovement;
	
	private ControllerInputType(boolean lockPlayerMovement) {
		this.lockPlayerMovement = lockPlayerMovement;
	}
	
	public boolean shouldLockPlayerMovement() {
		return lockPlayerMovement;
	}
	
}
