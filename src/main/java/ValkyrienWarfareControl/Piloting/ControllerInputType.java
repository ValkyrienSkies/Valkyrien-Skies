package ValkyrienWarfareControl.Piloting;

public enum ControllerInputType {

    PilotsChair(true),
    ShipHelm(true),
    Zepplin(false);

	private boolean lockPlayerMovement;

	private ControllerInputType(boolean lockPlayerMovement) {
		this.lockPlayerMovement = lockPlayerMovement;
	}

	public boolean shouldLockPlayerMovement() {
		return lockPlayerMovement;
	}

}
