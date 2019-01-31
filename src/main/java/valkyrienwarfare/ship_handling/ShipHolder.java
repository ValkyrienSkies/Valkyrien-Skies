package valkyrienwarfare.ship_handling;

import valkyrienwarfare.physics.management.PhysicsObject;

public class ShipHolder {

    private String shipID;
    private double shipPosX;
    private double shipPosY;
    private double shipPosZ;
    private IShipChunkClaims shipChunkClaims;
    private transient PhysicsObject ship;
    private boolean isActive;

    protected ShipHolder() {
    }

    protected boolean markShipAsActive() {
        if (ship == null) {
            return false;
        }
        return true;
    }

    protected boolean markShipAsInactive() {
        return true;
    }
}