package org.valkyrienskies.mixin.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.addon.control.piloting.ControllerInputType;
import org.valkyrienskies.addon.control.piloting.IShipPilot;

/**
 * Lazy programming, this can probably be replaced with forge hooks.
 *
 * @author thebest108
 */
@Deprecated
@Mixin(MovementInputFromOptions.class)
public abstract class MixinMovementInputFromOptions extends MovementInput {

    @Inject(method = "updatePlayerMoveState", at = @At("HEAD"), cancellable = true)
    public void preUpdatePlayerMoveState(CallbackInfo callbackInfo) {
        IShipPilot pilot = (IShipPilot) Minecraft.getMinecraft().player;

        if (pilot != null) {
            ControllerInputType inputTypeEnum = pilot.getControllerInputEnum();
            if (inputTypeEnum != null) {
                if (inputTypeEnum.shouldLockPlayerMovement()) {
                    // Then don't let the player move anymore while it's piloting this bastard
                    zeroAllPlayerMovements();
                    callbackInfo.cancel();
                }
            }
        }
    }

    /**
     * Stops all player movement.
     */
    private void zeroAllPlayerMovements() {
        this.moveStrafe = 0;
        this.moveForward = 0;
        this.jump = false;
    }
}
