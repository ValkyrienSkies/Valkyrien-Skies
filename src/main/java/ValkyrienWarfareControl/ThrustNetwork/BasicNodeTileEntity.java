package ValkyrienWarfareControl.ThrustNetwork;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class BasicNodeTileEntity extends TileEntity implements INodeProvider, ITickable {

	public final Node tileNode;

	public BasicNodeTileEntity(){
		tileNode = new Node(this);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, 0, writeToNBT(new NBTTagCompound()));
		return packet;
	}

	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		tileNode.readFromNBT(compound);
		super.readFromNBT(compound);
	}

	@Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.readFromNBT(tag);
    }

	@Override
    public NBTTagCompound getUpdateTag() {
		NBTTagCompound toReturn = super.getUpdateTag();

		return writeToNBT(toReturn);
    }


	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		tileNode.writeToNBT(compound);
		return super.writeToNBT(compound);
	}

	@Override
	public Node getNode() {
		return tileNode;
	}

	@Override
    public void invalidate() {
//		System.out.println("Please RNGesus!");
		//The Node just got destroyed
        this.tileEntityInvalid = true;
        Node toInvalidate = getNode();

        toInvalidate.destroyNode();
    }

    /**
     * validates a tile entity
     */
	@Override
    public void validate() {
        this.tileEntityInvalid = false;
    }

	@Override
	public void update() {

	}
}
