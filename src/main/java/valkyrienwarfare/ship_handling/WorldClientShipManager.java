package valkyrienwarfare.ship_handling;

import net.minecraft.world.World;

public class WorldClientShipManager implements IWorldShipManager {

    private final World world;
    private final IQuickShipAccess shipAccess;

    public WorldClientShipManager(World world) {
        this.world = world;
        this.shipAccess = new SimpleQuickShipAccess();
    }

    @Override
    public void tick() {
        // Do ship stuff
    }

    @Override
    public void onWorldUnload() {
        // Don't do anything
    }

    @Override
    public World getWorld() {
        return world;
    }
}
