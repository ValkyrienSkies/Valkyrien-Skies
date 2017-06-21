package ValkyrienWarfareControl.NodeNetwork;

import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;

public interface IPhysicsProcessorNode {

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
