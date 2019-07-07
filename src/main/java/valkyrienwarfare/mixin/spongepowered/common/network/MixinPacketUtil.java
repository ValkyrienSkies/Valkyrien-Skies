package valkyrienwarfare.mixin.spongepowered.common.network;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.fixes.ITransformablePacket;

/**
 * Necessary mixin.
 */
@Mixin(targets = "org/spongepowered/common/event/tracking/phase/packet/PacketPhaseUtil", remap = false)
public class MixinPacketUtil {

    @Inject(method = "onProcessPacket", at = @At(value = "HEAD"))
    private static void preOnProcessPacket(Packet packetIn, INetHandler netHandler, CallbackInfo info) {
        if (packetIn instanceof ITransformablePacket) {
            ITransformablePacket transformPacket = (ITransformablePacket) packetIn;
            transformPacket.doPreProcessing((NetHandlerPlayServer) netHandler, true);
        }
    }

    @Inject(method = "onProcessPacket", at = @At(value = "RETURN"))
    private static void postOnProcessPacket(Packet packetIn, INetHandler netHandler, CallbackInfo info) {
        if (packetIn instanceof ITransformablePacket) {
            ITransformablePacket transformPacket = (ITransformablePacket) packetIn;
            transformPacket.doPostProcessing((NetHandlerPlayServer) netHandler, true);
        }
    }

}
