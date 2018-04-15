package valkyrienwarfare.addon.control.tileentity;

import valkyrienwarfare.addon.control.nodenetwork.Node;
import valkyrienwarfare.api.block.ethercompressor.TileEntityEtherCompressor;
import valkyrienwarfare.physics.calculations.PhysicsCalculations;
import valkyrienwarfare.physics.management.PhysicsObject;

public class TileEntityLiftValve extends ImplPhysicsProcessorNodeTileEntity {

    public static final int PHYSICS_PROCESSOR_PRIORITY = 10;
    
    public TileEntityLiftValve() {
        super(PHYSICS_PROCESSOR_PRIORITY);
    }

    @Override
    public void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations, double secondsToSimulate) {
        for (Node node : getNode().getConnectedNodes()) {
            if (node.getParentTile() instanceof TileEntityEtherCompressor) {
                TileEntityEtherCompressor compressor = (TileEntityEtherCompressor) node.getParentTile();
                compressor.addEtherGas(10);
            }
        }
        
    }

}
