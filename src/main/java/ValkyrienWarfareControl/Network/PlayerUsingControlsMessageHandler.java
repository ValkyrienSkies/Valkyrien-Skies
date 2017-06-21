package ValkyrienWarfareControl.Network;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.Piloting.ClientPilotingManager;
import ValkyrienWarfareControl.Piloting.ControllerInputType;
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
                        ClientPilotingManager.setPilotedWrapperEntity(wrapper);
                        ClientPilotingManager.blockBeingControlled = message.controlBlockPos;
                        ClientPilotingManager.currentControllerInput = ControllerInputType.ShipHelm;
                    }
                }
            }
        });
        return null;
    }

}
