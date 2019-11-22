/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

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
