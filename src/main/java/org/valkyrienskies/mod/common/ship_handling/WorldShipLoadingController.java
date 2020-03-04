package org.valkyrienskies.mod.common.ship_handling;

import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;

/**
 * This class is responsible determining which ships will be loaded/unloaded.
 */
class WorldShipLoadingController {

    private final WorldServerShipManager shipManager;

    WorldShipLoadingController(WorldServerShipManager shipManager) {
        this.shipManager = shipManager;
    }

    void determineLoadAndUnload() {
        // For now, just spawn every single ship
        for (ShipData data : QueryableShipData.get(shipManager.getWorld())) {
            if (shipManager.getPhysObjectFromData(data) == null) {
                shipManager.queueShipLoad(data);
            }
        }
    }

}
