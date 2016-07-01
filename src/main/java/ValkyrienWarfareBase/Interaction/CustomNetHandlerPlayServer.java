package ValkyrienWarfareBase.Interaction;

import java.lang.reflect.Field;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.server.MinecraftServer;

public class CustomNetHandlerPlayServer extends NetHandlerPlayServer{

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
//	    final IntHashMap<Short> pendingTransactions = new IntHashMap();
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
		
	    try{
	    	Field intHashMapField = this.getClass().getDeclaredField("pendingTransactions");
	    	boolean isObsfucated = false;
	    	if(isObsfucated){
	    		intHashMapField = this.getClass().getDeclaredField("field_147372_n");
	    	}
	    	intHashMapField.setAccessible(true);
	    	intHashMapField.set(this, toReplace.pendingTransactions);
	    }catch(Exception e){}
	    
	}
	
	@Override
    public void processPlayerDigging(CPacketPlayerDigging packetIn){
		super.processPlayerDigging(packetIn);
	}

	@Override
	public void processUseEntity(CPacketUseEntity packetIn){
		super.processUseEntity(packetIn);
	}
}
