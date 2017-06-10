package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.API.Block.EtherCompressor.TileEntityEtherCompressor;

public class TileEntityNormalEtherCompressor extends TileEntityEtherCompressor {

	public TileEntityNormalEtherCompressor() {
		super();
	}

	public TileEntityNormalEtherCompressor(Vector normalForceVector, double power) {
		super(normalForceVector, power);
	}
}
