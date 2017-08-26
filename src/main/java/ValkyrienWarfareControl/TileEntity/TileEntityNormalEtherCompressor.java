package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.API.Block.EtherCompressor.TileEntityEtherCompressor;
import ValkyrienWarfareBase.API.Vector;

public class TileEntityNormalEtherCompressor extends TileEntityEtherCompressor {

	public TileEntityNormalEtherCompressor() {
		super();
	}

	public TileEntityNormalEtherCompressor(Vector normalForceVector, double power) {
		super(normalForceVector, power);
	}

}
