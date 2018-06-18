package valkyrienwarfare.mixin.network.play.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.world.GameType;
import valkyrienwarfare.fixes.ITransformablePacket;
import valkyrienwarfare.mod.coordinates.ISubspace;
import valkyrienwarfare.mod.coordinates.ISubspacedEntity;
import valkyrienwarfare.mod.coordinates.ISubspacedEntityRecord;
import valkyrienwarfare.mod.coordinates.VectorImmutable;
import valkyrienwarfare.mod.physmanagement.interaction.IDraggable;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

@Mixin(CPacketPlayer.class)
public class MixinCPacketPlayer implements ITransformablePacket {

	private final CPacketPlayer thisPacket = CPacketPlayer.class.cast(this);
	private GameType cachedPlayerGameType = null;

	@Inject(method = "processPacket", at = @At(value = "HEAD"))
	public void preDiggingProcessPacket(INetHandlerPlayServer server, CallbackInfo info) {
		this.doPreProcessing(server, false);
	}

	@Inject(method = "processPacket", at = @At(value = "RETURN"))
	public void postDiggingProcessPacket(INetHandlerPlayServer server, CallbackInfo info) {
		this.doPostProcessing(server, false);
	}

	@Override
	public void doPreProcessing(INetHandlerPlayServer server, boolean callingFromSponge) {
		if (isPacketOnMainThread(server, callingFromSponge)) {
			PhysicsWrapperEntity parent = getPacketParent((NetHandlerPlayServer) server);
			if (parent != null) {
				ISubspace parentSubspace = parent.getPhysicsObject().getSubspace();
				ISubspacedEntityRecord entityRecord = parentSubspace
						.getRecordForSubspacedEntity(ISubspacedEntity.class.cast(getPacketPlayer(server)));
				VectorImmutable positionGlobal = entityRecord.getPositionInGlobalCoordinates();
				VectorImmutable lookVectorGlobal = entityRecord.getLookDirectionInGlobalCoordinates();

				thisPacket.moving = true;
				thisPacket.onGround = true;
				thisPacket.x = positionGlobal.getX();
				thisPacket.y = positionGlobal.getY();
				thisPacket.z = positionGlobal.getZ();

				// ===== Dangerous code here =====
				cachedPlayerGameType = getPacketPlayer(server).interactionManager.gameType;
				getPacketPlayer(server).interactionManager.gameType = GameType.CREATIVE;
			}
		}
	}

	@Override
	public void doPostProcessing(INetHandlerPlayServer server, boolean callingFromSponge) {
		if (isPacketOnMainThread(server, callingFromSponge)) {
			// ===== Dangerous code here =====
			if (cachedPlayerGameType != null) {
				getPacketPlayer(server).interactionManager.gameType = cachedPlayerGameType;
			}
			PhysicsWrapperEntity parent = getPacketParent((NetHandlerPlayServer) server);
			if (parent != null) {
				parent.getPhysicsObject().getSubspace()
						.forceSubspaceRecord(ISubspacedEntity.class.cast(getPacketPlayer(server)), null);
			}
			IDraggable draggable = IDraggable.class.cast(getPacketPlayer(server));
			draggable.setForcedRelativeSubspace(null);
		}
	}

	@Override
	public PhysicsWrapperEntity getPacketParent(NetHandlerPlayServer server) {
		EntityPlayerMP player = server.player;
		IDraggable draggable = IDraggable.class.cast(player);
		return draggable.getForcedSubspaceBelowFeet();
	}

	private EntityPlayerMP getPacketPlayer(INetHandlerPlayServer server) {
		return ((NetHandlerPlayServer) server).player;
	}

}
