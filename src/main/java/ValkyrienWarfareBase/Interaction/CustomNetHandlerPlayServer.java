package ValkyrienWarfareBase.Interaction;

import java.lang.reflect.Field;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

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
		} catch (Exception e) {
		}

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

		/*
		 * WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension); EnumHand enumhand = packetIn.getHand(); ItemStack itemstack = this.playerEntity.getHeldItem(enumhand); BlockPos blockpos = packetIn.getPos(); EnumFacing enumfacing = packetIn.getDirection(); this.playerEntity.markPlayerActive();
		 * 
		 * if (blockpos.getY() < this.serverController.getBuildLimit() - 1 || enumfacing != EnumFacing.UP && blockpos.getY() < this.serverController.getBuildLimit()) { double dist = playerEntity.interactionManager.getBlockReachDistance() + 3; dist *= dist;
		 * 
		 * Vector blockPosInGlobal = new Vector((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D);
		 * 
		 * PhysicsWrapperEntity chunkManager = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldserver, blockpos);
		 * 
		 * if(chunkManager!=null){ chunkManager.wrapping.coordTransform.fromLocalToGlobal(blockPosInGlobal); }
		 * 
		 * if (this.targetPos == null && this.playerEntity.getDistanceSq(blockPosInGlobal.X,blockPosInGlobal.Y,blockPosInGlobal.Z) < dist && !this.serverController.isBlockProtected(worldserver, blockpos, this.playerEntity) && worldserver.getWorldBorder().contains(blockpos)) { this.playerEntity.interactionManager.processRightClickBlock(this.playerEntity, worldserver, itemstack, enumhand, blockpos, enumfacing, packetIn.getFacingX(), packetIn.getFacingY(), packetIn.getFacingZ()); } } else { TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("build.tooHigh", new Object[] {Integer.valueOf(this.serverController.getBuildLimit())}); textcomponenttranslation.getStyle().setColor(TextFormatting.RED); this.playerEntity.connection.sendPacket(new SPacketChat(textcomponenttranslation)); }
		 * 
		 * this.playerEntity.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos)); this.playerEntity.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos.offset(enumfacing))); itemstack = this.playerEntity.getHeldItem(enumhand);
		 * 
		 * if (itemstack != null && itemstack.stackSize == 0) { this.playerEntity.setHeldItem(enumhand, (ItemStack)null); net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(this.playerEntity, itemstack, enumhand); itemstack = null; }
		 */
	}

	@Override
	public void processPlayerDigging(CPacketPlayerDigging packetIn) {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(playerEntity.worldObj, packetIn.getPosition());
		if (wrapper != null && wrapper.wrapping.coordTransform != null) {
			float playerYaw = playerEntity.rotationYaw;
			float playerPitch = playerEntity.rotationPitch;
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, playerEntity);
			super.processPlayerDigging(packetIn);
			playerEntity.rotationYaw = playerYaw;
			playerEntity.rotationPitch = playerPitch;
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, playerEntity);
		} else {
			super.processPlayerDigging(packetIn);
		}
	}

	@Override
	public void processPlayerBlockPlacement(CPacketPlayerTryUseItem packetIn) {
		super.processPlayerBlockPlacement(packetIn);
	}

	@Override
	public void processUseEntity(CPacketUseEntity packetIn) {
		super.processUseEntity(packetIn);
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
