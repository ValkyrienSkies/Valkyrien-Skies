package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareControl.NodeNetwork.BasicForceNodeTileEntity;

public class TileEntityPropellerEngine extends BasicForceNodeTileEntity {

	public TileEntityPropellerEngine(Vector normalVeclocityUnoriented, boolean isForceOutputOriented, double maxThrust) {
        super(normalVeclocityUnoriented, isForceOutputOriented, maxThrust);
    }

	public TileEntityPropellerEngine() {}
}
