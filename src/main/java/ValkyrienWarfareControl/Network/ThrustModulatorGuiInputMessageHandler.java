package ValkyrienWarfareControl.Network;

import ValkyrienWarfareControl.TileEntity.ThrustModulatorTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ThrustModulatorGuiInputMessageHandler implements IMessageHandler<ThrustModulatorGuiInputMessage, IMessage> {

	@Override
	public IMessage onMessage(ThrustModulatorGuiInputMessage message, MessageContext ctx) {
		IThreadListener mainThread = ctx.getServerHandler().serverController;
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				TileEntity tileEnt = ctx.getServerHandler().player.world.getTileEntity(message.tileEntityPos);
				if (tileEnt != null) {
					if (tileEnt instanceof ThrustModulatorTileEntity) {
						((ThrustModulatorTileEntity) tileEnt).handleGUIInput(message, ctx);
					}
				} else {
					System.out.println("Player: " + ctx.getServerHandler().player.getName() + " sent a broken packet");
				}
			}
		});
		return null;
	}

}
