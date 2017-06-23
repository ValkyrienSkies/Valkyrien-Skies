package ValkyrienWarfareControl.Network;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Piloting.ControllerInputType;
import ValkyrienWarfareControl.Piloting.IShipPilot;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PlayerUsingControlsMessageHandler implements IMessageHandler<PlayerUsingControlsMessage, IMessage> {

    @Override
    public IMessage onMessage(PlayerUsingControlsMessage message, MessageContext ctx) {
        IThreadListener mainThread = Minecraft.getMinecraft();
        mainThread.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(message.playerControllingID);

                if (player == Minecraft.getMinecraft().player) {
                    PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(Minecraft.getMinecraft().world, message.controlBlockPos);

                    if (wrapper != null) {
                    	IShipPilot shipPilot = IShipPilot.class.cast(player);
                    	shipPilot.setPilotedShip(wrapper);
                    	shipPilot.setPosBeingControlled(message.controlBlockPos);
                    	shipPilot.setControllerInputEnum(ControllerInputType.ShipHelm);
                    }
                }
            }
        });
        return null;
    }

}
