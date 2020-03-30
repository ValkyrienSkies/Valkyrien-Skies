package org.valkyrienskies.mod.common.ship_handling;

import net.minecraft.entity.player.EntityPlayer;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;

/**
 * This class is responsible determining which ships will be loaded/unloaded.
 */
class WorldShipLoadingController {

    private static final double LOAD_DISTANCE = 32; // 128;
    private static final double LOAD_BACKGROUND_DISTANCE = 50; // 256;
    private static final double UNLOAD_DISTANCE = 80; // 512;
    private final WorldServerShipManager shipManager;

    WorldShipLoadingController(WorldServerShipManager shipManager) {
        this.shipManager = shipManager;
    }

    void determineLoadAndUnload() {
        // For now, just spawn every single ship
        for (ShipData data : QueryableShipData.get(shipManager.getWorld())) {
            ShipTransform transform = data.getShipTransform();
            if (shipManager.getPhysObjectFromData(data) == null) {
                EntityPlayer closestPlayer = shipManager.getWorld().getClosestPlayer(transform.getPosX(), transform.getPosY(), transform.getPosZ(), LOAD_DISTANCE, false);
                if (closestPlayer != null) {
                    shipManager.queueShipLoad(data);
                } else {
                    EntityPlayer nextClosestPlayer = shipManager.getWorld().getClosestPlayer(transform.getPosX(), transform.getPosY(), transform.getPosZ(), LOAD_BACKGROUND_DISTANCE, false);
                    if (nextClosestPlayer != null) {
                        shipManager.queueShipLoadBackground(data);
                    }
                }
            } else {
                EntityPlayer closestPlayer = shipManager.getWorld().getClosestPlayer(transform.getPosX(), transform.getPosY(), transform.getPosZ(), UNLOAD_DISTANCE, false);
                if (closestPlayer == null) {
                    shipManager.queueShipUnload(data);
                }
            }
        }
    }

}
