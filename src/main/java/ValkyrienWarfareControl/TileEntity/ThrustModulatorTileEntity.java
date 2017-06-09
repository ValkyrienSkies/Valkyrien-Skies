package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.API.Block.EtherCompressor.TileEntityEtherCompressor;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareControl.NodeNetwork.Node;
import ValkyrienWarfareControl.NodeNetwork.NodeNetwork;
import net.minecraft.tileentity.TileEntity;

public class ThrustModulatorTileEntity extends ImplPhysicsProcessorNodeTileEntity{

	public ThrustModulatorTileEntity(){
		super();
	}

	@Override
	public void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations, double secondsToSimulate) {

	}

	@Override
	public void update() {
		super.update();
		Node myNode = this.getNode();
		NodeNetwork nodeNetwork = myNode.getNodeNetwork();
		//Loop through all the networked nodes
		for(Node node : nodeNetwork.networkedNodes){
			TileEntity nodeTile = node.parentTile;
			if(nodeTile instanceof TileEntityEtherCompressor){
//				TileEntityEtherCompressor etherTile = (TileEntityEtherCompressor) nodeTile;

//				etherTile.setThrust(10000D);
//				System.out.println("shit");
//				etherTile.linearThrust.Y = 500D;
			}
		}
	}

}
