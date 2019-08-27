package org.valkyrienskies.mod.common.ship_handling;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.multithreaded.VWThread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldServerShipManager implements IWorldShipManager {

    private transient World world;
    private IQuickShipAccess shipAccess;
    private transient Map<EntityPlayer, List<ShipHolder>> playerToWatchingShips;
    private transient VWThread physicsThread;

    public WorldServerShipManager() {
        this.world = null;
        this.playerToWatchingShips = null;
        this.shipAccess = new SimpleQuickShipAccess();
    }

    public void initializeTransients(World world) {
        this.world = world;
        this.playerToWatchingShips = new HashMap<>();
        this.physicsThread = new VWThread(this.world);
        this.physicsThread.start();
    }

    @Override
    public void onWorldUnload() {
        this.world = null;
        // Just to avoid memory leaks.
        this.playerToWatchingShips.clear();
        this.playerToWatchingShips = null;
        this.physicsThread.kill();
        // TODO: Save into PorkDB
    }

    public void tick() {
        // Does nothing for now, will eventually be used when ships are no longer entities.
    }

    public World getWorld() {
        return world;
    }

    public VWThread getPhysicsThread() {
        return this.physicsThread;
    }
}
