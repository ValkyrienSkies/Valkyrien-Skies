package org.valkyrienskies.addon.control.piloting;

public enum ControllerInputType {

    CaptainsChair(true), ShipHelm(true), Zepplin(false), Telegraph(true), LiftLever(true);

    private final boolean lockPlayerMovement;

    private ControllerInputType(boolean lockPlayerMovement) {
        this.lockPlayerMovement = lockPlayerMovement;
    }

    public boolean shouldLockPlayerMovement() {
        return lockPlayerMovement;
    }

}
