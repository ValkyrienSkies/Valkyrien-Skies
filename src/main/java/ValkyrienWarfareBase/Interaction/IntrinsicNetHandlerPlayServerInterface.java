package ValkyrienWarfareBase.Interaction;

import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUpdateSign;

public interface IntrinsicNetHandlerPlayServerInterface {
	
	public void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock packetIn);
	
	public void processPlayerDigging(CPacketPlayerDigging packetIn);
	
	public void processUpdateSign(CPacketUpdateSign packetIn);
}
