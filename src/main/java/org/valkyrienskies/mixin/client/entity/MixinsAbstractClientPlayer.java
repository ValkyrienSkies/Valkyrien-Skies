package org.valkyrienskies.mixin.client.entity;

import net.minecraft.client.entity.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.piloting.ControllerInputType;
import org.valkyrienskies.mod.common.piloting.IShipPilotClient;
import org.valkyrienskies.mod.common.piloting.PilotControlsMessage;

import java.util.UUID;

/**
 * Todo: Replace this with a capability
 */
@Deprecated
@Mixin(AbstractClientPlayer.class)
public abstract class MixinsAbstractClientPlayer implements IShipPilotClient {

    @Override
    public void onClientTick() {
        if (isPilotingShip() || isPilotingATile()) {
            final UUID shipId;
            if (getPilotedShip() != null) {
                shipId = getPilotedShip().getUuid();
            } else {
                shipId = getShipIDBeingControlled();
            }
            sendPilotKeysToServer(this.getControllerInputEnum(), shipId, this);
        }
    }

    private void sendPilotKeysToServer(ControllerInputType type, UUID shipPiloting,
                                       IShipPilotClient shipPilotClient) {
        PilotControlsMessage keyMessage = new PilotControlsMessage();
        if (type == null) {
            System.out.println("This is totally wrong");
            type = ControllerInputType.CaptainsChair;
        }
        // System.out.println(blockBeingControlled);
        keyMessage.assignKeyBooleans(shipPiloting, type);
        keyMessage.controlBlockPos = shipPilotClient.getPosBeingControlled();

        ValkyrienSkiesMod.controlNetwork.sendToServer(keyMessage);
    }

}
