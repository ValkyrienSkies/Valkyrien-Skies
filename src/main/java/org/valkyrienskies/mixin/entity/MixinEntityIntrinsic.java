package org.valkyrienskies.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.fixes.EntityMoveInjectionMethods;
import org.valkyrienskies.mod.common.physics.collision.EntityCollisionInjector;
import org.valkyrienskies.mod.common.physics.collision.EntityCollisionInjector.IntermediateMovementVariableStorage;

/**
 * Todo: Remove this mess, eventually.
 */
@Deprecated
@Mixin(value = Entity.class, priority = 1)
public abstract class MixinEntityIntrinsic {

    @Shadow
    public double posX;
    @Shadow
    public double posY;
    @Shadow
    public double posZ;
    @Shadow
    public World world;
    public Entity thisClassAsAnEntity = Entity.class.cast(this);
    private IntermediateMovementVariableStorage alteredMovement = null;
    private boolean hasChanged = false;

    @Shadow
    public abstract void move(MoverType type, double x, double y, double z);

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void changeMoveArgs(MoverType type, double dx, double dy, double dz,
        CallbackInfo callbackInfo) {
        if (!hasChanged) {
            alteredMovement = EntityMoveInjectionMethods
                .handleMove(type, dx, dy, dz, thisClassAsAnEntity);
            if (alteredMovement != null) {
                hasChanged = true;
                this.move(type, alteredMovement.dxyz.x, alteredMovement.dxyz.y,
                    alteredMovement.dxyz.z);
                hasChanged = false;
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "move", at = @At("RETURN"))
    public void postMove(CallbackInfo callbackInfo) {
        if (hasChanged) {
            EntityCollisionInjector.alterEntityMovementPost(thisClassAsAnEntity, alteredMovement);
        }
    }
}
