package org.valkyrienskies.mod.common.ship_handling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Unfinished class. Purpose is to use this to manage which ships instead of the Minecraft entity
 * system.
 */
public class SimpleQuickShipAccess implements IQuickShipAccess {

    private final List<ShipHolder> ships;

    public SimpleQuickShipAccess() {
        ships = new ArrayList<>();
    }

    @Override
    public Iterator<ShipHolder> getShipsIntersectingWith(AxisAlignedBB playerBB) {
        List<ShipHolder> ships = new ArrayList<>();
        for (ShipHolder shipHolder : ships) {
            if (shipHolder.getShip() != null) {
                // TODO: Finish me
            }
        }
        return ships.iterator();
    }

    @Override
    public void addShip(ShipHolder shipHolder) {
        ships.add(shipHolder);
    }

    @Override
    public void deleteShip(ShipHolder shipHolder) {
        ships.remove(shipHolder);
    }

    @Override
    public void updateShipPositions() {
        // TODO: A more efficient implementation will make use of this
    }

    @Override
    public Iterable<ShipHolder> activeShips() {
        List<ShipHolder> activeShips = new ArrayList<>();
        for (ShipHolder ship : ships) {
            if (ship.isActive()) {
                activeShips.add(ship);
            }
        }
        return activeShips;
    }

    public Iterator<ShipHolder> getShipsNearby(int posX, int posZ, double range) {
        double rangeSq = range * range;
        List<ShipHolder> ships = new ArrayList<>();
        for (ShipHolder shipHolder : ships) {
            double distanceSq =
                (shipHolder.getShipPosX() - posX) * (shipHolder.getShipPosX() - posX)
                    + (shipHolder.getShipPosZ() - posZ) * (shipHolder.getShipPosZ() - posZ);
            if (distanceSq < rangeSq) {
                ships.add(shipHolder);
            }
        }
        return ships.iterator();
    }
}
