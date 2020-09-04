package org.valkyrienskies.mixin.network.play.client;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.GameType;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.fixes.ITransformablePacket;
import org.valkyrienskies.mod.common.util.VSMath;
import org.valkyrienskies.mod.common.ships.entity_interaction.IDraggable;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.JOML;

@Mixin(CPacketPlayer.class)
public class MixinCPacketPlayer implements ITransformablePacket {

    @Shadow
    public float pitch;
    private final CPacketPlayer thisPacket = CPacketPlayer.class.cast(this);
    private GameType cachedPlayerGameType = null;

    @Inject(method = "processPacket", at = @At(value = "HEAD"))
    private void preDiggingProcessPacket(INetHandlerPlayServer server, CallbackInfo info) {
        this.doPreProcessing(server, false);
    }

    @Inject(method = "processPacket", at = @At(value = "RETURN"))
    private void postDiggingProcessPacket(INetHandlerPlayServer server, CallbackInfo info) {
        this.doPostProcessing(server, false);
    }

    @Override
    public void doPreProcessing(INetHandlerPlayServer server, boolean callingFromSponge) {
        if (isPacketOnMainThread(server, callingFromSponge)) {
            ShipData parent = getPacketParent((NetHandlerPlayServer) server);
            if (parent != null) {
                EntityPlayerMP playerMP = getPacketPlayer(server);
                Vector3dc positionGlobal = new Vector3d(playerMP.posX, playerMP.posY, playerMP.posZ);
                Vector3dc lookVectorGlobal = JOML.convert(playerMP.getLook(1.0f));

                // ==== Get the player pitch/yaw from the look vector =====
                final Tuple<Double, Double> pitchYawTuple = VSMath.getPitchYawFromVector(lookVectorGlobal);
                float pitch = pitchYawTuple.getFirst().floatValue();
                float yaw = pitchYawTuple.getSecond().floatValue();

                // ===== Set the proper position values for the player packet =====
                thisPacket.moving = true;
                thisPacket.onGround = true;
                thisPacket.x = positionGlobal.x();
                thisPacket.y = positionGlobal.y();
                thisPacket.z = positionGlobal.z();

                // ===== Set the proper rotation values for the player packet =====
                thisPacket.rotating = true;
                thisPacket.yaw = yaw;
                thisPacket.pitch = pitch;

                // ===== Dangerous code here =====
                cachedPlayerGameType = getPacketPlayer(server).interactionManager.gameType;
                getPacketPlayer(server).interactionManager.gameType = GameType.CREATIVE;
            }
        }
    }

    @Override
    public void doPostProcessing(INetHandlerPlayServer server, boolean callingFromSponge) {
        if (isPacketOnMainThread(server, callingFromSponge)) {
            // ===== Dangerous code here =====
            if (cachedPlayerGameType != null) {
                getPacketPlayer(server).interactionManager.gameType = cachedPlayerGameType;
            }
        }
    }

    @Override
    public ShipData getPacketParent(NetHandlerPlayServer server) {
        return null;
    }

    private EntityPlayerMP getPacketPlayer(INetHandlerPlayServer server) {
        return ((NetHandlerPlayServer) server).player;
    }

}
