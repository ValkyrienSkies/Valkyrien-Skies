package ValkyrienWarfareControl.Network;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HovercraftControllerGUIInputHandler implements IMessageHandler<HovercraftControllerGUIInputMessage, IMessage> {

	@Override
	public IMessage onMessage(final HovercraftControllerGUIInputMessage message, final MessageContext ctx) {
		IThreadListener mainThread = ctx.getServerHandler().serverController;
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(ctx.getServerHandler().playerEntity.worldObj, message.tilePos);
				TileEntity tileEnt = wrapper.wrapping.VKChunkCache.getTileEntity(message.tilePos);
				if (tileEnt != null) {
					if (tileEnt instanceof TileEntityHoverController) {
						((TileEntityHoverController) tileEnt).handleGUIInput(message, ctx);
					}
				} else {
					System.out.println("Player: " + ctx.getServerHandler().playerEntity.getName() + " sent a broken packet");
				}
			}
		});
		return null;
	}

}
