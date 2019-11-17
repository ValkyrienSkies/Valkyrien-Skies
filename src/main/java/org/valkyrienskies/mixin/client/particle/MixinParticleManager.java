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

package org.valkyrienskies.mixin.client.particle;

import java.util.Optional;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

@Mixin(ParticleManager.class)
public abstract class MixinParticleManager {

    @Inject(method = "addEffect", at = @At("HEAD"), cancellable = true)
    public void preAddEffect(Particle effect, CallbackInfo callbackInfoReturnable) {
        if (effect == null) {
            callbackInfoReturnable.cancel();
            return;
        }

        BlockPos pos = new BlockPos(effect.posX, effect.posY, effect.posZ);
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(effect.world, pos);
        if (physicsObject.isPresent()) {
            Vector posVec = new Vector(effect.posX, effect.posY, effect.posZ);
            Vector velocity = new Vector(effect.motionX, effect.motionY, effect.motionZ);
            physicsObject.get()
                .shipTransformationManager()
                .fromLocalToGlobal(posVec);
//            RotationMatrices.doRotationOnly(wrapper.wrapping.coordTransform.lToWTransform, velocity);
            physicsObject.get()
                .shipTransformationManager()
                .getCurrentTickTransform()
                .rotate(velocity, TransformType.SUBSPACE_TO_GLOBAL);
            effect.setPosition(posVec.X, posVec.Y, posVec.Z);
            effect.motionX = velocity.X;
            effect.motionY = velocity.Y;
            effect.motionZ = velocity.Z;
        }
        //vanilla code follows
    }
}
