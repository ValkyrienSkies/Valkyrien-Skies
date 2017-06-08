package ValkyrienWarfareControl.NodeNetwork;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BasicForceNodeTileEntity extends BasicNodeTileEntity implements IForceTile{

	private Vector forceOutputVector = new Vector();

	protected double maxThrust = 5000D;
	protected double currentThrust = 0D;

	private boolean isForceOutputOriented = true;
	private Vector normalVelocityUnoriented;
	//Tells if the tile is in Ship Space, if it isn't then it doesn't try to find a parent Ship object
	private boolean hasAlreadyCheckedForParent = false;
	private PhysicsWrapperEntity parentShip;

	/**
	 * Only used for the NBT creation, other <init> calls should go through the other methods
	 */
	public BasicForceNodeTileEntity(){}

	public BasicForceNodeTileEntity(Vector normalVeclocityUnoriented, boolean isForceOutputOriented, double maxThrust){
		this.normalVelocityUnoriented = normalVeclocityUnoriented;
		this.isForceOutputOriented = isForceOutputOriented;
		this.maxThrust = maxThrust;
	}

	@Override
	public Vector getForceOutputNormal() {
		// TODO Auto-generated method stub
		return normalVelocityUnoriented;
	}

	@Override
	public Vector getForceOutputUnoriented(double secondsToApply) {
		return normalVelocityUnoriented.getProduct(currentThrust * secondsToApply);
	}

	@Override
	public Vector getForceOutputOriented(double secondsToApply) {
		Vector outputForce = getForceOutputUnoriented(secondsToApply);
		if(isForceOutputOriented) {
			if(updateParentShip()){
				RotationMatrices.applyTransform(parentShip.wrapping.coordTransform.lToWRotation, outputForce);
			}
		}
		return outputForce;
	}

	@Override
	public double getMaxThrust() {
		return maxThrust;
	}

	@Override
	public void setThrust(double newMagnitude) {
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


	@Override
	public void readFromNBT(NBTTagCompound compound) {
		isForceOutputOriented = compound.getBoolean("isForceOutputOriented");
		maxThrust = compound.getDouble("maxThrust");
		currentThrust = compound.getDouble("currentThrust");
		normalVelocityUnoriented = NBTUtils.readVectorFromNBT("normalVelocityUnoriented", compound);
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("isForceOutputOriented", isForceOutputOriented);
		compound.setDouble("maxThrust", maxThrust);
		compound.setDouble("currentThrust", currentThrust);
		NBTUtils.writeVectorToNBT("normalVelocityUnoriented", normalVelocityUnoriented, compound);
		return super.writeToNBT(compound);
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
