package valkyrienwarfare.mixin.client.entity;

import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.IShipPilotClient;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinsAbstractClientPlayer implements IShipPilotClient {

	@Override
	public void onClientTick() {
		if (isPiloting()) {
			sendPilotKeysToServer(this.getControllerInputEnum(), getPilotedShip(), getPosBeingControlled());
		}

	}

	private void sendPilotKeysToServer(ControllerInputType type, PhysicsWrapperEntity shipPiloting, BlockPos blockBeingControlled) {
		PilotControlsMessage keyMessage = new PilotControlsMessage();
		if (type == null) {
			System.out.println("This is totally wrong");
			type = ControllerInputType.PilotsChair;
		}
//		System.out.println(blockBeingControlled);
		keyMessage.assignKeyBooleans(shipPiloting, type);
		keyMessage.controlBlockPos = blockBeingControlled;


		ValkyrienWarfareControl.controlNetwork.sendToServer(keyMessage);
	}

}
