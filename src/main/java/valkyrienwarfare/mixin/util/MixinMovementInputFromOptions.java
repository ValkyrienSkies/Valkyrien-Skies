/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mixin.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.IShipPilot;

/**
 * This mixin prevents the player from moving while they're piloting something,
 * by blocking the code that checks if the movement keys are down.
 *
 * @author thebest108
 */
@Mixin(MovementInputFromOptions.class)
public abstract class MixinMovementInputFromOptions extends MovementInput {

    @Inject(method = "updatePlayerMoveState", at = @At("HEAD"), cancellable = true)
    public void preUpdatePlayerMoveState(CallbackInfo callbackInfo) {
        IShipPilot pilot = IShipPilot.class.cast(Minecraft.getMinecraft().player);

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
