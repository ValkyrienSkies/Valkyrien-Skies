package org.valkyrienskies.mixin.client.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.network.MessagePlayerOnShipPos;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.entity_interaction.IDraggable;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.Vector;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    private final EntityPlayerSP player = EntityPlayerSP.class.cast(this);
    @Final
    @Shadow
    public NetHandlerPlayClient connection;
    @Shadow
    private boolean serverSprintState;
    @Shadow
    private boolean serverSneakState;
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
    @Shadow
    private boolean prevOnGround;
    @Shadow
    private boolean autoJumpEnabled;
    @Shadow
    protected Minecraft mc;

    @Shadow
    public abstract boolean isSneaking();
    @Shadow
    protected abstract boolean isCurrentViewEntity();

    /**
     * The purpose of this mixin is to send the server information about the ship the player is currently standing on.
     * @author  Tri0de
     */
    @Overwrite
    private void onUpdateWalkingPlayer() {
        boolean flag = player.isSprinting();

        if (flag != this.serverSprintState) {
            if (flag) {
                this.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.START_SPRINTING));
            } else {
                this.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
            this.serverSprintState = flag;
        }

        boolean flag1 = this.isSneaking();

        if (flag1 != this.serverSneakState) {
            if (flag1) {
                this.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING));
            } else {
                this.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
            this.serverSneakState = flag1;
        }

        if (this.isCurrentViewEntity()) {
            AxisAlignedBB axisalignedbb = player.getEntityBoundingBox();
            double d0 = player.posX - this.lastReportedPosX;
            double d1 = axisalignedbb.minY - this.lastReportedPosY;
            double d2 = player.posZ - this.lastReportedPosZ;
            double d3 = (double)(player.rotationYaw - this.lastReportedYaw);
            double d4 = (double)(player.rotationPitch - this.lastReportedPitch);
            ++this.positionUpdateTicks;
            boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || this.positionUpdateTicks >= 20;
            boolean flag3 = d3 != 0.0D || d4 != 0.0D;

            final ShipData worldBelowFeet = ValkyrienUtils.getLastShipTouchedByEntity(player);
            if (worldBelowFeet != null) {
                final ShipTransform shipTransform = worldBelowFeet.getShipTransform();
                final Vector3d playerPosInLocal = new Vector3d(player.posX, player.posY, player.posZ);
                shipTransform.transformPosition(playerPosInLocal, TransformType.GLOBAL_TO_SUBSPACE);
                final Vector3d playerLookInLocal = JOML.convert(player.getLook(1));
                shipTransform.transformDirection(playerLookInLocal, TransformType.GLOBAL_TO_SUBSPACE);

                final MessagePlayerOnShipPos messagePlayerOnShipPos = new MessagePlayerOnShipPos(
                        worldBelowFeet.getUuid(),
                        playerPosInLocal,
                        playerLookInLocal,
                        player.onGround,
                        true,
                        true
                );
                ValkyrienSkiesMod.physWrapperNetwork.sendToServer(messagePlayerOnShipPos);

                this.lastReportedPosX = player.posX;
                this.lastReportedPosY = axisalignedbb.minY;
                this.lastReportedPosZ = player.posZ;
                this.positionUpdateTicks = 0;

                this.lastReportedYaw = player.rotationYaw;
                this.lastReportedPitch = player.rotationPitch;
                this.prevOnGround = player.onGround;
                this.autoJumpEnabled = this.mc.gameSettings.autoJump;
            } else {
                if (player.isRiding()) {
                    this.connection.sendPacket(new CPacketPlayer.PositionRotation(player.motionX, -999.0D, player.motionZ, player.rotationYaw, player.rotationPitch, player.onGround));
                    flag2 = false;
                } else if (flag2 && flag3) {
                    this.connection.sendPacket(new CPacketPlayer.PositionRotation(player.posX, axisalignedbb.minY, player.posZ, player.rotationYaw, player.rotationPitch, player.onGround));
                } else if (flag2) {
                    this.connection.sendPacket(new CPacketPlayer.Position(player.posX, axisalignedbb.minY, player.posZ, player.onGround));
                } else if (flag3) {
                    this.connection.sendPacket(new CPacketPlayer.Rotation(player.rotationYaw, player.rotationPitch, player.onGround));
                } else if (this.prevOnGround != player.onGround) {
                    this.connection.sendPacket(new CPacketPlayer(player.onGround));
                }

                if (flag2) {
                    this.lastReportedPosX = player.posX;
                    this.lastReportedPosY = axisalignedbb.minY;
                    this.lastReportedPosZ = player.posZ;
                    this.positionUpdateTicks = 0;
                }

                if (flag3) {
                    this.lastReportedYaw = player.rotationYaw;
                    this.lastReportedPitch = player.rotationPitch;
                }

                this.prevOnGround = player.onGround;
                this.autoJumpEnabled = this.mc.gameSettings.autoJump;
            }
        }
    }

    /**
     * @author Tri0de
     * @reason Fixes player ray tracing when they're on a ship thats rotating.
     */
    @Overwrite
    public Vec3d getLook(float partialTicks) {
        if (partialTicks == 1.0F) {
            return getVectorForRotationInMc_1_12(player.rotationPitch, player.rotationYawHead);
        } else {
            float f = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
            float f1 = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
            return this.getVectorForRotationInMc_1_12(f, f1);
        }
    }

    // This is only valid for MC 1.12, may or may not be correct in future versions.
    private Vec3d getVectorForRotationInMc_1_12(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d(f1 * f2, f3, f * f2);
    }

}
