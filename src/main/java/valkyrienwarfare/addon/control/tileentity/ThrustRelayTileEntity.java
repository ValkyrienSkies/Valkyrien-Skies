package valkyrienwarfare.addon.control.tileentity;

import valkyrienwarfare.addon.control.nodenetwork.BasicNodeTileEntity;
import valkyrienwarfare.addon.control.nodenetwork.Node;

public class ThrustRelayTileEntity extends BasicNodeTileEntity {

	public ThrustRelayTileEntity() {
		super();
		Node node = this.getNode();
		node.setIsNodeRelay(true);
	}

}
