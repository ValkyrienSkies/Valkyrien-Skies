package valkyrienwarfare.ship_handling;

import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class WorldShipManager {

    private transient World world;
    private final List<ShipHolder> everyShip;
    private final List<ShipHolder> activeShips;
    private final List<ShipHolder> idleShips;

    public WorldShipManager() {
        this.world = null;
        this.everyShip = new ArrayList<>();
        this.activeShips = new ArrayList<>();
        this.idleShips = new ArrayList<>();
    }

    public void initialize(World world) {
        this.world = world;
    }

}
