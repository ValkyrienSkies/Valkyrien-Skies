package org.valkyrienskies.mixin.client.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.coordinates.ISubspacedEntity;
import org.valkyrienskies.mod.common.coordinates.ISubspacedEntityRecord;
import org.valkyrienskies.mod.common.network.SubspacedEntityRecordMessage;
import org.valkyrienskies.mod.common.physmanagement.interaction.IDraggable;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    private final ISubspacedEntity thisAsSubspaced = (ISubspacedEntity) this;
    private final EntityPlayerSP player = EntityPlayerSP.class.cast(this);
    @Shadow
    protected Minecraft mc;
    @Shadow
    private boolean serverSneakState;
    @Shadow
    private boolean serverSprintState;
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

    /**
     * @reason is because we need to ensure the CPacketPlayer is always sent no matter what.
     * @author thebest108
     */
    @Overwrite
    private void onUpdateWalkingPlayer() {
        // ===== Injection code starts here =====

        IDraggable draggable = (IDraggable) this;
        if (draggable.getWorldBelowFeet() != null) {
            draggable.getWorldBelowFeet().getPhysicsObject().getSubspace().snapshotSubspacedEntity(thisAsSubspaced);
            ISubspacedEntityRecord entityRecord = draggable.getWorldBelowFeet().getPhysicsObject().getSubspace()
                    .getRecordForSubspacedEntity(thisAsSubspaced);
            SubspacedEntityRecordMessage recordMessage = new SubspacedEntityRecordMessage(entityRecord);
            ValkyrienSkiesMod.physWrapperNetwork.sendToServer(recordMessage);
        }

        // ===== Injection code ends here =====

        boolean flag = player.isSprinting();

        if (flag != serverSprintState) {
            if (flag) {
                player.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.START_SPRINTING));
            } else {
                player.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            serverSprintState = flag;
        }

        boolean flag1 = player.isSneaking();

        if (flag1 != serverSneakState) {
            if (flag1) {
                player.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING));
            } else {
                player.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            serverSneakState = flag1;
        }

        if (isCurrentViewEntity()) {
            AxisAlignedBB axisalignedbb = player.getEntityBoundingBox();
            double d0 = player.posX - lastReportedPosX;
            double d1 = axisalignedbb.minY - lastReportedPosY;
            double d2 = player.posZ - lastReportedPosZ;
            double d3 = (double) (player.rotationYaw - lastReportedYaw);
            double d4 = (double) (player.rotationPitch - lastReportedPitch);
            ++positionUpdateTicks;
            // Always true because why not.
            boolean flag2 = true; // d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || positionUpdateTicks >= 20;
            boolean flag3 = true; // d3 != 0.0D || d4 != 0.0D;

            if (player.isRiding()) {
                player.connection.sendPacket(new CPacketPlayer.PositionRotation(player.motionX, -999.0D, player.motionZ, player.rotationYaw, player.rotationPitch, player.onGround));
                flag2 = false;
            } else if (flag2 && flag3) {
                player.connection.sendPacket(new CPacketPlayer.PositionRotation(player.posX, axisalignedbb.minY, player.posZ, player.rotationYaw, player.rotationPitch, player.onGround));
            } else if (flag2) {
                player.connection.sendPacket(new CPacketPlayer.Position(player.posX, axisalignedbb.minY, player.posZ, player.onGround));
            } else if (flag3) {
                player.connection.sendPacket(new CPacketPlayer.Rotation(player.rotationYaw, player.rotationPitch, player.onGround));
            }

            if (flag2) {
                lastReportedPosX = player.posX;
                lastReportedPosY = axisalignedbb.minY;
                lastReportedPosZ = player.posZ;
                positionUpdateTicks = 0;
            }

            if (flag3) {
                lastReportedYaw = player.rotationYaw;
                lastReportedPitch = player.rotationPitch;
            }

            prevOnGround = player.onGround;
            autoJumpEnabled = mc.gameSettings.autoJump;
        }
    }

    @Shadow
    protected boolean isCurrentViewEntity() {
        return false;
    }
}
