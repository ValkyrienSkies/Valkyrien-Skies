package ValkyrienWarfareControl.TileEntity;

import java.util.ArrayList;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.Math.RotationMatrices;
import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityHoverController extends TileEntity{

	public ArrayList<BlockPos> enginePositions = new ArrayList<BlockPos>();
	public double idealHeight = 10D;
	public double stabilityBias = .5D;

	public double linearVelocityBias = 3D;
	public double angularVelocityBias = 1D;
	
	public Vector normalVector = new Vector(0D,1D,0D);
	
	public double angularConstant = 100000000D;
	public double linearConstant = 1000000D;
	
	public TileEntityHoverController(){
		validate();
	}
	
	/*
	 * Returns the Force Vector the engine will send to the Physics Engine
	 */
	public Vector getForceForEngine(AntiGravEngineTileEntity engine,World world, BlockPos enginePos, IBlockState state, PhysicsObject physObj, double secondsToApply){
		physObj.physicsProcessor.convertTorqueToVelocity();
		
//		secondsToApply*=5D;
		
		
		angularVelocityBias = 3D;
		stabilityBias = .4D;
		
		engine.maxThrust = 10000D;
		
		double linearDist = -getControllerDistFromIdealY(physObj);
		double angularDist = -getEngineDistFromIdealAngular(enginePos,physObj,secondsToApply);
		
		engine.angularThrust.Y -= (angularConstant*secondsToApply)*angularDist;
		engine.linearThrust.Y -= (linearConstant*secondsToApply)*linearDist;
		
		engine.angularThrust.Y = Math.max(engine.angularThrust.Y, 0D);
		engine.linearThrust.Y = Math.max(engine.linearThrust.Y, 0D);
		
		engine.angularThrust.Y = Math.min(engine.angularThrust.Y, engine.maxThrust*stabilityBias);
		engine.linearThrust.Y = Math.min(engine.linearThrust.Y, engine.maxThrust*(1D-stabilityBias));
		
		Vector aggregateForce = engine.linearThrust.getAddition(engine.angularThrust);
		aggregateForce.multiply(secondsToApply);
		
//		System.out.println(aggregateForce);
		
		return aggregateForce;
	}
	
	public double getEngineDistFromIdealAngular(BlockPos enginePos,PhysicsObject physObj,double secondsToApply){
		Vector controllerPos = new Vector(pos.getX()+.5D,pos.getY()+.5D,pos.getZ()+.5D);
		Vector enginePosVec = new Vector(enginePos.getX()+.5D,enginePos.getY()+.5D,enginePos.getZ()+.5D);
		
		controllerPos.subtract(physObj.physicsProcessor.centerOfMass);
		enginePosVec.subtract(physObj.physicsProcessor.centerOfMass);
		
		Vector unOrientedPosDif = new Vector(enginePosVec.X-controllerPos.X,enginePosVec.Y-controllerPos.Y,enginePosVec.Z-controllerPos.Z);
		
		double idealYDif = unOrientedPosDif.dot(normalVector);
		
		RotationMatrices.doRotationOnly(physObj.coordTransform.lToWRotation, controllerPos);
		RotationMatrices.doRotationOnly(physObj.coordTransform.lToWRotation, enginePosVec);
		
		double inWorldYDif = enginePosVec.Y-controllerPos.Y;
		
		Vector angularVelocityAtPoint = physObj.physicsProcessor.angularVelocity.cross(enginePosVec);
		angularVelocityAtPoint.multiply(secondsToApply);
		
		return idealYDif-(inWorldYDif+angularVelocityAtPoint.Y*angularVelocityBias);
	}
	
	public double getControllerDistFromIdealY(PhysicsObject physObj){
		Vector controllerPos = new Vector(pos.getX()+.5D,pos.getY()+.5D,pos.getZ()+.5D);
		physObj.coordTransform.fromLocalToGlobal(controllerPos);
		return idealHeight-(controllerPos.Y+(physObj.physicsProcessor.linearMomentum.Y*physObj.physicsProcessor.invMass*linearVelocityBias));
	}

	public void readFromNBT(NBTTagCompound compound){
		NBTUtils.writeBlockPosArrayListToNBT("enginePositions", enginePositions, compound);
		NBTUtils.writeVectorToNBT("normalVector", normalVector, compound);
		idealHeight = compound.getDouble("idealHeight");
		super.readFromNBT(compound);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound){
    	enginePositions = NBTUtils.readBlockPosArrayListFromNBT("enginePositions", compound);
    	normalVector = NBTUtils.readVectorFromNBT("normalVector", compound);
    	compound.setDouble("idealHeight", idealHeight);
    	return super.writeToNBT(compound);
    }
	
}
