package valkyrienwarfare.ship_handling;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.util.*;

public class WorldShipManager {

    private transient World world;
    private IQuickShipAccess shipAccess;
    private transient Map<EntityPlayer, List<ShipHolder>> playerToWatchingShips;

    public WorldShipManager() {
        this.world = null;
        this.playerToWatchingShips = null;
        this.shipAccess = new SimpleQuickShipAccess();
    }

    public void initializeTransients(World world) {
        this.world = world;
        this.playerToWatchingShips = new HashMap<EntityPlayer, List<ShipHolder>>();
        System.out.println("INITIALIZATION SUCCESS!");
    }

    public void tick() {
        for (EntityPlayer player : world.playerEntities) {
            if (!playerToWatchingShips.containsKey(player)) {
                // Then the player hasn't been initialized into the system yet.
                // TODO: Properly initialize players eventually.
                playerToWatchingShips.put(player, new ArrayList<>());
                // continue;
            }
            List<ShipHolder> shipsToUnwatch = new ArrayList<>(playerToWatchingShips.get(player));
            Iterator<ShipHolder> nearbyShips = shipAccess.getShipsNearby((int) player.posX, (int) player.posZ, 128);
            while (nearbyShips.hasNext()) {
                ShipHolder nearbyShip = nearbyShips.next();
                shipsToUnwatch.remove(nearbyShip);
                 if (nearbyShip.isActive()) {
                     setPlayerToWatchShip((EntityPlayerMP) player, nearbyShip);
                     // nearbyShip.sendToPlayer((EntityPlayerMP) player);
                 } else {
                     nearbyShip.markShipAsActive();
                 }
            }
            for (ShipHolder shipToUnwatch : shipsToUnwatch) {
                setPlayerToUnwatchShip((EntityPlayerMP) player, shipToUnwatch);
            }
        }

        for (ShipHolder ship : shipAccess.activeShips()) {
             if (ship.getWatchingPlayers().isEmpty()) {
                 ship.markShipAsInactive();
             }
        }
    }

    private void setPlayerToWatchShip(EntityPlayerMP player, ShipHolder ship) {
        ship.getWatchingPlayers().add(player);
    }

    private void setPlayerToUnwatchShip(EntityPlayerMP player, ShipHolder ship) {
        ship.getWatchingPlayers().remove(player);
    }

    protected World getWorld() {
        return world;
    }
}
