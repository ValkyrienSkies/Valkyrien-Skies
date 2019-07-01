/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.PhysicsWrapperEntity;

@Mixin(Entity.class)
public abstract class MixinEntityClient {

    @Shadow
    public double posX;
    @Shadow
    public double posY;
    @Shadow
    public double posZ;

    @Shadow
    public float getEyeHeight() {
        return 0.0f;
    }

    @Inject(method = "getPositionEyes(F)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    public void getPositionEyesInject(float partialTicks, CallbackInfoReturnable<Vec3d> callbackInfo) {
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getShipFixedOnto(Entity.class.cast(this));

        if (wrapper != null) {
            Vector playerPosition = new Vector(wrapper.getPhysicsObject().getLocalPositionForEntity(Entity.class.cast(this)));
            wrapper.getPhysicsObject().getShipTransformationManager().getRenderTransform().transform(playerPosition,
                    TransformType.SUBSPACE_TO_GLOBAL);

            Vector playerEyes = new Vector(0, this.getEyeHeight(), 0);
            // Remove the original position added for the player's eyes
            // RotationMatrices.doRotationOnly(wrapper.wrapping.coordTransform.lToWTransform,
            // playerEyes);
            wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().rotate(playerEyes, TransformType.SUBSPACE_TO_GLOBAL);
            // Add the new rotate player eyes to the position
            playerPosition.add(playerEyes);
            callbackInfo.setReturnValue(playerPosition.toVec3d());
            callbackInfo.cancel(); // return the value, as opposed to the default one
        }
    }
}
