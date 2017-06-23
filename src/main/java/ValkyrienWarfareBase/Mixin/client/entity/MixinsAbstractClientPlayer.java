package ValkyrienWarfareBase.Mixin.client.entity;

import org.spongepowered.asm.mixin.Mixin;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.Piloting.ControllerInputType;
import ValkyrienWarfareControl.Piloting.IShipPilotClient;
import ValkyrienWarfareControl.Piloting.PilotControlsMessage;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.BlockPos;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinsAbstractClientPlayer implements IShipPilotClient {

	@Override
	public void onClientTick() {
		if (isPilotingShip()) {
            sendPilotKeysToServer(ControllerInputType.PilotsChair, getPilotedShip(), getPosBeingControlled());
		}

	}

    private void sendPilotKeysToServer(ControllerInputType type, PhysicsWrapperEntity shipPiloting, BlockPos blockBeingControlled) {
        PilotControlsMessage keyMessage = new PilotControlsMessage();
        if (type == null) {
            type = ControllerInputType.PilotsChair;
        }
//		System.out.println(blockBeingControlled);
        keyMessage.assignKeyBooleans(shipPiloting, type);
        keyMessage.controlBlockPos = blockBeingControlled;

        ValkyrienWarfareControlMod.controlNetwork.sendToServer(keyMessage);
    }

}
