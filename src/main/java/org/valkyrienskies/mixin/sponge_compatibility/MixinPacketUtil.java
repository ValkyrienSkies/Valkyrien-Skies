package org.valkyrienskies.mixin.sponge_compatibility;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.valkyrienskies.mod.fixes.ITransformablePacket;

/**
 * This mixin transforms the position of packets (like digging/place block) before the game gets them.
 *
 * Necessary to make the game think the player is allowed to edit the ships blocks, otherwise it would think the player
 * is too far away.
 */
@Mixin(value = PacketPhaseUtil.class, remap = false)
public class MixinPacketUtil {

    @Inject(method = "onProcessPacket", at = @At(value = "HEAD"))
    private static void preOnProcessPacket(Packet packetIn, INetHandler netHandler,
        CallbackInfo info) {
        if (packetIn instanceof ITransformablePacket) {
            ITransformablePacket transformPacket = (ITransformablePacket) packetIn;
            transformPacket.doPreProcessing((NetHandlerPlayServer) netHandler, true);
        }
    }

    @Inject(method = "onProcessPacket", at = @At(value = "RETURN"))
    private static void postOnProcessPacket(Packet packetIn, INetHandler netHandler,
        CallbackInfo info) {
        if (packetIn instanceof ITransformablePacket) {
            ITransformablePacket transformPacket = (ITransformablePacket) packetIn;
            transformPacket.doPostProcessing((NetHandlerPlayServer) netHandler, true);
        }
    }

}
