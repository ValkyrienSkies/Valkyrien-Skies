package valkyrienwarfare.fixes;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import valkyrienwarfare.MixinLoadManager;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.RotationMatrices;
import valkyrienwarfare.mod.coordinates.*;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

/**
 * Used to indicate when a packet must be transformed into ship space to work
 * properly (Digging packets for example). Also comes with functionality to
 * store and retrieve a player data backup to prevent the player from getting
 * teleported somewhere else, but this is not necessarily required.
 *
 * @author thebest108
 */
public interface ITransformablePacket {

    default boolean isPacketOnMainThread(INetHandlerPlayServer server, boolean callingFromSponge) {
        if (!MixinLoadManager.isSpongeEnabled() || callingFromSponge) {
            NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) server;
            EntityPlayerMP player = serverHandler.player;
            return player.getServerWorld().isCallingFromMinecraftThread();
        } else {
            return false;
        }
    }

    /**
     * Puts the player into local coordinates and makes a record of where they used
     * to be.
     *
     * @param server
     * @param callingFromSponge
     */
    default void doPreProcessing(INetHandlerPlayServer server, boolean callingFromSponge) {
        if (isPacketOnMainThread(server, callingFromSponge)) {
            // System.out.println("Pre packet process");
            NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) server;
            EntityPlayerMP player = serverHandler.player;
            PhysicsWrapperEntity wrapper = getPacketParent(serverHandler);
            if (wrapper != null && wrapper.getPhysicsObject().getShipTransformationManager() != null) {
                ISubspaceProvider worldProvider = ISubspaceProvider.class.cast(player.getServerWorld());
                ISubspace worldSubspace = worldProvider.getSubspace();
                worldSubspace.snapshotSubspacedEntity(ISubspacedEntity.class.cast(player));
                RotationMatrices.applyTransform(
                        wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform(), player,
                        TransformType.GLOBAL_TO_SUBSPACE);
            }

        }
    }

    /**
     * Restores the player from local coordinates to where they used to be.
     *
     * @param server
     * @param callingFromSponge
     */
    default void doPostProcessing(INetHandlerPlayServer server, boolean callingFromSponge) {
        if (isPacketOnMainThread(server, callingFromSponge)) {
            NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) server;
            EntityPlayerMP player = serverHandler.player;
            PhysicsWrapperEntity wrapper = getPacketParent(serverHandler);
            // I don't care what happened to that ship in the time between, we must restore
            // the player to their proper coordinates.
            ISubspaceProvider worldProvider = ISubspaceProvider.class.cast(player.getServerWorld());
            ISubspace worldSubspace = worldProvider.getSubspace();
            ISubspacedEntity subspacedEntity = ISubspacedEntity.class.cast(player);
            ISubspacedEntityRecord record = worldSubspace.getRecordForSubspacedEntity(subspacedEntity);
            // System.out.println(player.getPosition());
            if (subspacedEntity.currentSubspaceType() == CoordinateSpaceType.SUBSPACE_COORDINATES) {
                subspacedEntity.restoreSubspacedEntityStateToRecord(record);
                player.setPosition(player.posX, player.posY, player.posZ);
            }
            // System.out.println(player.getPosition());
            // We need this because Sponge Mixins prevent this from properly working. This
            // won't be necessary on client however.
        }
    }

    PhysicsWrapperEntity getPacketParent(NetHandlerPlayServer server);
}
