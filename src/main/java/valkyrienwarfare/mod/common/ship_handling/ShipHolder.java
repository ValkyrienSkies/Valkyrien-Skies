package valkyrienwarfare.mod.common.ship_handling;

import net.minecraft.entity.player.EntityPlayerMP;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;

import java.util.ArrayList;
import java.util.List;

public class ShipHolder {

    private String shipID;
    private double shipPosX;
    private double shipPosY;
    private double shipPosZ;
    private IShipChunkClaims shipChunkClaims;
    private transient PhysicsObject ship;
    private transient List<EntityPlayerMP> watchingPlayers = new ArrayList<>();
    private transient boolean isActive = false;
    private transient IWorldShipManager worldShipManager;

    protected void initializeTransients(IWorldShipManager worldShipManager) {
        this.worldShipManager = worldShipManager;
        shipChunkClaims.initializeTransients(worldShipManager, this);
    }

    protected boolean markShipAsActive() {
        // If the chunks aren't yet loaded, tell the game to load them
        if (!this.shipChunkClaims.areChunkClaimsFullyLoaded()) {
            this.shipChunkClaims.loadAllChunkClaims();
        } else {
            // If the chunks are completely loaded, then we shall generate the PhysicsObject.
            // ship = new PhysicsObject(this);
            this.isActive = true;
        }
        return this.shipChunkClaims.areChunkClaimsFullyLoaded();
    }

    protected void markShipAsInactive() {
        // TODO: Implement chunk unloading
        this.isActive = false;
    }

    protected boolean isActive() {
        return isActive;
    }

    protected double getShipPosX() {
        return shipPosX;
    }

    protected double getShipPosZ() {
        return shipPosZ;
    }

    protected PhysicsObject getShip() {
        return ship;
    }

    protected List<EntityPlayerMP> getWatchingPlayers() {
        return watchingPlayers;
    }

}