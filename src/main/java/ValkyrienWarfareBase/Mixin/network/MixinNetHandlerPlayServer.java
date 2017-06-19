package ValkyrienWarfareBase.Mixin.network;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.Interaction.IntrinsicNetHandlerPlayServerInterface;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBucket;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.util.math.BlockPos;

@Mixin(NetHandlerPlayServer.class)
@Implements(@Interface(iface = IntrinsicNetHandlerPlayServerInterface.class, prefix = "vw$"))
public abstract class MixinNetHandlerPlayServer {

	private double dummyBlockReachDist = 9999999999999999999999999999D;
	private double lastGoodBlockReachDist;
	private int ticksSinceLastTry = 0;

	@Shadow
    public EntityPlayerMP player;

	@Intrinsic(displace = true)
	public void vw$processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock packetIn) {
		BlockPos packetPos = packetIn.getPos();
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, packetPos);
		if(player.interactionManager.getBlockReachDistance() != dummyBlockReachDist){
			lastGoodBlockReachDist = player.interactionManager.getBlockReachDistance();
		}
		if(wrapper != null){
			player.interactionManager.setBlockReachDistance(dummyBlockReachDist);
			ticksSinceLastTry = 0;
		}

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, NetHandlerPlayServer.class.cast(this), this.player.getServerWorld());
		if (wrapper != null && wrapper.wrapping.coordTransform != null) {
			float playerYaw = player.rotationYaw;
			float playerPitch = player.rotationPitch;
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, player);
			if(player.getHeldItem(packetIn.getHand()) != null && player.getHeldItem(packetIn.getHand()).getItem() instanceof ItemBucket){
				player.interactionManager.setBlockReachDistance(lastGoodBlockReachDist);
			}
			try{
				processTryUseItemOnBlock(packetIn);
			}catch(Exception e){}
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, player);
			player.rotationYaw = playerYaw;
			player.rotationPitch = playerPitch;
		} else {
			processTryUseItemOnBlock(packetIn);
		}
		player.interactionManager.setBlockReachDistance(lastGoodBlockReachDist);
	}

	@Intrinsic(displace = true)
	public void vw$processPlayerDigging(CPacketPlayerDigging packetIn) {
		BlockPos packetPos = packetIn.getPosition();
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, packetPos);
		if(player.interactionManager.getBlockReachDistance() != dummyBlockReachDist){
			lastGoodBlockReachDist = player.interactionManager.getBlockReachDistance();
		}
		if(wrapper != null){
			player.interactionManager.setBlockReachDistance(dummyBlockReachDist);
			ticksSinceLastTry = 0;
		}

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, NetHandlerPlayServer.class.cast(this), this.player.getServerWorld());
		if (wrapper != null && wrapper.wrapping.coordTransform != null) {
			float playerYaw = player.rotationYaw;
			float playerPitch = player.rotationPitch;
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, player);
			processPlayerDigging(packetIn);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, player);
			player.rotationYaw = playerYaw;
			player.rotationPitch = playerPitch;
		} else {
			processPlayerDigging(packetIn);
		}
		player.interactionManager.setBlockReachDistance(lastGoodBlockReachDist);
	}

	@Intrinsic(displace = true)
	public void vw$processUpdateSign(CPacketUpdateSign packetIn) {
		BlockPos packetPos = packetIn.getPosition();
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, packetPos);
		if(player.interactionManager.getBlockReachDistance() != dummyBlockReachDist){
			lastGoodBlockReachDist = player.interactionManager.getBlockReachDistance();
		}
		if(wrapper != null){
			player.interactionManager.setBlockReachDistance(dummyBlockReachDist);
			ticksSinceLastTry = 0;
		}

		PacketThreadUtil.checkThreadAndEnqueue(packetIn, NetHandlerPlayServer.class.cast(this), this.player.getServerWorld());
		if (wrapper != null && wrapper.wrapping.coordTransform != null) {
			float playerYaw = player.rotationYaw;
			float playerPitch = player.rotationPitch;
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, player);
			processUpdateSign(packetIn);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, player);
			player.rotationYaw = playerYaw;
			player.rotationPitch = playerPitch;
		} else {
			processUpdateSign(packetIn);
		}
		player.interactionManager.setBlockReachDistance(lastGoodBlockReachDist);
	}

	@Shadow
	public abstract void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock packetIn);

	@Shadow
	public abstract void processPlayerDigging(CPacketPlayerDigging packetIn);

	@Shadow
	public abstract void processUpdateSign(CPacketUpdateSign packetIn);
}
