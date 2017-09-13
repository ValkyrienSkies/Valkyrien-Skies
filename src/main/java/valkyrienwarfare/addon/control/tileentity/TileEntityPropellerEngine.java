package valkyrienwarfare.addon.control.tileentity;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.addon.control.nodenetwork.BasicForceNodeTileEntity;

public class TileEntityPropellerEngine extends BasicForceNodeTileEntity {
	
	public TileEntityPropellerEngine(Vector normalVeclocityUnoriented, boolean isForceOutputOriented, double maxThrust) {
		super(normalVeclocityUnoriented, isForceOutputOriented, maxThrust);
	}
	
	public TileEntityPropellerEngine() {
	}
}
