package org.valkyrienskies.mixin.client.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityShipMountData;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    private final EntityPlayerSP player = EntityPlayerSP.class.cast(this);
    @Final
    @Shadow
    public NetHandlerPlayClient connection;

    /**
     * @author Tri0de
     * @reason Fixes player ray tracing when they're on a ship thats rotating.
     */
    @Overwrite
    public Vec3d getLook(final float partialTicks) {
        final Vec3d playerLook;
        if (partialTicks == 1.0F) {
            playerLook = getVectorForRotationInMc_1_12(player.rotationPitch, player.rotationYawHead);
        } else {
            final float playerPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
            final float playerYaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
            playerLook = this.getVectorForRotationInMc_1_12(playerPitch, playerYaw);
        }

        // If the player is mounted to a ship then we must rotate the player look vector.
        final EntityShipMountData mountData = ValkyrienUtils
                .getMountedShipAndPos(player);
        if (mountData.isMounted()) {
            return mountData.getMountedShip()
                    .getShipTransformationManager()
                    .getRenderTransform()
                    .rotate(playerLook, TransformType.SUBSPACE_TO_GLOBAL);
        } else {
            return playerLook;
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
