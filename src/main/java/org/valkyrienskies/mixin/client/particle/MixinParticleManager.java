package org.valkyrienskies.mixin.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.ship_handling.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.Optional;

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
            Vector3d posVec = new Vector3d(effect.posX, effect.posY, effect.posZ);
            Vector3d velocity = new Vector3d(effect.motionX, effect.motionY, effect.motionZ);

            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .transformPosition(posVec, TransformType.SUBSPACE_TO_GLOBAL);
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .transformDirection(velocity, TransformType.SUBSPACE_TO_GLOBAL);

            effect.setPosition(posVec.x, posVec.y, posVec.z);
            effect.motionX = velocity.x;
            effect.motionY = velocity.y;
            effect.motionZ = velocity.z;
        }
    }
}
