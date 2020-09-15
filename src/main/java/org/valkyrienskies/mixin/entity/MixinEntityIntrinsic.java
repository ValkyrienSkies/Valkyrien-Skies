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
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityCollisionInjector;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityCollisionInjector.IntermediateMovementVariableStorage;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityMoveInjectionMethods;
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
    @Shadow
    public boolean collided;

    private final Entity thisClassAsAnEntity = Entity.class.cast(this);
    private final IDraggable thisClassAsDraggable = IDraggable.class.cast(this);
    // Used to remember alteredMovement, so that it can be passed from changeMoveArgs() to postMove()
    private IntermediateMovementVariableStorage alteredMovement = null;
    // We only want to run onEntityPreMove() code when Minecraft calls Entity.move(), not when we call it ourselves.
    // We use this boolean to keep track of when we've called move() ourselves.
    private boolean didMinecraftInvokeMove = true;

    @Shadow
    public abstract void move(MoverType type, double x, double y, double z);

    /**
     * The goal of this injection is to adjust the arguments passed into move(). By adjusting the arguments we can add
     * collision between entities and ships.
     */
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void onEntityPreMove(MoverType type, double dx, double dy, double dz,
        CallbackInfo callbackInfo) {
        // Only run this code if Minecraft invoked move().
        if (didMinecraftInvokeMove) {
            alteredMovement = EntityMoveInjectionMethods
                .handleMove(type, dx, dy, dz, thisClassAsAnEntity);
            if (alteredMovement != null) {
                // We're about to invoke move, so set didMinecraftInvokeMove to false.
                didMinecraftInvokeMove = false;
                this.move(type, alteredMovement.dxyz.x(), alteredMovement.dxyz.y(),
                    alteredMovement.dxyz.z());
                // Now our invocation of move has finished, so set didMinecraftInvokeMove back to true.
                didMinecraftInvokeMove = true;
                // Since we've already called move() in the code above, we must cancel the original invocation of move.
                // If we didn't cancel the original invocation then move() would be twice, which is obviously wrong.
                callbackInfo.cancel();
            }
        }
    }

    /**
     * The goal of this injection is to correctly setup {@link IDraggable#getEntityShipMovementData()} for this Entity.
     * Specifically this code handles the last ship touched by the entity, as well as how many ticks ago that touch was.
     */
    @Inject(method = "move", at = @At("RETURN"))
    private void onEntityPostMove(CallbackInfo callbackInfo) {
        final EntityShipMovementData oldEntityShipMovementData = thisClassAsDraggable.getEntityShipMovementData();
        if (alteredMovement != null) {
            // If alteredMovement isn't null then we're touching a ship.
            final EntityShipMovementData newEntityShipMovementData = oldEntityShipMovementData
                    .withLastTouchedShip(alteredMovement.shipTouched)
                    .withTicksSinceTouchedShip(0)
                    .withTicksPartOfGround(0);
            thisClassAsDraggable.setEntityShipMovementData(newEntityShipMovementData);
            EntityCollisionInjector.alterEntityMovementPost(thisClassAsAnEntity, alteredMovement);
        } else {
            if (this.collided) {
                // If we collided and alteredMovement is null, then we're touching the ground.
                final int newTicksPartOfGround = oldEntityShipMovementData.getTicksPartOfGround() + 1;
                final EntityShipMovementData newEntityShipMovementData = new EntityShipMovementData(
                        null, 0, newTicksPartOfGround, new Vector3d(), 0
                );
                thisClassAsDraggable.setEntityShipMovementData(newEntityShipMovementData);
            } else {
                // If we're not collided and alteredMovement is null, then we're in the air.
                final int newTicksPartOfGround;
                if (oldEntityShipMovementData.getLastTouchedShip() != null) {
                    newTicksPartOfGround = 0;
                } else {
                    newTicksPartOfGround = oldEntityShipMovementData.getTicksPartOfGround() + 1;
                }
                final EntityShipMovementData newEntityShipMovementData = oldEntityShipMovementData
                        .withTicksSinceTouchedShip(oldEntityShipMovementData.getTicksSinceTouchedShip() + 1)
                        .withTicksPartOfGround(newTicksPartOfGround);
                thisClassAsDraggable.setEntityShipMovementData(newEntityShipMovementData);
            }
        }
    }
}
