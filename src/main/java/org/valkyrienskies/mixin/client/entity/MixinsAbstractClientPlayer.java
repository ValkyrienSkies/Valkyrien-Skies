package org.valkyrienskies.mixin.client.entity;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.piloting.ControllerInputType;
import org.valkyrienskies.addon.control.piloting.IShipPilotClient;
import org.valkyrienskies.addon.control.piloting.PilotControlsMessage;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

/**
 * Todo: Replace this with a capability
 */
@Deprecated
@Mixin(AbstractClientPlayer.class)
public abstract class MixinsAbstractClientPlayer implements IShipPilotClient {

    @Override
    public void onClientTick() {
        if (isPiloting()) {
            sendPilotKeysToServer(this.getControllerInputEnum(), getPilotedShip(),
                getPosBeingControlled());
        }
    }

    private void sendPilotKeysToServer(ControllerInputType type, PhysicsObject shipPiloting,
                                       BlockPos blockBeingControlled) {
        PilotControlsMessage keyMessage = new PilotControlsMessage();
        if (type == null) {
            System.out.println("This is totally wrong");
            type = ControllerInputType.CaptainsChair;
        }
        // System.out.println(blockBeingControlled);
        keyMessage.assignKeyBooleans(shipPiloting, type);
        keyMessage.controlBlockPos = blockBeingControlled;

        ValkyrienSkiesControl.controlNetwork.sendToServer(keyMessage);
    }

}
