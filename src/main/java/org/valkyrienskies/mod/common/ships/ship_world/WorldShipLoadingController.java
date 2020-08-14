package org.valkyrienskies.mod.common.ships.ship_world;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.network.ShipIndexDataMessage;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ShipData;

import java.util.*;

/**
 * This class is responsible determining which ships will be loaded/unloaded.
 *
 * It also keeps track of which players are watching a ship, and sending update packets to players.
 */
class WorldShipLoadingController {

    /**
     * These constants must satisfy the following constraints:
     *
     * WATCH_DISTANCE < UNWATCH_DISTANCE
     * LOAD_DISTANCE < LOAD_BACKGROUND_DISTANCE < UNLOAD_DISTANCE
     * UNWATCH_DISTANCE <= UNLOAD_DISTANCE
     * WATCH_DISTANCE <= LOAD_DISTANCE
     */
    public static final double UNWATCH_DISTANCE = 50;
    public static final double WATCH_DISTANCE = 32;

    public static final double LOAD_DISTANCE = 32; // 128;
    public static final double LOAD_BACKGROUND_DISTANCE = 50; // 256;
    public static final double UNLOAD_DISTANCE = 80; // 512;
    private final WorldServerShipManager shipManager;
    private Map<ShipData, Set<EntityPlayerMP>> shipToWatchingPlayers;

    WorldShipLoadingController(WorldServerShipManager shipManager) {
        this.shipManager = shipManager;
        this.shipToWatchingPlayers = new HashMap<>();
    }

    /**
     * Tells the WorldServerShipManager which ships to load/unload/load in background.
     */
    void determineLoadAndUnload() {
        for (ShipData data : QueryableShipData.get(shipManager.getWorld())) {
            ShipTransform transform = data.getShipTransform();
            Vec3d shipPos = transform.getShipPositionVec3d();
            if (shipManager.getPhysObjectFromUUID(data.getUuid()) == null) {
                if (existsPlayerWithinDistanceXZ(shipManager.getWorld(), shipPos, LOAD_DISTANCE)) {
                    shipManager.queueShipLoad(data.getUuid());
                } else {
                    if (existsPlayerWithinDistanceXZ(shipManager.getWorld(), shipPos, LOAD_BACKGROUND_DISTANCE)) {
                        shipManager.queueShipLoadBackground(data.getUuid());
                    }
                }
            } else {
                if (!existsPlayerWithinDistanceXZ(shipManager.getWorld(), shipPos, UNLOAD_DISTANCE)) {
                    shipManager.queueShipUnload(data.getUuid());
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
            // Remove players further than the unwatch distance
            newWatching.get(ship.getShipData()).removeIf(watcher -> !isPlayerWithinDistanceXZ(watcher, shipPos, UNWATCH_DISTANCE));

            // Add players closer than the watch distance
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
        // First send the update packets
        // Create a map for every player to the ship data updates it will receive
        Map<EntityPlayerMP, List<ShipData>> updatesMap = new HashMap<>();
        shipManager.getWorld().playerEntities.forEach((player) -> updatesMap.put((EntityPlayerMP) player, new ArrayList<>()));

        for (PhysicsObject ship : shipManager.getAllLoadedPhysObj()) {
            ShipData shipData = ship.getShipData();
            Set<EntityPlayerMP> currentWatchers = newWatching.get(shipData);
            currentWatchers.forEach((player) -> updatesMap.get(player).add(shipData));
        }

        Map<EntityPlayerMP, ShipIndexDataMessage> playerPacketMap = new HashMap<>();

        // Then send those updates
        updatesMap.forEach((player, updates) -> {
            ShipIndexDataMessage indexDataMessage = new ShipIndexDataMessage();
            indexDataMessage.setDimensionID(shipManager.getWorld().provider.getDimension());
            if (!updates.isEmpty()) {
                indexDataMessage.addData(updates);
            }
            playerPacketMap.put(player, indexDataMessage);
        });

        // Then send ship loads to the packets
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

                newWatchers.forEach(player -> playerPacketMap.get(player).addLoadUUID(shipData.getUuid()));
            }
        }

        // Then add ship unloads to the packets
        for (ShipData shipData : oldWatching.keySet()) {
            Set<EntityPlayerMP> removedWatchers = new HashSet<>(oldWatching.get(shipData));
            if (newWatching.containsKey(shipData)) {
                removedWatchers.removeAll(newWatching.get(shipData));
            }
            for (EntityPlayerMP player : removedWatchers) {
                // Handles the case of players who left the world/dimension. Basically just prevents crashes with
                // BetterPortals.
                if (!playerPacketMap.containsKey(player)) {
                    playerPacketMap.put(player, new ShipIndexDataMessage());
                    playerPacketMap.get(player).setDimensionID(shipManager.getWorld().provider.getDimension());
                }
                playerPacketMap.get(player).addUnloadUUID(shipData.getUuid());
            }
        }

        // Finally, send each player their update packet
        playerPacketMap.forEach((player, packet) -> {
            if (!player.hasDisconnected()) {
                ValkyrienSkiesMod.physWrapperNetwork.sendTo(packet, player);
            }
        });
    }

    /**
     * Returns true if player is within distance of pos, only using XZ coordinates
     */
    private static boolean isPlayerWithinDistanceXZ(EntityPlayer player, Vec3d pos, double distance) {
        double xDif = player.posX - pos.x;
        double zDif = player.posZ - pos.z;
        return (xDif * xDif + zDif * zDif) < distance * distance;
    }

    /**
     * Returns true if there exists a player within world that is within distance of pos, only using XZ coordinates.
     */
    private static boolean existsPlayerWithinDistanceXZ(World world, Vec3d pos, double distance) {
        for (EntityPlayer player : world.playerEntities) {
            if (isPlayerWithinDistanceXZ(player, pos, distance)) {
                return true;
            }
        }
        return false;
    }

}
