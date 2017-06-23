package ValkyrienWarfareBase.Mixin.util;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import ValkyrienWarfareControl.Piloting.ControllerInputType;
import ValkyrienWarfareControl.Piloting.IShipPilot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions extends MovementInput {

	@Shadow @Final
    private GameSettings gameSettings;

	@Overwrite
	public void updatePlayerMoveState() {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;


		IShipPilot pilot = IShipPilot.class.cast(Minecraft.getMinecraft().player);

		if(pilot != null) {
			ControllerInputType inputTypeEnum = pilot.getControllerInputEnum();
			if(inputTypeEnum != null) {
				if(inputTypeEnum.shouldLockPlayerMovement()) {
					//Then don't let the player move anymore while it's piloting this bastard
					return;
				}
			}
		}

        if (this.gameSettings.keyBindForward.isKeyDown())
        {
            ++this.moveForward;
            this.forwardKeyDown = true;
        }
        else
        {
            this.forwardKeyDown = false;
        }

        if (this.gameSettings.keyBindBack.isKeyDown())
        {
            --this.moveForward;
            this.backKeyDown = true;
        }
        else
        {
            this.backKeyDown = false;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown())
        {
            ++this.moveStrafe;
            this.leftKeyDown = true;
        }
        else
        {
            this.leftKeyDown = false;
        }

        if (this.gameSettings.keyBindRight.isKeyDown())
        {
            --this.moveStrafe;
            this.rightKeyDown = true;
        }
        else
        {
            this.rightKeyDown = false;
        }

        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();

        if (this.sneak)
        {
            this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
            this.moveForward = (float)((double)this.moveForward * 0.3D);
        }
    }

}
