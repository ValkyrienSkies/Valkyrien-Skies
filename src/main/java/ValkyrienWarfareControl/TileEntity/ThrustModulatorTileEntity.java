package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.API.Block.EtherCompressor.TileEntityEtherCompressor;
import ValkyrienWarfareControl.ThrustNetwork.BasicNodeTileEntity;
import ValkyrienWarfareControl.ThrustNetwork.Node;
import ValkyrienWarfareControl.ThrustNetwork.NodeNetwork;
import net.minecraft.tileentity.TileEntity;

public class ThrustModulatorTileEntity extends BasicNodeTileEntity{

	public ThrustModulatorTileEntity(){

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
				TileEntityEtherCompressor etherTile = (TileEntityEtherCompressor) nodeTile;

//				etherTile.linearThrust.Y = 500D;
			}
		}
	}

}
