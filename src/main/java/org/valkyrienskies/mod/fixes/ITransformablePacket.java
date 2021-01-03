package org.valkyrienskies.mod.fixes;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.capability.VSCapabilityRegistry;
import org.valkyrienskies.mod.common.capability.entity_backup.ICapabilityEntityBackup;
import org.valkyrienskies.mod.common.ships.ShipData;
import valkyrienwarfare.api.TransformType;

/**
 * Used to indicate when a packet must be transformed into ship space to work properly (Digging
 * packets for example). Also comes with functionality to store and retrieve a player data backup to
 * prevent the player from getting teleported somewhere else, but this is not necessarily required.
 *
 * @author thebest108
 */
public interface ITransformablePacket {

    default boolean isPacketOnMainThread(INetHandlerPlayServer server, boolean callingFromSponge) {
        if (!ValkyrienSkiesMod.isSpongePresent() || callingFromSponge) {
            NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) server;
            EntityPlayerMP player = serverHandler.player;
            return player.getServerWorld().isCallingFromMinecraftThread();
        } else {
            return false;
        }
    }

    /**
     * Puts the player into local coordinates and makes a record of where they used to be.
     */
    default void doPreProcessing(INetHandlerPlayServer server, boolean callingFromSponge) {
        if (isPacketOnMainThread(server, callingFromSponge)) {
            // System.out.println("Pre packet process");
            NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) server;
            EntityPlayerMP player = serverHandler.player;
            ShipData physicsObject = getPacketParent(serverHandler);
            if (physicsObject != null) {
                // First make a backup of the player position
                ICapabilityEntityBackup entityBackup = player.getCapability(VSCapabilityRegistry.VS_ENTITY_BACKUP, null);
                entityBackup.backupEntityPosition(player);
                // Then put the player into ship coordinates.
                physicsObject.getShipTransform()
                        .transform(player, TransformType.GLOBAL_TO_SUBSPACE, true);
            }
        }
    }

    /**
     * Restores the player from local coordinates to where they used to be.
     */
    default void doPostProcessing(INetHandlerPlayServer server, boolean callingFromSponge) {
        if (isPacketOnMainThread(server, callingFromSponge)) {
            NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) server;
            EntityPlayerMP player = serverHandler.player;
            // If we made a backup in doPreProcessing(), then restore from that backup.
            ICapabilityEntityBackup entityBackup = player.getCapability(VSCapabilityRegistry.VS_ENTITY_BACKUP, null);
            if (entityBackup.hasBackupPosition()) {
                entityBackup.restoreEntityToBackup(player);
            }
        }
    }

    ShipData getPacketParent(NetHandlerPlayServer server);
}
