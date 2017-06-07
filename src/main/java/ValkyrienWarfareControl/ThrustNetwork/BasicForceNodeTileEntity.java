package ValkyrienWarfareControl.ThrustNetwork;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BasicForceNodeTileEntity extends BasicNodeTileEntity implements IForceTile{

	private Vector forceOutputVector = new Vector();

	protected double maxThrust = 5000D;
	protected double currentThrust = 0D;

	private Vector normalVelocityUnoriented;
	//Tells if the tile is in Ship Space, if it isn't then it doesn't try to find a parent Ship object
	private boolean hasAlreadyCheckedForParent = false;
	private PhysicsWrapperEntity parentShip;

	/**
	 * Only used for the NBT creation, other <init> calls should go through the other methods
	 */
	public BasicForceNodeTileEntity(){}

	public BasicForceNodeTileEntity(Vector normalVeclocityUnoriented){
		this.normalVelocityUnoriented = normalVeclocityUnoriented;
	}

	@Override
	public Vector getForceOutputNormal() {
		// TODO Auto-generated method stub
		return normalVelocityUnoriented;
	}

	@Override
	public Vector getForceOutput() {
		return forceOutputVector;
	}

	@Override
	public double getMaxThrust() {
		return maxThrust;
	}

	@Override
	public void setForceMagnitude(double newMagnitude) {
		currentThrust = newMagnitude;
	}

	@Override
	public Vector getPositionInLocalSpaceWithOrientation() {
		if(updateParentShip()){
			return null;
		}
		Vector engineCenter = new Vector(getPos().getX() + .5D, getPos().getY() + .5D, getPos().getZ() + .5D);
		RotationMatrices.applyTransform(parentShip.wrapping.coordTransform.lToWTransform, engineCenter);
		engineCenter.subtract(parentShip.posX, parentShip.posY, parentShip.posZ);
		return engineCenter;
	}

	@Override
	public Vector getVelocityAtEngineCenter() {
		if(updateParentShip()){
			return null;
		}
		PhysicsCalculations calculations = parentShip.wrapping.physicsProcessor;
		return calculations.getVelocityAtPoint(getPositionInLocalSpaceWithOrientation());
	}

	@Override
	public Vector getLinearVelocityAtEngineCenter() {
		if(updateParentShip()){
			return null;
		}
		PhysicsCalculations calculations = parentShip.wrapping.physicsProcessor;
		return calculations.linearMomentum;
	}

	@Override
	public Vector getAngularVelocityAtEngineCenter() {
		if(updateParentShip()){
			return null;
		}
		PhysicsCalculations calculations = parentShip.wrapping.physicsProcessor;
		return calculations.angularVelocity.cross(getPositionInLocalSpaceWithOrientation());
	}

	/**
	 * Returns true if a parent Ship exists, and false if otherwise
	 * @return
	 */
	private boolean updateParentShip() {
		if(hasAlreadyCheckedForParent){
			return parentShip != null;
		}
		BlockPos pos = this.getPos();
		World world = this.getWorld();
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldObj, pos);
		//Already checked
		hasAlreadyCheckedForParent = true;
		if(wrapper != null){
			parentShip = wrapper;
			return true;
		}else{
			return false;
		}
	}

}
