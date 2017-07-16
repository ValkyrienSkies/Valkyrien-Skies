package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareControl.NodeNetwork.BasicNodeTileEntity;
import ValkyrienWarfareControl.NodeNetwork.Node;

public class ThrustRelayTileEntity extends BasicNodeTileEntity {

    public ThrustRelayTileEntity() {
    	super();
    	Node node = this.getNode();
    	node.setIsNodeRelay(true);
    }

}
