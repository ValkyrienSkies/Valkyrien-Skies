package valkyrienwarfare.mod.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import valkyrienwarfare.mod.gui.IVWTileGui;

public class VWGuiButtonHandler implements IMessageHandler<VWGuiButtonMessage, IMessage> {

    @Override
    public IMessage onMessage(VWGuiButtonMessage message, MessageContext ctx) {
        IThreadListener mainThread = ctx.getServerHandler().server;
        mainThread.addScheduledTask(() -> {
            World playerWorld = ctx.getServerHandler().player.world;
            TileEntity tileEntity = playerWorld.getTileEntity(message.getTileEntityPos());
            if (tileEntity == null) {
                // Nothing there, ignore this message
                return;
            }
            int buttonId = message.getButtonId();
            // Tell the tile entity that this player tried pressing the given button.
            if (tileEntity instanceof IVWTileGui) {
                ((IVWTileGui) tileEntity).onButtonPress(buttonId, ctx.getServerHandler().player);
            }
        });
        return null;
    }
}
