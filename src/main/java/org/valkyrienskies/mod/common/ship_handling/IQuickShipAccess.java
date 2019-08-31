package org.valkyrienskies.mod.common.ship_handling;

import java.util.Iterator;
import net.minecraft.util.math.AxisAlignedBB;

public interface IQuickShipAccess {

    Iterator<ShipHolder> getShipsNearby(int posX, int posZ, double range);

    Iterator<ShipHolder> getShipsIntersectingWith(AxisAlignedBB playerBB);

    void addShip(ShipHolder shipHolder);

    void deleteShip(ShipHolder shipHolder);

    void updateShipPositions();

    Iterable<ShipHolder> activeShips();
}
