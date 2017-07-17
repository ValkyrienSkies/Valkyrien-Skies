package ValkyrienWarfareWorld.TileEntity;

import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.Physics.PhysicsCalculationsManualControl;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareControl.NodeNetwork.IPhysicsProcessorNode;
import net.minecraft.tileentity.TileEntity;

public class TileEntitySkyTempleController extends TileEntity implements IPhysicsProcessorNode {

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void setPriority(int newPriority) {

	}

	@Override
	public void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations, double secondsToSimulate) {
		if(calculations instanceof PhysicsCalculationsManualControl) {
			PhysicsCalculationsManualControl manualControl = (PhysicsCalculationsManualControl) calculations;

			manualControl.yawRate = 1D;
		}
	}

}
