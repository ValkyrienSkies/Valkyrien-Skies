package org.valkyrienskies.mixin.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.util.EntityShipMountData;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

@Mixin(RenderManager.class)
public abstract class MixinRenderManager {

    private boolean hasChanged = false;

    @Shadow
    public abstract void renderEntity(Entity entityIn, double x, double y, double z, float yaw,
        float partialTicks, boolean p_188391_10_);

    @Inject(method = "renderEntity",
        at = @At("HEAD"),
        cancellable = true)
    public void preDoRenderEntity(Entity entityIn, double x, double y, double z, float yaw,
        float partialTicks, boolean p_188391_10_, CallbackInfo callbackInfo) {
        if (!hasChanged) {
            EntityShipMountData mountData = ValkyrienUtils.getMountedShipAndPos(entityIn);

            if (mountData.isMounted()) {
                double oldPosX = entityIn.posX;
                double oldPosY = entityIn.posY;
                double oldPosZ = entityIn.posZ;

                double oldLastPosX = entityIn.lastTickPosX;
                double oldLastPosY = entityIn.lastTickPosY;
                double oldLastPosZ = entityIn.lastTickPosZ;

                Vector localPosition = new Vector(mountData.getMountPos());

                mountData.getMountedShip()
                    .getShipRenderer()
                    .applyRenderTransform(partialTicks);

                if (localPosition != null) {
                    localPosition = new Vector(localPosition);

                    localPosition.x -= mountData.getMountedShip()
                        .getShipRenderer().offsetPos.getX();
                    localPosition.y -= mountData.getMountedShip()
                        .getShipRenderer().offsetPos.getY();
                    localPosition.z -= mountData.getMountedShip()
                        .getShipRenderer().offsetPos.getZ();

                    x = entityIn.posX = entityIn.lastTickPosX = localPosition.x;
                    y = entityIn.posY = entityIn.lastTickPosY = localPosition.y;
                    z = entityIn.posZ = entityIn.lastTickPosZ = localPosition.z;
                }

                hasChanged = true;
                this.renderEntity(entityIn, x, y, z, yaw, partialTicks, p_188391_10_);
                hasChanged = false;


                if (localPosition != null) {
                    mountData.getMountedShip()
                        .getShipRenderer()
                        .inverseTransform(partialTicks);
                }

                entityIn.posX = oldPosX;
                entityIn.posY = oldPosY;
                entityIn.posZ = oldPosZ;

                entityIn.lastTickPosX = oldLastPosX;
                entityIn.lastTickPosY = oldLastPosY;
                entityIn.lastTickPosZ = oldLastPosZ;

                callbackInfo.cancel();
            }
        }
    }
}
