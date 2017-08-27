package valkyrienwarfare.addon.control.tileentity;

import valkyrienwarfare.addon.control.controlsystems.ShipPulseImpulseControlSystem;
import valkyrienwarfare.physics.PhysicsCalculations;
import valkyrienwarfare.physicsmanagement.PhysicsObject;
import valkyrienwarfare.addon.control.network.ThrustModulatorGuiInputMessage;
import valkyrienwarfare.addon.control.nodenetwork.Node;
import valkyrienwarfare.addon.control.proxy.ClientProxyControl;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ThrustModulatorTileEntity extends ImplPhysicsProcessorNodeTileEntity {
	
	public ShipPulseImpulseControlSystem controlSystem;
	public double idealYHeight = 25D;
	public double maximumYVelocity = 10D;
	
	public ThrustModulatorTileEntity() {
		super();
		controlSystem = new ShipPulseImpulseControlSystem(this);
	}
	
	@Override
	public void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations, double secondsToSimulate) {
		controlSystem.solveThrustValues(calculations);
//    	System.out.println("test");
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
		idealYHeight = Math.min(message.idealYHeight, 5000D);
		maximumYVelocity = Math.max(Math.min(message.maximumYVelocity, 100D), 0D);
		Node thisTileEntitiesNode = this.getNode();
		thisTileEntitiesNode.sendUpdatesToNearby();
		this.markDirty();
	}
	
}
