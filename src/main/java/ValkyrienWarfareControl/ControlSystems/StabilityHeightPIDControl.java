package ValkyrienWarfareControl.ControlSystems;

import java.util.HashSet;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.API.Block.EtherCompressor.TileEntityEtherCompressor;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareControl.NodeNetwork.Node;
import ValkyrienWarfareControl.TileEntity.ThrustModulatorTileEntity;
import ValkyrienWarfareControl.TileEntity.TileEntityNormalEtherCompressor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class StabilityHeightPIDControl {

	public final ThrustModulatorTileEntity parentTile;
	private double idealY;

	public StabilityHeightPIDControl(ThrustModulatorTileEntity parentTile) {
		this.parentTile = parentTile;
	}

	public void solveThrustValues(PhysicsCalculations calculations) {
		//TODO: Implement magic algorithm here :(
	}

	public double getMaxThrustForAllThrusters() {
		double totalThrustAvaliable = 0D;

		for(Node otherNode : getNetworkedNodesList()) {
			TileEntity nodeTile = otherNode.parentTile;
			if(nodeTile instanceof TileEntityNormalEtherCompressor) {
				TileEntityNormalEtherCompressor ether = (TileEntityNormalEtherCompressor) nodeTile;
				totalThrustAvaliable += ether.getMaxThrust();
			}
		}

		return totalThrustAvaliable;
	}

	private HashSet<Node> getNetworkedNodesList() {
		return parentTile.tileNode.getNodeNetwork().networkedNodes;
	}

}
