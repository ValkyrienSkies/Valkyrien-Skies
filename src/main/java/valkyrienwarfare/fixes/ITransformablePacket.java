package valkyrienwarfare.fixes;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.MixinLoadManager;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.mod.coordinates.CoordinateSpaceType;
import valkyrienwarfare.mod.coordinates.ISubspace;
import valkyrienwarfare.mod.coordinates.ISubspaceProvider;
import valkyrienwarfare.mod.coordinates.ISubspacedEntity;
import valkyrienwarfare.mod.coordinates.ISubspacedEntityRecord;
import valkyrienwarfare.mod.coordinates.TransformType;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

/**
 * Used to indicate when a packet must be transformed into ship space to work
 * properly (Digging packets for example). Also comes with functionality to
 * store and retrieve a player data backup to prevent the player from getting
 * teleported somewhere else. Also comes with helper default methods packets
 * implementing this can use.
 *
 * @author thebest108
 */
public interface ITransformablePacket {

	BlockPos getBlockPos();

	default boolean shouldTransformInCurrentEnvironment() {
		return MixinLoadManager.isSpongeEnabled();
	}

	/**
	 * Puts the player into local coordinates and makes a record of where they used
	 * to be.
	 * 
	 * @param server
	 * @param callingFromSponge
	 */
	default void doPreProcessing(INetHandlerPlayServer server, boolean callingFromSponge) {
		if (!MixinLoadManager.isSpongeEnabled() || callingFromSponge) {
			// System.out.println("Pre packet process");
			NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) server;
			EntityPlayerMP player = serverHandler.player;
			if (player.getServerWorld().isCallingFromMinecraftThread()) {
				BlockPos packetPos = getBlockPos();
				PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(player.world,
						packetPos);
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
	}

	/**
	 * Restores the player from local coordinates to where they used to be.
	 * 
	 * @param server
	 * @param callingFromSponge
	 */
	default void doPostProcessing(INetHandlerPlayServer server, boolean callingFromSponge) {
		if (!MixinLoadManager.isSpongeEnabled() || callingFromSponge) {
			NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) server;
			EntityPlayerMP player = serverHandler.player;
			if (player.getServerWorld().isCallingFromMinecraftThread()) {
				BlockPos packetPos = getBlockPos();
				PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(player.world,
						packetPos);
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
	}
}
