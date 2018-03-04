package valkyrienwarfare.addon.control.tileentity;

import valkyrienwarfare.physics.calculations.PhysicsCalculations;
import valkyrienwarfare.physics.management.PhysicsObject;

public class TileEntityLiftValve extends ImplPhysicsProcessorNodeTileEntity {

    public static final int PHYSICS_PROCESSOR_PRIORITY = 10;
    
    public TileEntityLiftValve() {
        super(PHYSICS_PROCESSOR_PRIORITY);
    }

    @Override
    public void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations, double secondsToSimulate) {
        // TODO Auto-generated method stub
        
    }

}
