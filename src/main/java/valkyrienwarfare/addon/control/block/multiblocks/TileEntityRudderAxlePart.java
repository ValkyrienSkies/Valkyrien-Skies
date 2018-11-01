package valkyrienwarfare.addon.control.block.multiblocks;

import valkyrienwarfare.mod.coordinates.VectorImmutable;

public class TileEntityRudderAxlePart extends TileEntityMultiblockPartForce {

	public TileEntityRudderAxlePart() {
		super();
	}
	
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
