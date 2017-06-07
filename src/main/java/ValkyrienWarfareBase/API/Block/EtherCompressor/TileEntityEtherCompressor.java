package ValkyrienWarfareBase.API.Block.EtherCompressor;

import ValkyrienWarfareControl.ThrustNetwork.BasicForceNodeTileEntity;

public abstract class TileEntityEtherCompressor extends BasicForceNodeTileEntity {

	public TileEntityEtherCompressor() {
		validate();
	}

	public TileEntityEtherCompressor(double power) {
		this.maxThrust = power;
		validate();
	}

}
