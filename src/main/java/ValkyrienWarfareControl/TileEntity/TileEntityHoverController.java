package ValkyrienWarfareControl.TileEntity;

import java.util.ArrayList;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareControl.Network.HovercraftControllerGUIInputMessage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TileEntityHoverController extends TileEntity {

	public ArrayList<BlockPos> enginePositions = new ArrayList<BlockPos>();
	public double idealHeight = 16D;
	public double stabilityBias = .45D;

	public double linearVelocityBias = 1D;
	public double angularVelocityBias = 50D;

	public Vector normalVector = new Vector(0D, 1D, 0D);

	public double angularConstant = 500000000D;
	public double linearConstant = 1000000D;

	public boolean autoStabalizerControl = true;

	public TileEntityHoverController() {
		// validate();
	}

	/*
	 * Returns the Force Vector the engine will send to the Physics Engine
	 */
	public Vector getForceForEngine(AntiGravEngineTileEntity engine, World world, BlockPos enginePos, IBlockState state, PhysicsObject physObj, double secondsToApply) {
		// physObj.physicsProcessor.convertTorqueToVelocity();
		// secondsToApply*=5D;
		// idealHeight = 100D;
		if (worldObj.isBlockPowered(getPos())||autoStabalizerControl) {
			setAutoStabilizationValue(physObj);
		}

		double linearDist = -getControllerDistFromIdealY(physObj);
		double angularDist = -getEngineDistFromIdealAngular(enginePos, physObj, secondsToApply);

		engine.angularThrust.Y -= (angularConstant * secondsToApply) * angularDist;
		engine.linearThrust.Y -= (linearConstant * secondsToApply) * linearDist;

		engine.angularThrust.Y = Math.max(engine.angularThrust.Y, 0D);
		engine.linearThrust.Y = Math.max(engine.linearThrust.Y, 0D);

		engine.angularThrust.Y = Math.min(engine.angularThrust.Y, engine.maxThrust * stabilityBias);
		engine.linearThrust.Y = Math.min(engine.linearThrust.Y, engine.maxThrust * (1D - stabilityBias));

		Vector aggregateForce = engine.linearThrust.getAddition(engine.angularThrust);
		aggregateForce.multiply(secondsToApply);

		// System.out.println(aggregateForce);

		return aggregateForce;
	}

	public double getEngineDistFromIdealAngular(BlockPos enginePos, PhysicsObject physObj, double secondsToApply) {
		Vector controllerPos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
		Vector enginePosVec = new Vector(enginePos.getX() + .5D, enginePos.getY() + .5D, enginePos.getZ() + .5D);

		controllerPos.subtract(physObj.physicsProcessor.centerOfMass);
		enginePosVec.subtract(physObj.physicsProcessor.centerOfMass);

		Vector unOrientedPosDif = new Vector(enginePosVec.X - controllerPos.X, enginePosVec.Y - controllerPos.Y, enginePosVec.Z - controllerPos.Z);

		double idealYDif = unOrientedPosDif.dot(normalVector);

		RotationMatrices.doRotationOnly(physObj.coordTransform.lToWRotation, controllerPos);
		RotationMatrices.doRotationOnly(physObj.coordTransform.lToWRotation, enginePosVec);

		double inWorldYDif = enginePosVec.Y - controllerPos.Y;

		Vector angularVelocityAtPoint = physObj.physicsProcessor.angularVelocity.cross(enginePosVec);
		angularVelocityAtPoint.multiply(secondsToApply);

		return idealYDif - (inWorldYDif + angularVelocityAtPoint.Y * angularVelocityBias);
	}

	public double getControllerDistFromIdealY(PhysicsObject physObj) {
		Vector controllerPos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
		physObj.coordTransform.fromLocalToGlobal(controllerPos);
		return idealHeight - (physObj.physicsProcessor.wrapperEnt.posY + (physObj.physicsProcessor.linearMomentum.Y * physObj.physicsProcessor.invMass * linearVelocityBias));
	}

	public void handleGUIInput(HovercraftControllerGUIInputMessage message, MessageContext ctx) {
		idealHeight = message.newIdealHeight;

		if (message.newStablitiyBias < 0 || message.newStablitiyBias > 1D) {
			// Out of bounds, set to auto
			autoStabalizerControl = true;
		} else {
			double stabilityDif = Math.abs(stabilityBias - message.newStablitiyBias);
			// if(stabilityDif>.05D){
			stabilityBias = message.newStablitiyBias;
			autoStabalizerControl = false;
			// }
		}

		linearVelocityBias = message.newLinearVelocityBias;
		markDirty();
	}

	private void setAutoStabilizationValue(PhysicsObject physObj) {
		Vector controllerPos = new Vector(pos.getX()+.5D,pos.getY()+.5D,pos.getZ()+.5D);
		physObj.coordTransform.fromLocalToGlobal(controllerPos);
		
		double controllerDistToIdeal = -(idealHeight-physObj.physicsProcessor.wrapperEnt.posY);
		double yVelocity = physObj.physicsProcessor.linearMomentum.Y*physObj.physicsProcessor.invMass*linearVelocityBias;
		
		double biasChange = .00005D;
		
		if(Math.abs(controllerDistToIdeal)>.5D){
		
			if((yVelocity > 0 && controllerDistToIdeal > 0)||(yVelocity < 0 && controllerDistToIdeal < 0)){
				stabilityBias -= biasChange;
				
			}else{
//				stabilityBias += biasChange;
			}
//			stabilityBias -= biasChange;
		}else{
			stabilityBias += biasChange;
		}
		
		
		// double epsilon = 5D;
		// double biasChange = .00005D;
		//
		// Vector controllerPos = new Vector(pos.getX()+.5D,pos.getY()+.5D,pos.getZ()+.5D);
		// physObj.coordTransform.fromLocalToGlobal(controllerPos);
		//
		// double controllerDist = idealHeight-controllerPos.Y;
		//
		// double yVelocity = physObj.physicsProcessor.linearMomentum.Y*physObj.physicsProcessor.invMass*linearVelocityBias;
		//
		// if(Math.abs(controllerDist)>epsilon){
		// if(Math.signum(controllerDist)!=Math.signum(yVelocity)){
		// stabilityBias+=(biasChange*controllerDist);
		// }else{
		// stabilityBias-=(biasChange*controllerDist);
		// }
		// }
		// //Limit bias to between 0 and 1
		 stabilityBias = Math.max(Math.min(stabilityBias, 1D), 0D);
		
		
		
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, 0, writeToNBT(new NBTTagCompound()));
		return packet;
	}

	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		enginePositions = NBTUtils.readBlockPosArrayListFromNBT("enginePositions", compound);
		normalVector = NBTUtils.readVectorFromNBT("normalVector", compound);
		if (normalVector.isZero()) {
			normalVector = new Vector(0, 1, 0);
		}
		idealHeight = compound.getDouble("idealHeight");
		stabilityBias = compound.getDouble("stabilityBias");
		linearVelocityBias = compound.getDouble("linearVelocityBias");
		angularVelocityBias = compound.getDouble("angularVelocityBias");
		autoStabalizerControl = compound.getBoolean("autoStabalizerControl");
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTUtils.writeBlockPosArrayListToNBT("enginePositions", enginePositions, compound);
		NBTUtils.writeVectorToNBT("normalVector", normalVector, compound);
		compound.setDouble("idealHeight", idealHeight);
		compound.setDouble("stabilityBias", stabilityBias);
		compound.setDouble("linearVelocityBias", linearVelocityBias);
		compound.setDouble("angularVelocityBias", angularVelocityBias);
		compound.setBoolean("autoStabalizerControl", autoStabalizerControl);
		return super.writeToNBT(compound);
	}

}
