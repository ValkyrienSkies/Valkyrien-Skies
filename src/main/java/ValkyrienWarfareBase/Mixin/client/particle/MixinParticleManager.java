package ValkyrienWarfareBase.Mixin.client.particle;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {
	@Inject(method = "addEffect(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
	public void preAddEffect(Particle effect, CallbackInfo callbackInfoReturnable) {
		if (effect == null) {
			callbackInfoReturnable.cancel();
			return;
		}
		
		BlockPos pos = new BlockPos(effect.posX, effect.posY, effect.posZ);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(effect.world, pos);
		if (wrapper != null) {
			Vector posVec = new Vector(effect.posX, effect.posY, effect.posZ);
			Vector velocity = new Vector(effect.motionX, effect.motionY, effect.motionZ);
			wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWRotation, velocity);
			effect.setPosition(posVec.X, posVec.Y, posVec.Z);
			effect.motionX = velocity.X;
			effect.motionY = velocity.Y;
			effect.motionZ = velocity.Z;
		}
		//vanilla code follows
	}
}
