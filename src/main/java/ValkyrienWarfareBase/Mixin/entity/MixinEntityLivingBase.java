package ValkyrienWarfareBase.Mixin.entity;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {

    /**
     * This constructor is needed to make javac happy
     */
    public MixinEntityLivingBase() {
        super(null);
    }

    @Inject(method = "dismountEntity", at = @At("HEAD"), cancellable = true)
    public void dismountEntity(Entity entityIn, CallbackInfo info)   {
        if(entityIn instanceof PhysicsWrapperEntity){
            this.ridingEntity = null;
            this.posY += 1.45D;
            info.cancel(); // effectively a premature method return
        }
    }
}
