package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.fuel.IEtherEngine;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.coordinates.VectorImmutable;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class TileEntityEthereumCompressorPart extends TileEntityMultiblockPartForce implements IEtherEngine {

	private static final VectorImmutable FORCE_NORMAL = new VectorImmutable(0, 1, 0);

	public TileEntityEthereumCompressorPart() {
		super();
	}
	
	public TileEntityEthereumCompressorPart(double maxThrust) {
		this();
		this.setMaxThrust(maxThrust);
	}
	
	@Override
	public VectorImmutable getForceOutputNormal() {
		return FORCE_NORMAL;
	}

	@Override
	public void setThrustMultiplierGoal(double thrustMultiplierGoal) {
		// TODO: Something is fundamentally wrong here.
		if (this.isMaster() || this.getMaster() == this) {
			super.setThrustMultiplierGoal(thrustMultiplierGoal);
		} else {
			TileEntityEthereumCompressorPart.class.cast(this.getMaster()).setThrustMultiplierGoal(thrustMultiplierGoal);
		}
	}
	
	@Override
	public double getThrustMagnitude() {
		if (this.isPartOfAssembledMultiblock() && this.getMaster() instanceof TileEntityEthereumCompressorPart) {
			return this.getMaxThrust() * TileEntityEthereumCompressorPart.class.cast(this.getMaster()).getThrustMultiplierGoal() * this.getCurrentEtherEfficiency();
		} else {
			return 0;
		}
	}

	@Override
	public double getCurrentEtherEfficiency() {
		PhysicsWrapperEntity tilePhysics = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(getWorld(), getPos());
		if (tilePhysics != null) {
			Vector tilePos = new Vector(getPos().getX() + .5D, getPos().getY() + .5D, getPos().getZ() + .5D);
			tilePhysics.getPhysicsObject().getShipTransformationManager().getCurrentPhysicsTransform().transform(tilePos, TransformType.SUBSPACE_TO_GLOBAL);
			double yPos = tilePos.Y;
			if (yPos <= -30) {
				return 1;
			} else {
				double efficiency = 1D - ((yPos + 30) / 330);
				efficiency = Math.max(0, Math.min(1, efficiency));
				return efficiency;
			}
		} else {
			return 1;
		}
	}

}
