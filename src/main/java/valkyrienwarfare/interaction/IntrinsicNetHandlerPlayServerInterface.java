package valkyrienwarfare.interaction;

import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUpdateSign;

public interface IntrinsicNetHandlerPlayServerInterface {
	
	void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock packetIn);
	
	void processPlayerDigging(CPacketPlayerDigging packetIn);
	
	void processUpdateSign(CPacketUpdateSign packetIn);
}
