package ValkyrienWarfareBase.Interaction;

import java.lang.reflect.Field;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBucket;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CustomNetHandlerPlayServer extends NetHandlerPlayServer {

	//Blame Sponge for this
	public static final double dummyBlockReachDist = 999999999999999999999999999999999999D;
	public double lastGoodBlockReachDist;
	public int ticksSinceLastTry = 0;

	public CustomNetHandlerPlayServer(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn) {
		super(server, networkManagerIn, playerIn);
	}

	public CustomNetHandlerPlayServer(NetHandlerPlayServer toReplace) {
		super(toReplace.serverController, toReplace.netManager, toReplace.player);

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

		lastGoodBlockReachDist = toReplace.player.interactionManager.getBlockReachDistance();

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
	public void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock packetIn) {
		BlockPos packetPos = packetIn.getPos();
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, packetPos);
		if(player.interactionManager.getBlockReachDistance() != dummyBlockReachDist){
			lastGoodBlockReachDist = player.interactionManager.getBlockReachDistance();
		}
		if(wrapper != null){
			player.interactionManager.setBlockReachDistance(dummyBlockReachDist);
			ticksSinceLastTry = 0;
		}

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.player.getServerWorld());
		if (wrapper != null && wrapper.wrapping.coordTransform != null) {
			float playerYaw = player.rotationYaw;
			float playerPitch = player.rotationPitch;
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, player);
			if(player.getHeldItem(packetIn.getHand()) != null && player.getHeldItem(packetIn.getHand()).getItem() instanceof ItemBucket){
				player.interactionManager.setBlockReachDistance(lastGoodBlockReachDist);
			}
			try{
				super.processTryUseItemOnBlock(packetIn);
			}catch(Exception e){}
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, player);
			player.rotationYaw = playerYaw;
			player.rotationPitch = playerPitch;
		} else {
			super.processTryUseItemOnBlock(packetIn);
		}
	}

	@Override
	public void processPlayerDigging(CPacketPlayerDigging packetIn) {
		BlockPos packetPos = packetIn.getPosition();
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, packetPos);
		if(player.interactionManager.getBlockReachDistance() != dummyBlockReachDist){
			lastGoodBlockReachDist = player.interactionManager.getBlockReachDistance();
		}
		if(wrapper != null){
			player.interactionManager.setBlockReachDistance(dummyBlockReachDist);
			ticksSinceLastTry = 0;
		}

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.player.getServerWorld());
		if (wrapper != null && wrapper.wrapping.coordTransform != null) {
			float playerYaw = player.rotationYaw;
			float playerPitch = player.rotationPitch;
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, player);
			super.processPlayerDigging(packetIn);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, player);
			player.rotationYaw = playerYaw;
			player.rotationPitch = playerPitch;
		} else {
			super.processPlayerDigging(packetIn);
		}
	}

	@Override
	public void processUpdateSign(CPacketUpdateSign packetIn) {
		BlockPos packetPos = packetIn.getPosition();
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, packetPos);
		if(player.interactionManager.getBlockReachDistance() != dummyBlockReachDist){
			lastGoodBlockReachDist = player.interactionManager.getBlockReachDistance();
		}
		if(wrapper != null){
			player.interactionManager.setBlockReachDistance(dummyBlockReachDist);
			ticksSinceLastTry = 0;
		}

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.player.getServerWorld());
		if (wrapper != null && wrapper.wrapping.coordTransform != null) {
			float playerYaw = player.rotationYaw;
			float playerPitch = player.rotationPitch;
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, player);
			super.processUpdateSign(packetIn);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, player);
			player.rotationYaw = playerYaw;
			player.rotationPitch = playerPitch;
		} else {
			super.processUpdateSign(packetIn);
		}
	}
}
