package org.valkyrienskies.mixin.entity;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.ships.entity_interaction.IDraggable;

@Mixin(EntityLivingBase.class)
public class MixinEntityLivingBase {

    /**
     * This mixin allows players to breathe underwater when they're in an air pocket.
     */
    @Inject(method = "canBreatheUnderwater", at = @At("HEAD"), cancellable = true)
    private void onPreCanBreatheUnderwater(CallbackInfoReturnable<Boolean> cir) {
        final IDraggable draggable = (IDraggable) this;
        final boolean isInAirPocket = draggable.getInAirPocket();
        if (isInAirPocket) {
            cir.setReturnValue(true);
        }
    }
}
