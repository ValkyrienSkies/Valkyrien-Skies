package ValkyrienWarfareBase.API.Block.EtherCompressor;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareControl.ThrustNetwork.BasicForceNodeTileEntity;

public abstract class TileEntityEtherCompressor extends BasicForceNodeTileEntity {

	public TileEntityEtherCompressor() {
		validate();
	}

	public TileEntityEtherCompressor(Vector normalForceVector, double power) {
		super(normalForceVector, false, power);
		validate();
	}

}
