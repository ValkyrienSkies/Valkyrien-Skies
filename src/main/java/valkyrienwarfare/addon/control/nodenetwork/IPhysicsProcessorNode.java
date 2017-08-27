package valkyrienwarfare.addon.control.nodenetwork;

import valkyrienwarfare.physics.PhysicsCalculations;
import valkyrienwarfare.physicsmanagement.PhysicsObject;

public interface IPhysicsProcessorNode extends INodeProvider {
	
	public int getPriority();
	
	public void setPriority(int newPriority);
	
	/**
	 * Does nothing by default, insert processor logic here
	 *
	 * @param object
	 * @param calculations
	 * @param secondsToSimulate
	 */
	public void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations, double secondsToSimulate);
	
}
