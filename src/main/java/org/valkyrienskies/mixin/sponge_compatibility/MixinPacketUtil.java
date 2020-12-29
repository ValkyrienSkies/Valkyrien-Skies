package org.valkyrienskies.mixin.sponge_compatibility;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.fixes.ITransformablePacket;

/**
 * Necessary mixin.
 */
@Mixin(Chunk.class)
public class MixinPacketUtil {

    /*
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

     */

}
