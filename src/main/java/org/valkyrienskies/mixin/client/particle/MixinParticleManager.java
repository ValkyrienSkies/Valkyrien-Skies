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
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
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
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(effect.world, pos);
        if (physicsObject.isPresent()) {
            Vector posVec = new Vector(effect.posX, effect.posY, effect.posZ);
            Vector velocity = new Vector(effect.motionX, effect.motionY, effect.motionZ);
            physicsObject.get()
                .getShipTransformationManager()
                .fromLocalToGlobal(posVec);
//            RotationMatrices.doRotationOnly(wrapper.wrapping.coordTransform.lToWTransform, velocity);
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .rotate(velocity, TransformType.SUBSPACE_TO_GLOBAL);
            effect.setPosition(posVec.x, posVec.y, posVec.z);
            effect.motionX = velocity.x;
            effect.motionY = velocity.y;
            effect.motionZ = velocity.z;
        }
        //vanilla code follows
    }
}
