package valkyrienwarfare.addon.control.network;

import valkyrienwarfare.addon.control.piloting.ITileEntityPilotable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePlayerStoppedPilotingHandler implements IMessageHandler<MessagePlayerStoppedPiloting, IMessage> {

	@Override
	public IMessage onMessage(MessagePlayerStoppedPiloting message, MessageContext ctx) {
		IThreadListener mainThread = ctx.getServerHandler().serverController;
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				BlockPos pos = message.posToStopPiloting;
				EntityPlayerMP player = ctx.getServerHandler().player;

				TileEntity tileEntity = player.world.getTileEntity(pos);

				if (tileEntity instanceof ITileEntityPilotable) {
					((ITileEntityPilotable) tileEntity).playerWantsToStopPiloting(player);
				}
			}
		});
		return null;
	}

}
