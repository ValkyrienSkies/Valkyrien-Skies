package valkyrienwarfare.fixes;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.MixinLoadManager;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.mod.physmanagement.interaction.INHPServerVW;
import valkyrienwarfare.mod.physmanagement.interaction.PlayerDataBackup;
import valkyrienwarfare.physics.TransformType;
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

	PlayerDataBackup getPlayerDataBackup();

	void setPlayerDataBackup(PlayerDataBackup backup);

	default boolean shouldTransformInCurrentEnvironment() {
		return MixinLoadManager.isSpongeEnabled;
	}

	/**
	 * Puts the player into local coordinates and makes a record of where they used
	 * to be.
	 * 
	 * @param server
	 * @param callingFromSponge
	 */
	default void doPreProcessing(INetHandlerPlayServer server, boolean callingFromSponge) {
		if (!MixinLoadManager.isSpongeEnabled || callingFromSponge) {
			INHPServerVW vw = (INHPServerVW) (NetHandlerPlayServer) server;
			EntityPlayerMP player = vw.getEntityPlayerFromHandler();
			if (player.getServerWorld().isCallingFromMinecraftThread()) {
				BlockPos packetPos = getBlockPos();
				PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world,
						packetPos);
				if (wrapper != null && wrapper.getPhysicsObject().coordTransform != null) {
					setPlayerDataBackup(new PlayerDataBackup(player));
					RotationMatrices.applyTransform(wrapper.getPhysicsObject().coordTransform.getCurrentTickTransform(), player,
							TransformType.GLOBAL_TO_LOCAL);
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
		if (!MixinLoadManager.isSpongeEnabled || callingFromSponge) {
			INHPServerVW vw = (INHPServerVW) (NetHandlerPlayServer) server;
			EntityPlayerMP player = vw.getEntityPlayerFromHandler();
			if (player.getServerWorld().isCallingFromMinecraftThread()) {
				BlockPos packetPos = getBlockPos();
				PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world,
						packetPos);
				// I don't care what happened to that ship in the time between, we must restore
				// the player to their proper coordinates.
				if (getPlayerDataBackup() != null) {
					getPlayerDataBackup().restorePlayerToBackup();
				}

			}
		}
	}
}
