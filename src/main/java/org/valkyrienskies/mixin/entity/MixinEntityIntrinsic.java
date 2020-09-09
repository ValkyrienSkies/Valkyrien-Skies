package org.valkyrienskies.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.entity.EntityShipMovementData;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityMoveInjectionMethods;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityCollisionInjector;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityCollisionInjector.IntermediateMovementVariableStorage;
import org.valkyrienskies.mod.common.ships.entity_interaction.IDraggable;

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

    @Shadow public boolean collided;

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void changeMoveArgs(MoverType type, double dx, double dy, double dz,
        CallbackInfo callbackInfo) {
        if (!hasChanged) {
            alteredMovement = EntityMoveInjectionMethods
                .handleMove(type, dx, dy, dz, thisClassAsAnEntity);
            if (alteredMovement != null) {
                hasChanged = true;
                this.move(type, alteredMovement.dxyz.x(), alteredMovement.dxyz.y(),
                    alteredMovement.dxyz.z());
                hasChanged = false;
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void postMove(CallbackInfo callbackInfo) {
        final IDraggable entityDraggable = IDraggable.class.cast(this);
        final EntityShipMovementData oldEntityShipMovementData = entityDraggable.getEntityShipMovementData();
        if (alteredMovement != null) {
            // If alteredMovement isn't null then we're touching a ship.
            final EntityShipMovementData newEntityShipMovementData = oldEntityShipMovementData
                    .withLastTouchedShip(alteredMovement.shipTouched)
                    .withTicksSinceTouchedShip(0);
            entityDraggable.setEntityShipMovementData(newEntityShipMovementData);
            EntityCollisionInjector.alterEntityMovementPost(thisClassAsAnEntity, alteredMovement);
        } else {
            if (this.collided) {
                // If we collided and alteredMovement is null, then we're touching the ground.
                final EntityShipMovementData newEntityShipMovementData = new EntityShipMovementData(
                        null, 0, new Vector3d(), 0
                );
                entityDraggable.setEntityShipMovementData(newEntityShipMovementData);
            } else {
                // If we're not collided and alteredMovement is null, then we're in the air.
                final EntityShipMovementData newEntityShipMovementData = oldEntityShipMovementData
                        .withTicksSinceTouchedShip(oldEntityShipMovementData.getTicksSinceTouchedShip() + 1);
                entityDraggable.setEntityShipMovementData(newEntityShipMovementData);
            }
        }
    }
}
