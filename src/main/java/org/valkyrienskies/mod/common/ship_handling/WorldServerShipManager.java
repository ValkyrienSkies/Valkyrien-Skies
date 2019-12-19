package org.valkyrienskies.mod.common.ship_handling;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.multithreaded.VSThread;
import org.valkyrienskies.mod.common.network.ShipIndexDataMessage;
import org.valkyrienskies.mod.common.physics.IPhysicsEngine;
import org.valkyrienskies.mod.common.physics.bullet.BulletPhysicsEngine;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldServerShipManager implements IPhysObjectWorld {

    private transient World world;
    private transient Map<EntityPlayer, List<ShipData>> playerToWatchingShips;
    private transient VSThread physicsThread;

    private final IPhysicsEngine physicsEngine;

    public WorldServerShipManager(World world) {
        this.world = world;
        this.playerToWatchingShips = new HashMap<>();
        this.physicsThread = new VSThread(this.world);
        this.physicsEngine = new BulletPhysicsEngine();
        this.physicsThread.start();
    }
    @Override
    public void onWorldUnload() {
        this.world = null;
        // Just to avoid memory leaks.
        this.playerToWatchingShips.clear();
        this.playerToWatchingShips = null;
        this.physicsThread.kill();
        this.physicsEngine.unload();
    }

    @Override
    public PhysicsObject createPhysObjectFromData(ShipData data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removePhysObject(ShipData data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PhysicsObject getPhysObjectFromData(ShipData data) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public List<PhysicsObject> getNearbyPhysObjects(AxisAlignedBB toCheck) {
        List<PhysicsObject> nearby = new ArrayList<>();
        for (ShipData data : QueryableShipData.get(world)) {
            if (data.getPhyso() != null) {
                if (toCheck.intersects(data.getShipBB())) {
                    nearby.add(data.getPhyso());
                }
            }
        }
        return nearby;
    }

    public void tick() {
        // Does nothing for now, will eventually be used when ships are no longer entities.
        for (ShipData data : QueryableShipData.get(world)) {
            // TODO: Temp code. We should only be spawning in ships once a player gets close, and de-spawn them when
            //  players are far.
            if (data.getPhyso() == null) {
                PhysicsObject physicsObject = new PhysicsObject(world, data, false);
                data.setPhyso(physicsObject);
            }
            if (data.getPhyso() != null) {
                data.getPhyso().onTick();
                // Add players to the thing
                data.getPhyso().preloadNewPlayers();
            }
        }
        // Send all players in this world ship data.
        ShipIndexDataMessage indexDataMessage = new ShipIndexDataMessage();
        indexDataMessage.addDataToMessage(QueryableShipData.get(world));
        ValkyrienSkiesMod.physWrapperNetwork.sendToDimension(indexDataMessage, world.provider.getDimension());
    }

    @Nonnull
    @Override
    public List<PhysicsObject> getAllLoadedPhysObj() {
        List<PhysicsObject> allLoaded = new ArrayList<>();
        for (ShipData data : QueryableShipData.get(world)) {
            if (data.getPhyso() != null) {
                allLoaded.add(data.getPhyso());
            }
        }
        return allLoaded;
    }

    public World getWorld() {
        return world;
    }

    public VSThread getPhysicsThread() {
        return this.physicsThread;
    }

    @Override
    public IPhysicsEngine getPhysicsEngine() {
        return physicsEngine;
    }
}
