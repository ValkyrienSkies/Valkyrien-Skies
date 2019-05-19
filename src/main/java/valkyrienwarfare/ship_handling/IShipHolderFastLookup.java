package valkyrienwarfare.ship_handling;

import java.util.Iterator;

public interface IShipHolderFastLookup {

    Iterator<ShipHolder> getNearbyShips(double posX, double posY, double posZ, double range);
}
