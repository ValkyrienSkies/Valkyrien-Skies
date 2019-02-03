package valkyrienwarfare.ship_handling;

import net.minecraft.util.math.AxisAlignedBB;

import java.util.Iterator;

public interface IQuickShipAccess {

    Iterator<ShipHolder> getShipsNearby(int posX, int posZ, double range);

    Iterator<ShipHolder> getShipsIntersectingWith(AxisAlignedBB playerBB);

    void addShip(ShipHolder shipHolder);

    void deleteShip(ShipHolder shipHolder);

    void updateShipPositions();

    Iterable<ShipHolder> activeShips();
}
