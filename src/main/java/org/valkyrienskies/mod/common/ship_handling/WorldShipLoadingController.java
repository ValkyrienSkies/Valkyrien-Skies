package org.valkyrienskies.mod.common.ship_handling;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.network.ShipIndexDataMessage;
import org.valkyrienskies.mod.common.network.SpawnPhysObjMessage;
import org.valkyrienskies.mod.common.network.UnloadPhysObjMessage;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;

import java.util.*;

/**
 * This class is responsible determining which ships will be loaded/unloaded.
 *
 * It also keeps track of which players are watching a ship, and sending update packets to players.
 */
class WorldShipLoadingController {

    /**
     * These constants must satisfy these constraints: (TODO: Make test for this)
     *
     * WATCH_DISTANCE < UNWATCH_DISTANCE
     * LOAD_DISTANCE < LOAD_BACKGROUND_DISTANCE < UNLOAD_DISTANCE
     * UNWATCH_DISTANCE <= UNLOAD_DISTANCE
     * WATCH_DISTANCE <= LOAD_DISTANCE
     */
    private static final double UNWATCH_DISTANCE = 80;
    private static final double WATCH_DISTANCE = 32;

    private static final double LOAD_DISTANCE = 32; // 128;
    private static final double LOAD_BACKGROUND_DISTANCE = 50; // 256;
    private static final double UNLOAD_DISTANCE = 80; // 512;
    private final WorldServerShipManager shipManager;
    private Map<ShipData, Set<EntityPlayerMP>> shipToWatchingPlayers;

    WorldShipLoadingController(WorldServerShipManager shipManager) {
        this.shipManager = shipManager;
        this.shipToWatchingPlayers = new HashMap<>();
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

    /**
     * Send ship updates to clients.
     */
    void sendUpdatesToPlayers() {
        // First get an updated watching players map
        Map<ShipData, Set<EntityPlayerMP>> newWatching = updateWatchingPlayers();
        // Then send updates to players based on the old watching map and new watching map
        sendUpdatesPackets(shipToWatchingPlayers, newWatching);
        // Then update the watching map
        shipToWatchingPlayers = newWatching;
        // Then update the watching player map of the ship chunks
        for (PhysicsObject ship : shipManager.getAllLoadedPhysObj()) {
            ship.getWatchingPlayers().clear();
            ship.getWatchingPlayers().addAll(shipToWatchingPlayers.get(ship.getShipData()));
        }
    }

    /**
     * Determine which ships are watched by which players.
     */
    private Map<ShipData, Set<EntityPlayerMP>> updateWatchingPlayers() {
        Map<ShipData, Set<EntityPlayerMP>> newWatching = new HashMap<>();
        // Copy the old watching to the new, making sure not to copy data from ships that got unloaded.
        for (PhysicsObject ship : shipManager.getAllLoadedPhysObj()) {
            ShipData shipData = ship.getShipData();
            if (shipToWatchingPlayers.containsKey(shipData)) {
                Set<EntityPlayerMP> oldWatchingPlayers = shipToWatchingPlayers.get(shipData);
                Set<EntityPlayerMP> newWatchingPlayers = new HashSet<>();
                // Do this to prevent players who left the game from propagating to future watching maps.
                for (EntityPlayer player : shipManager.getWorld().playerEntities) {
                    if (oldWatchingPlayers.contains(player)) {
                        newWatchingPlayers.add((EntityPlayerMP) player);
                    }
                }
                newWatching.put(shipData, newWatchingPlayers);
            } else {
                newWatching.put(shipData, new HashSet<>());
            }
        }

        // Remove players that aren't watching anymore, and add new watching players
        for (PhysicsObject ship : shipManager.getAllLoadedPhysObj()) {
            Vec3d shipPos = ship.getShipTransform().getShipPositionVec3d();
            newWatching.get(ship.getShipData()).removeIf(watcher -> isPlayerWithinDistanceXZ(watcher, shipPos, UNWATCH_DISTANCE));

            for (EntityPlayer player : shipManager.getWorld().playerEntities) {
                if (isPlayerWithinDistanceXZ(player, shipPos, WATCH_DISTANCE)) {
                    newWatching.get(ship.getShipData()).add((EntityPlayerMP) player);
                }
            }
        }

        return newWatching;
    }

    /**
     * Send load/unload/update packets accordingly.
     */
    private void sendUpdatesPackets(Map<ShipData, Set<EntityPlayerMP>> oldWatching, Map<ShipData, Set<EntityPlayerMP>> newWatching) {
        // First send ship spawn packets
        for (PhysicsObject ship : shipManager.getAllLoadedPhysObj()) {
            ShipData shipData = ship.getShipData();
            Set<EntityPlayerMP> newWatchers = new HashSet<>(newWatching.get(shipData));
            if (oldWatching.containsKey(shipData)) {
                newWatchers.removeAll(oldWatching.get(shipData));
            }
            if (!newWatchers.isEmpty()) {
                // First send the ship chunks to the new watchers
                for (Chunk chunk : ship.getClaimedChunkCache()) {
                    SPacketChunkData data = new SPacketChunkData(chunk, 65535);
                    for (EntityPlayerMP player : newWatchers) {
                        player.connection.sendPacket(data);
                        shipManager.getWorld().getEntityTracker().sendLeashedEntitiesInChunk(player, chunk);
                    }
                }

                // Then send the ship spawn packet
                SpawnPhysObjMessage physObjMessage = new SpawnPhysObjMessage();
                physObjMessage.initializeData(shipData);
                for (EntityPlayerMP player : newWatchers) {
                    ValkyrienSkiesMod.physWrapperNetwork.sendTo(physObjMessage, player);
                }
            }
        }

        // Then send the ship unload packets
        for (ShipData shipData : oldWatching.keySet()) {
            Set<EntityPlayerMP> removedWatchers = new HashSet<>(oldWatching.get(shipData));
            if (newWatching.containsKey(shipData)) {
                removedWatchers.removeAll(newWatching.get(shipData));
            }
            if (!removedWatchers.isEmpty()) {
                UnloadPhysObjMessage unloadMsg = new UnloadPhysObjMessage();
                unloadMsg.initializeData(shipData);
                // Send the unload packet
                for (EntityPlayerMP removedWatcher : removedWatchers) {
                    ValkyrienSkiesMod.physWrapperNetwork.sendTo(unloadMsg, removedWatcher);
                    System.out.println("Unload packet sent!");
                }
            }
        }


        // And finally, send the update packets
        // First create a map for every player to the ship data updates it will receive
        Map<EntityPlayerMP, List<ShipData>> updatesMap = new HashMap<>();
        shipManager.getWorld().playerEntities.forEach((player) -> updatesMap.put((EntityPlayerMP) player, new ArrayList<>()));

        for (PhysicsObject ship : shipManager.getAllLoadedPhysObj()) {
            ShipData shipData = ship.getShipData();
            Set<EntityPlayerMP> currentWatchers = newWatching.get(shipData);
            currentWatchers.forEach((player) -> updatesMap.get(player).add(shipData));
        }
        // Then send those updates
        updatesMap.forEach((player, updates) -> {
            ShipIndexDataMessage indexDataMessage = new ShipIndexDataMessage();
            if (!updates.isEmpty()) {
                indexDataMessage.addDataToMessage(updates);
                ValkyrienSkiesMod.physWrapperNetwork.sendTo(indexDataMessage, player);
            }
        });
    }

    private boolean isPlayerWithinDistanceXZ(EntityPlayer player, Vec3d pos, double distance) {
        double xDif = player.posX - pos.x;
        double zDif = player.posZ - pos.z;
        return (xDif * xDif + zDif * zDif) < distance * distance;
    }

}
