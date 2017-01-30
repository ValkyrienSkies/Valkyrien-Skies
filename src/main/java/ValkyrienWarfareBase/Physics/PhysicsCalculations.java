package ValkyrienWarfareBase.Physics;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Matrix3d;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.PhysicsSettings;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.IBlockForceProvider;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Math.BigBastardMath;
import ValkyrienWarfareBase.Math.Quaternion;
import ValkyrienWarfareBase.PhysCollision.ShipPhysicsCollider;
import ValkyrienWarfareBase.PhysCollision.WorldPhysicsCollider;
import ValkyrienWarfareBase.PhysicsManagement.CoordTransformObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Balloon.BalloonProcessor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PhysicsCalculations {

	public PhysicsObject parent;
	public PhysicsWrapperEntity wrapperEnt;
	public World worldObj;
	public WorldPhysicsCollider worldCollision;
	public ShipPhysicsCollider shipCollision;

	public Vector centerOfMass;
	public Vector linearMomentum;
	public Vector angularVelocity;
	public Vector torque;

	public double mass, invMass;
	public Vector gravity = new Vector(0, -9.8D, 0);
	// The time occurring on each PhysTick
	public double physRawSpeed;
	// Number of iterations the solver runs on each game tick
	public int iterations = 5;
	// The amount of time to be simulated on each rawPhysTick *(Its physSpeed/iterations)
	public double physTickSpeed = physRawSpeed / iterations;
	// Used to limit the accumulation of motion by an object (Basically Air-Resistance preventing infinite energy)
	public double drag = .99D;
	private double blocksToMetersConversion = 1.8D;

	public ArrayList<BlockPos> activeForcePositions = new ArrayList<BlockPos>();

	public double[] MoITensor, invMoITensor;
	public double[] framedMOI, invFramedMOI;
	
	public boolean actAsArchimedes = false;

	public PhysicsCalculations(PhysicsObject toProcess) {
		parent = toProcess;
		wrapperEnt = parent.wrapper;
		worldObj = toProcess.worldObj;
		worldCollision = new WorldPhysicsCollider(this);
		shipCollision = new ShipPhysicsCollider(this);

		MoITensor = RotationMatrices.getZeroMatrix(3);
		invMoITensor = RotationMatrices.getZeroMatrix(3);
		framedMOI = RotationMatrices.getZeroMatrix(3);
		invFramedMOI = RotationMatrices.getZeroMatrix(3);

		centerOfMass = new Vector(toProcess.centerCoord);
		linearMomentum = new Vector();
		angularVelocity = new Vector();
		torque = new Vector();
	}

	public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos pos) {
		if (oldState.getBlock() == Blocks.AIR) {
			if (BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)) {
				activeForcePositions.add(pos);
			}
		} else {
			// int index = activeForcePositions.indexOf(pos);
			// if(BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)){
			// if(index==-1){
			// activeForcePositions.add(pos);
			// }
			// }else{
			// if(index!=-1){
			// activeForcePositions.remove(index);
			// }
			// }
			if (activeForcePositions.contains(pos)) {
				if (!BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)) {
					activeForcePositions.remove(pos);
				}
			} else {
				if (BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)) {
					activeForcePositions.add(pos);
				}
			}
		}
		if (newState.getBlock() == Blocks.AIR) {
			activeForcePositions.remove(pos);
		}

		double oldMassAtPos = BlockMass.basicMass.getMassFromState(oldState, pos, worldObj);
		double newMassAtPos = BlockMass.basicMass.getMassFromState(newState, pos, worldObj);
		// Don't change anything if the mass is the same
		if (oldMassAtPos != newMassAtPos) {
			final double notAHalf = .5D;
			final double x = pos.getX() + .5D;
			final double y = pos.getY() + .5D;
			final double z = pos.getZ() + .5D;

			if (oldMassAtPos > 0D) {
				oldMassAtPos /= -9.0D;
				addMassAt(x, y, z, oldMassAtPos);
				addMassAt(x + notAHalf, y + notAHalf, z + notAHalf, oldMassAtPos);
				addMassAt(x + notAHalf, y + notAHalf, z - notAHalf, oldMassAtPos);
				addMassAt(x + notAHalf, y - notAHalf, z + notAHalf, oldMassAtPos);
				addMassAt(x + notAHalf, y - notAHalf, z - notAHalf, oldMassAtPos);
				addMassAt(x - notAHalf, y + notAHalf, z + notAHalf, oldMassAtPos);
				addMassAt(x - notAHalf, y + notAHalf, z - notAHalf, oldMassAtPos);
				addMassAt(x - notAHalf, y - notAHalf, z + notAHalf, oldMassAtPos);
				addMassAt(x - notAHalf, y - notAHalf, z - notAHalf, oldMassAtPos);
			}
			if (newMassAtPos > 0D) {
				newMassAtPos /= 9.0D;
				addMassAt(x, y, z, newMassAtPos);
				addMassAt(x + notAHalf, y + notAHalf, z + notAHalf, newMassAtPos);
				addMassAt(x + notAHalf, y + notAHalf, z - notAHalf, newMassAtPos);
				addMassAt(x + notAHalf, y - notAHalf, z + notAHalf, newMassAtPos);
				addMassAt(x + notAHalf, y - notAHalf, z - notAHalf, newMassAtPos);
				addMassAt(x - notAHalf, y + notAHalf, z + notAHalf, newMassAtPos);
				addMassAt(x - notAHalf, y + notAHalf, z - notAHalf, newMassAtPos);
				addMassAt(x - notAHalf, y - notAHalf, z + notAHalf, newMassAtPos);
				addMassAt(x - notAHalf, y - notAHalf, z - notAHalf, newMassAtPos);
			}
		}
	}

	private void addMassAt(double x, double y, double z, double addedMass) {
		Vector prevCenterOfMass = new Vector(centerOfMass);
		if (mass > .0001D) {
			centerOfMass.multiply(mass);
			centerOfMass.add(new Vector(x, y, z).getProduct(addedMass));
			centerOfMass.multiply(1.0D / (mass + addedMass));
		} else {
			centerOfMass = new Vector(x, y, z);
			MoITensor = RotationMatrices.getZeroMatrix(3);
		}
		double cmShiftX = prevCenterOfMass.X - centerOfMass.X;
		double cmShiftY = prevCenterOfMass.Y - centerOfMass.Y;
		double cmShiftZ = prevCenterOfMass.Z - centerOfMass.Z;
		double rx = x - centerOfMass.X;
		double ry = y - centerOfMass.Y;
		double rz = z - centerOfMass.Z;

		MoITensor[0] = MoITensor[0] + (cmShiftY * cmShiftY + cmShiftZ * cmShiftZ) * mass + (ry * ry + rz * rz) * addedMass;
		MoITensor[1] = MoITensor[1] - cmShiftX * cmShiftY * mass - rx * ry * addedMass;
		MoITensor[2] = MoITensor[2] - cmShiftX * cmShiftZ * mass - rx * rz * addedMass;
		MoITensor[3] = MoITensor[1];
		MoITensor[4] = MoITensor[4] + (cmShiftX * cmShiftX + cmShiftZ * cmShiftZ) * mass + (rx * rx + rz * rz) * addedMass;
		MoITensor[5] = MoITensor[5] - cmShiftY * cmShiftZ * mass - ry * rz * addedMass;
		MoITensor[6] = MoITensor[2];
		MoITensor[7] = MoITensor[5];
		MoITensor[8] = MoITensor[8] + (cmShiftX * cmShiftX + cmShiftY * cmShiftY) * mass + (rx * rx + ry * ry) * addedMass;

		mass += addedMass;
		invMass = 1.0D / mass;
		invMoITensor = RotationMatrices.inverse3by3(MoITensor);
		// angularVelocity = RotationMatrices.get3by3TransformedVec(oldMOI, torque);
		// angularVelocity = RotationMatrices.get3by3TransformedVec(invMoITensor, torque);
		// System.out.println(MoITensor[0]+":"+MoITensor[1]+":"+MoITensor[2]);
		// System.out.println(MoITensor[3]+":"+MoITensor[4]+":"+MoITensor[5]);
		// System.out.println(MoITensor[6]+":"+MoITensor[7]+":"+MoITensor[8]);
	}

	public void rawPhysTickPreCol(double newPhysSpeed, int iters) {
		if (parent.doPhysics) {
			updatePhysSpeedAndIters(newPhysSpeed, iters);
			updateCenterOfMass();
			calculateFramedMOITensor();
			if(!actAsArchimedes){
				calculateForces();
			}else{
				calculateForcesArchimedes();
			}
		}
	}

	public void processWorldCollision() {
		if (parent.doPhysics) {
			worldCollision.runPhysCollision();
		}
	}

	public void rawPhysTickPostCol() {
		if (parent.doPhysics) {
			if (PhysicsSettings.doAirshipRotation)
				applyAngularVelocity();

			if (PhysicsSettings.doAirshipMovement)
				applyLinearVelocity();
		}
	}

	// The x/y/z variables need to be updated when the centerOfMass location changes
	public void updateCenterOfMass() {
		Vector parentCM = parent.centerCoord;
		if (!parent.centerCoord.equals(centerOfMass)) {
			Vector CMDif = centerOfMass.getSubtraction(parentCM);
			RotationMatrices.applyTransform(parent.coordTransform.lToWRotation, CMDif);

			parent.wrapper.posX -= CMDif.X;
			parent.wrapper.posY -= CMDif.Y;
			parent.wrapper.posZ -= CMDif.Z;

			parent.centerCoord = new Vector(centerOfMass);
			parent.coordTransform.updateAllTransforms();
		}
	}

	// Applies the rotation transform onto the Moment of Inertia to generate the REAL MOI at that given instant
	public void calculateFramedMOITensor() {
		framedMOI = new double[9];
		Matrix3d pitch = new Matrix3d();
		Matrix3d yaw = new Matrix3d();
		Matrix3d roll = new Matrix3d();
		pitch.rotX(Math.toRadians(parent.wrapper.pitch));
		yaw.rotY(Math.toRadians(parent.wrapper.yaw));
		roll.rotZ(Math.toRadians(parent.wrapper.roll));
		pitch.mul(yaw);
		pitch.mul(roll);
		pitch.normalize();
		Matrix3d inertiaBodyFrame = new Matrix3d(MoITensor);
		Matrix3d multipled = new Matrix3d();
		multipled.mul(pitch, inertiaBodyFrame);
		pitch.transpose();
		multipled.mul(pitch);
		framedMOI[0] = multipled.m00;
		framedMOI[1] = multipled.m01;
		framedMOI[2] = multipled.m02;
		framedMOI[3] = multipled.m10;
		framedMOI[4] = multipled.m11;
		framedMOI[5] = multipled.m12;
		framedMOI[6] = multipled.m20;
		framedMOI[7] = multipled.m21;
		framedMOI[8] = multipled.m22;
		invFramedMOI = RotationMatrices.inverse3by3(framedMOI);
	}

	public void calculateForces() {
		double modifiedDrag = Math.pow(drag, physTickSpeed / .05D);
		linearMomentum.multiply(modifiedDrag);
		angularVelocity.multiply(modifiedDrag);
		if (PhysicsSettings.doGravity) {
			addForceAtPoint(new Vector(0, 0, 0), ValkyrienWarfareMod.gravity.getProduct(mass * physTickSpeed));
		}
		addQueuedForces();
		Collections.shuffle(activeForcePositions);

		Vector blockForce = new Vector();
		Vector inBodyWO = new Vector();
		Vector crossVector = new Vector();

		if (PhysicsSettings.doPhysicsBlocks) {
			for (BlockPos pos : activeForcePositions) {
				IBlockState state = parent.VKChunkCache.getBlockState(pos);
				Block blockAt = state.getBlock();
				BigBastardMath.getBodyPosWithOrientation(pos, centerOfMass, parent.coordTransform.lToWRotation, inBodyWO);

				BlockForce.basicForces.getForceFromState(state, pos, worldObj, physTickSpeed, parent, blockForce);

				if (blockForce != null) {
					if (blockAt instanceof IBlockForceProvider) {
						Vector otherPosition = ((IBlockForceProvider) blockAt).getBlockForcePosition(worldObj, pos, state, parent.wrapper, physTickSpeed);
						if (otherPosition != null) {
							BigBastardMath.getBodyPosWithOrientation(otherPosition, centerOfMass, parent.coordTransform.lToWRotation, inBodyWO);
						}
					}
					addForceAtPoint(inBodyWO, blockForce, crossVector);
				} else {
				}
			}
		}

		if (PhysicsSettings.doBalloons) {
			for (BalloonProcessor balloon : parent.balloonManager.balloonProcessors) {
				balloon.tickBalloonTemperatures(physTickSpeed, this);

				Vector balloonForce = balloon.getBalloonForce(physTickSpeed, this);
				Vector balloonCenterInBody = balloon.getForceCenter();

				BigBastardMath.getBodyPosWithOrientation(balloonCenterInBody, centerOfMass, parent.coordTransform.lToWRotation, inBodyWO);

				addForceAtPoint(inBodyWO, balloonForce, crossVector);
			}
		}

		convertTorqueToVelocity();
	}

	public void calculateForcesArchimedes() {
		double modifiedDrag = Math.pow(drag, physTickSpeed / .05D);
		linearMomentum.multiply(modifiedDrag);
		angularVelocity.multiply(modifiedDrag);
	}

	public void addQueuedForces() {
		Collections.shuffle(parent.queuedPhysForces);
		for (PhysicsQueuedForce queuedForce : parent.queuedPhysForces) {
			Vector forceVec = new Vector(queuedForce.force);
			if (queuedForce.isLocal) {
				RotationMatrices.doRotationOnly(parent.coordTransform.lToWRotation, forceVec);
			}
			forceVec.multiply(physTickSpeed);

			Vector posVec = new Vector(queuedForce.inBodyPos);
			// RotationMatrices.applyTransform(parent.coordTransform.lToWTransform, posVec);
			posVec.X -= wrapperEnt.posX;
			posVec.Y -= wrapperEnt.posY;
			posVec.Z -= wrapperEnt.posZ;

			addForceAtPoint(posVec, forceVec);
			// System.out.println(posVec);
			// torque.add(posVec.cross(forceVec));
			// linearMomentum.add(forceVec);
		}
	}

	public void convertTorqueToVelocity() {
		if (!torque.isZero()) {
			angularVelocity.add(RotationMatrices.get3by3TransformedVec(invFramedMOI, torque));
			torque.zero();
		}
	}

	public void addForceAtPoint(Vector inBodyWO, Vector forceToApply) {
		forceToApply.multiply(blocksToMetersConversion);
		torque.add(inBodyWO.cross(forceToApply));
		linearMomentum.add(forceToApply);
	}

	public void addForceAtPoint(Vector inBodyWO, Vector forceToApply, Vector crossVector) {
		forceToApply.multiply(blocksToMetersConversion);
		crossVector.setCross(inBodyWO, forceToApply);
		torque.add(crossVector);
		linearMomentum.add(forceToApply);
	}

	public void updatePhysSpeedAndIters(double newPhysSpeed, int iters) {
		physRawSpeed = newPhysSpeed;
		iterations = iters;
		physTickSpeed = physRawSpeed / iterations;
	}

	public void applyAngularVelocity() {
		CoordTransformObject coordTrans = parent.coordTransform;

		double[] rotationChange = RotationMatrices.getRotationMatrix(angularVelocity.X, angularVelocity.Y, angularVelocity.Z, angularVelocity.length() * physTickSpeed);
		Quaternion faggot = Quaternion.QuaternionFromMatrix(RotationMatrices.getMatrixProduct(rotationChange, coordTrans.lToWRotation));
		double[] radians = faggot.toRadians();
		if (!(Double.isNaN(radians[0]) || Double.isNaN(radians[1]) || Double.isNaN(radians[2]))) {
			wrapperEnt.pitch = (float) Math.toDegrees(radians[0]);
			wrapperEnt.yaw = (float) Math.toDegrees(radians[1]);
			wrapperEnt.roll = (float) Math.toDegrees(radians[2]);
			coordTrans.updateAllTransforms();
		} else {
			 wrapperEnt.isDead=true;
			wrapperEnt.wrapping.doPhysics = false;
//			linearMomentum = new Vector();
//			angularVelocity = new Vector();
			System.out.println(angularVelocity);
			System.out.println("Rotational Error?");
		}
	}

	public void applyLinearVelocity() {
		if (mass > 0) {
			double momentMod = physTickSpeed * invMass;
			wrapperEnt.posX += (linearMomentum.X * momentMod);
			wrapperEnt.posY += (linearMomentum.Y * momentMod);
			wrapperEnt.posZ += (linearMomentum.Z * momentMod);
		}
		if (wrapperEnt.posY > ValkyrienWarfareMod.shipUpperLimit) {
			wrapperEnt.posY = ValkyrienWarfareMod.shipUpperLimit;
		}
		if (wrapperEnt.posY < ValkyrienWarfareMod.shipLowerLimit) {
			wrapperEnt.posY = ValkyrienWarfareMod.shipLowerLimit;
		}
	}

	public Vector getMomentumAtPoint(Vector inBodyWO) {
		Vector speed = angularVelocity.cross(inBodyWO);
		speed.X += (linearMomentum.X * invMass);
		speed.Y += (linearMomentum.Y * invMass);
		speed.Z += (linearMomentum.Z * invMass);
		return speed;
	}

	public void writeToNBTTag(NBTTagCompound compound) {
		compound.setDouble("mass", mass);

		NBTUtils.writeVectorToNBT("linear", linearMomentum, compound);
		NBTUtils.writeVectorToNBT("angularVelocity", angularVelocity, compound);
		NBTUtils.writeVectorToNBT("CM", centerOfMass, compound);

		NBTUtils.write3x3MatrixToNBT("MOI", MoITensor, compound);
	}

	public void readFromNBTTag(NBTTagCompound compound) {
		mass = compound.getDouble("mass");

		linearMomentum = NBTUtils.readVectorFromNBT("linear", compound);
		angularVelocity = NBTUtils.readVectorFromNBT("angularVelocity", compound);
		centerOfMass = NBTUtils.readVectorFromNBT("CM", compound);

		MoITensor = NBTUtils.read3x3MatrixFromNBT("MOI", compound);

		processNBTRead();
	}

	// Calculates the inverses and the framed MOIs
	public void processNBTRead() {
		invMoITensor = RotationMatrices.inverse3by3(MoITensor);
		invMass = 1D / mass;
	}

	// Called upon a Ship being created from the World, and generates the physics data for it
	public void processInitialPhysicsData() {
		IBlockState Air = Blocks.AIR.getDefaultState();
		for (BlockPos pos : parent.blockPositions) {
			onSetBlockState(Air, parent.VKChunkCache.getBlockState(pos), pos);
		}
	}

}
