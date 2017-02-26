package ValkyrienWarfareBase.Interaction;

import java.lang.reflect.Field;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.server.MinecraftServer;

public class CustomNetHandlerPlayServer extends NetHandlerPlayServer {

	public CustomNetHandlerPlayServer(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn) {
		super(server, networkManagerIn, playerIn);
	}

	public CustomNetHandlerPlayServer(NetHandlerPlayServer toReplace) {
		super(toReplace.serverController, toReplace.netManager, toReplace.playerEntity);

		networkTickCount = toReplace.networkTickCount;
		keepAliveId = toReplace.keepAliveId;
		lastPingTime = toReplace.lastPingTime;
		lastSentPingPacket = toReplace.lastSentPingPacket;

		chatSpamThresholdCount = toReplace.chatSpamThresholdCount;
		itemDropThreshold = toReplace.itemDropThreshold;
		// final IntHashMap<Short> pendingTransactions = new IntHashMap();
		firstGoodX = toReplace.firstGoodX;
		firstGoodY = toReplace.firstGoodY;
		firstGoodZ = toReplace.firstGoodZ;
		lastGoodX = toReplace.lastGoodX;
		lastGoodY = toReplace.lastGoodY;
		lastGoodZ = toReplace.lastGoodZ;
		lowestRiddenEnt = toReplace.lowestRiddenEnt;
		lowestRiddenX = toReplace.lowestRiddenX;
		lowestRiddenY = toReplace.lowestRiddenY;
		lowestRiddenZ = toReplace.lowestRiddenZ;
		lowestRiddenX1 = toReplace.lowestRiddenX1;
		lowestRiddenY1 = toReplace.lowestRiddenY1;
		lowestRiddenZ1 = toReplace.lowestRiddenZ1;
		targetPos = toReplace.targetPos;
		teleportId = toReplace.teleportId;
		lastPositionUpdate = toReplace.lastPositionUpdate;
		floating = toReplace.floating;

		floatingTickCount = toReplace.floatingTickCount;
		vehicleFloating = toReplace.vehicleFloating;
		vehicleFloatingTickCount = toReplace.vehicleFloatingTickCount;
		movePacketCounter = toReplace.movePacketCounter;
		lastMovePacketCounter = toReplace.lastMovePacketCounter;

		try {
			Field intHashMapField = this.getClass().getDeclaredField("pendingTransactions");
			boolean isObsfucated = false;
			if (isObsfucated) {
				intHashMapField = this.getClass().getDeclaredField("field_147372_n");
			}
			intHashMapField.setAccessible(true);
			intHashMapField.set(this, toReplace.pendingTransactions);
		} catch (Exception e) {}

	}

	@Override
	public void processRightClickBlock(CPacketPlayerTryUseItemOnBlock packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(playerEntity.worldObj, packetIn.getPos());
		if (wrapper != null && wrapper.wrapping.coordTransform != null) {
			float playerYaw = playerEntity.rotationYaw;
			float playerPitch = playerEntity.rotationPitch;
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, playerEntity);
			super.processRightClickBlock(packetIn);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, playerEntity);
			playerEntity.rotationYaw = playerYaw;
			playerEntity.rotationPitch = playerPitch;
		} else {
			super.processRightClickBlock(packetIn);
		}
	}

	@Override
	public void processPlayerDigging(CPacketPlayerDigging packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(playerEntity.worldObj, packetIn.getPosition());
		if (wrapper != null && wrapper.wrapping.coordTransform != null) {
			float playerYaw = playerEntity.rotationYaw;
			float playerPitch = playerEntity.rotationPitch;
			Vector oldPlayerPos = new Vector(playerEntity);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, playerEntity);
			super.processPlayerDigging(packetIn);
			playerEntity.rotationYaw = playerYaw;
			playerEntity.rotationPitch = playerPitch;
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, playerEntity);
			playerEntity.posX = oldPlayerPos.X;
			playerEntity.posY = oldPlayerPos.Y;
			playerEntity.posZ = oldPlayerPos.Z;
		} else {
			super.processPlayerDigging(packetIn);
		}
	}

	@Override
	public void processUpdateSign(CPacketUpdateSign packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(playerEntity.worldObj, packetIn.getPosition());
		if (wrapper != null && wrapper.wrapping.coordTransform != null) {
			float playerYaw = playerEntity.rotationYaw;
			float playerPitch = playerEntity.rotationPitch;
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, playerEntity);
			super.processUpdateSign(packetIn);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, playerEntity);
			playerEntity.rotationYaw = playerYaw;
			playerEntity.rotationPitch = playerPitch;
		} else {
			super.processUpdateSign(packetIn);
		}
	}
}
