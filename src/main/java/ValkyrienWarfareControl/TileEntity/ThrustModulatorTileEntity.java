package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareControl.ControlSystems.StabilityHeightPIDControl;
import ValkyrienWarfareControl.Network.ThrustModulatorGuiInputMessage;
import ValkyrienWarfareControl.NodeNetwork.Node;
import ValkyrienWarfareControl.Proxy.ClientProxyControl;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ThrustModulatorTileEntity extends ImplPhysicsProcessorNodeTileEntity {

	public StabilityHeightPIDControl controlSystem;
	public double idealYHeight = 25D;
	public double maximumYVelocity = 10D;

    public ThrustModulatorTileEntity() {
        super();
        controlSystem = new StabilityHeightPIDControl(this);
    }

    @Override
    public void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations, double secondsToSimulate) {
    	controlSystem.solveThrustValues(calculations);
//    	System.out.println("test");
    }

    @Override
    public void update() {
        super.update();
//        Node myNode = this.getNode();
//        NodeNetwork nodeNetwork = myNode.getNodeNetwork();
//        idealYHeight = 25D;
//        maximumYVelocity = 10D;

    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
    	super.onDataPacket(net, pkt);
    	ClientProxyControl.checkForTextFieldUpdate(this);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
    	super.readFromNBT(compound);
    	idealYHeight = compound.getFloat("idealYHeight");
    	maximumYVelocity = compound.getFloat("maximumYVelocity");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    	compound = super.writeToNBT(compound);
        compound.setFloat("idealYHeight", (float) idealYHeight);
        compound.setFloat("maximumYVelocity", (float) maximumYVelocity);
        return compound;
    }

	public void handleGUIInput(ThrustModulatorGuiInputMessage message, MessageContext ctx) {
		idealYHeight = message.idealYHeight;
		maximumYVelocity = message.maximumYVelocity;
    	Node thisTileEntitiesNode = this.getNode();
    	thisTileEntitiesNode.sendUpdatesToNearby();
		this.markDirty();
	}

}
