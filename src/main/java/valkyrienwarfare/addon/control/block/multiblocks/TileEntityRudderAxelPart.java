package valkyrienwarfare.addon.control.block.multiblocks;

import valkyrienwarfare.mod.coordinates.VectorImmutable;

public class TileEntityRudderAxelPart extends TileEntityMultiblockPartForce {

	@Override
	public VectorImmutable getForceOutputNormal() {
		// TODO Auto-generated method stub
		return new VectorImmutable(1, 0, 0);
	}

	@Override
	public double getThrustMagnitude() {
		return 0;
	}

}
