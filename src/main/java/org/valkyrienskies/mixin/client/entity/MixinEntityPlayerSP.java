package org.valkyrienskies.mixin.client.entity;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    private final EntityPlayerSP player = EntityPlayerSP.class.cast(this);
    @Shadow
    private double lastReportedPosX;
    @Shadow
    private double lastReportedPosY;
    @Shadow
    private double lastReportedPosZ;
    @Shadow
    private float lastReportedYaw;
    @Shadow
    private float lastReportedPitch;
    @Shadow
    private int positionUpdateTicks;

    @Inject(method = "onUpdateWalkingPlayer()V", at = @At("RETURN"))
    private void postOnUpdateWalkingPlayer(CallbackInfo ci) {
        if (isCurrentViewEntity()) {
            if (player.isRiding()) {
                player.connection.sendPacket(
                        new CPacketPlayer.PositionRotation(player.motionX, -999.0D, player.motionZ,
                                player.rotationYaw, player.rotationPitch, player.onGround));
            } else {
                AxisAlignedBB axisalignedbb = player.getEntityBoundingBox();
                player.connection.sendPacket(
                        new CPacketPlayer.PositionRotation(player.posX, axisalignedbb.minY, player.posZ,
                                player.rotationYaw, player.rotationPitch, player.onGround));
                lastReportedPosX = player.posX;
                lastReportedPosY = axisalignedbb.minY;
                lastReportedPosZ = player.posZ;
                positionUpdateTicks = 0;
            }
            lastReportedYaw = player.rotationYaw;
            lastReportedPitch = player.rotationPitch;
        }
    }

    @Shadow
    protected boolean isCurrentViewEntity() {
        return false;
    }
}
