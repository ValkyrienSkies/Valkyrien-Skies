package ValkyrienWarfareControl.Network;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.GUI.HovercraftControllerGUI;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class HovercraftControllerGUIInputMessage implements IMessage {

	public BlockPos tilePos;
	public int physEntId;

	public double newIdealHeight;
	public double newStablitiyBias;
	public double newLinearVelocityBias;

	public HovercraftControllerGUIInputMessage() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tilePos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		physEntId = buf.readInt();
		newIdealHeight = buf.readDouble();
		newStablitiyBias = buf.readDouble();
		newLinearVelocityBias = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(tilePos.getX());
		buf.writeInt(tilePos.getY());
		buf.writeInt(tilePos.getZ());
		buf.writeInt(physEntId);
		buf.writeDouble(newIdealHeight);
		buf.writeDouble(newStablitiyBias);
		buf.writeDouble(newLinearVelocityBias);
	}

}
