package valkyrienwarfare.addon.control.tileentity;

import valkyrienwarfare.api.block.ethercompressor.TileEntityEtherCompressor;
import valkyrienwarfare.api.Vector;

public class TileEntityNormalEtherCompressor extends TileEntityEtherCompressor {

	public TileEntityNormalEtherCompressor() {
		super();
	}

	public TileEntityNormalEtherCompressor(Vector normalForceVector, double power) {
		super(normalForceVector, power);
	}

}
