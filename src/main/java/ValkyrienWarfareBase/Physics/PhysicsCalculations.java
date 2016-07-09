package ValkyrienWarfareBase.Physics;

import javax.vecmath.Matrix3d;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.Math.RotationMatrices;
import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PhysicsCalculations {

	public PhysicsObject parent;
	public World worldObj;
	
	public Vector centerOfMass;
	public Vector linearVelocity;
	public Vector angularVelocity;
	
	public double mass,invMass;
	public double gravity = -9.8D;
	//The time occurring on each PhysTick
	public double physSpeed;
	//Number of iterations the solver runs on each game tick
	public int iterations = 5;
	//The amount of time to be simulated on each rawPhysTick *(Its physSpeed/iterations)
	public double rawPhysSpeed = physSpeed/iterations;
	//Used to limit the accumulation of motion by an object (Basically Air-Resistance preventing infinite energy)
	public double drag = .98D;
	
	public double[] MoITensor,invMoITensor;
	public double[] framedMOI,invFramedMOI;
	
	public PhysicsCalculations(PhysicsObject toProcess){
		parent = toProcess;
		worldObj = toProcess.worldObj;
		
		MoITensor = RotationMatrices.getZeroMatrix(3);
		invMoITensor = RotationMatrices.getZeroMatrix(3);
		framedMOI = RotationMatrices.getZeroMatrix(3);
		invFramedMOI = RotationMatrices.getZeroMatrix(3);
		
		centerOfMass = new Vector(toProcess.centerCoord);
		linearVelocity = new Vector();
		angularVelocity = new Vector();
	}
	
	public void onSetBlockState(IBlockState oldState,IBlockState newState,BlockPos pos){
		double oldMassAtPos = BlockMass.basicMass.getMassFromState(oldState, pos, worldObj);
		double newMassAtPos = BlockMass.basicMass.getMassFromState(newState, pos, worldObj);
		//Don't change anything if the mass is the same
		if(oldMassAtPos!=newMassAtPos){
			final double notAHalf = .5D;
			final double x = pos.getX()+.5D;
			final double y = pos.getY()+.5D;
			final double z = pos.getZ()+.5D;
			
			if(oldMassAtPos>0D){
				oldMassAtPos /= -9.0D;
				addMassAt(x,y,z,oldMassAtPos);
				addMassAt(x+notAHalf,y+notAHalf,z+notAHalf,oldMassAtPos);
				addMassAt(x+notAHalf,y+notAHalf,z-notAHalf,oldMassAtPos);
				addMassAt(x+notAHalf,y-notAHalf,z+notAHalf,oldMassAtPos);
				addMassAt(x+notAHalf,y-notAHalf,z-notAHalf,oldMassAtPos);
				addMassAt(x-notAHalf,y+notAHalf,z+notAHalf,oldMassAtPos);
				addMassAt(x-notAHalf,y+notAHalf,z-notAHalf,oldMassAtPos);
				addMassAt(x-notAHalf,y-notAHalf,z+notAHalf,oldMassAtPos);
				addMassAt(x-notAHalf,y-notAHalf,z-notAHalf,oldMassAtPos);
			}
			if(newMassAtPos>0D){
				newMassAtPos /= 9.0D;
				addMassAt(x,y,z,newMassAtPos);
				addMassAt(x+notAHalf,y+notAHalf,z+notAHalf,newMassAtPos);
				addMassAt(x+notAHalf,y+notAHalf,z-notAHalf,newMassAtPos);
				addMassAt(x+notAHalf,y-notAHalf,z+notAHalf,newMassAtPos);
				addMassAt(x+notAHalf,y-notAHalf,z-notAHalf,newMassAtPos);
				addMassAt(x-notAHalf,y+notAHalf,z+notAHalf,newMassAtPos);
				addMassAt(x-notAHalf,y+notAHalf,z-notAHalf,newMassAtPos);
				addMassAt(x-notAHalf,y-notAHalf,z+notAHalf,newMassAtPos);
				addMassAt(x-notAHalf,y-notAHalf,z-notAHalf,newMassAtPos);
			}
		}
	}
	
	private void addMassAt(double x,double y,double z,double addedMass){
		Vector prevCenterOfMass = new Vector(centerOfMass);
		if(mass>.0001D){
			centerOfMass.multiply(mass);
			centerOfMass.add(new Vector(x,y,z).getProduct(addedMass));
			centerOfMass.multiply(1.0D/(mass+addedMass));
		}else{
			centerOfMass = new Vector(x,y,z);
			MoITensor = RotationMatrices.getZeroMatrix(3);
		}
		double cmShiftX = prevCenterOfMass.X-centerOfMass.X;
		double cmShiftY = prevCenterOfMass.Y-centerOfMass.Y;
		double cmShiftZ = prevCenterOfMass.Z-centerOfMass.Z;
		double rx = x-centerOfMass.X;
		double ry = y-centerOfMass.Y;
		double rz = z-centerOfMass.Z;

		MoITensor[0] = MoITensor[0] + (cmShiftY*cmShiftY + cmShiftZ*cmShiftZ)*mass + (ry*ry + rz*rz)*addedMass;
		MoITensor[1] = MoITensor[1] - cmShiftX*cmShiftY*mass - rx*ry*addedMass;
		MoITensor[2] = MoITensor[2] - cmShiftX*cmShiftZ*mass - rx*rz*addedMass;
		MoITensor[3] = MoITensor[1];
		MoITensor[4] = MoITensor[4] + (cmShiftX*cmShiftX + cmShiftZ*cmShiftZ)*mass + (rx*rx + rz*rz)*addedMass;
		MoITensor[5] = MoITensor[5] - cmShiftY*cmShiftZ*mass - ry*rz*addedMass;
		MoITensor[6] = MoITensor[2];
		MoITensor[7] = MoITensor[5];
		MoITensor[8] = MoITensor[8]  + (cmShiftX*cmShiftX + cmShiftY*cmShiftY)*mass + (rx*rx + ry*ry)*addedMass;
		
		mass += addedMass;
		invMass = 1.0D/mass;
		invMoITensor = RotationMatrices.inverse3by3(MoITensor);
//		angularVelocity = RotationMatrices.get3by3TransformedVec(oldMOI, torque);
//		angularVelocity = RotationMatrices.get3by3TransformedVec(invMoITensor, torque);
//		System.out.println(MoITensor[0]+":"+MoITensor[1]+":"+MoITensor[2]);
//		System.out.println(MoITensor[3]+":"+MoITensor[4]+":"+MoITensor[5]);
//		System.out.println(MoITensor[6]+":"+MoITensor[7]+":"+MoITensor[8]);
	}
	
	public void rawPhysTickPreCol(double newPhysSpeed,int iters){
		physSpeed = newPhysSpeed;
		iterations = iters;
		
		updateCenterOfMass();
		
		calculateFramedMOITensor();
		
		
		
	}
	
	//The x/y/z variables need to be updated when the centerOfMass location changes
	public void updateCenterOfMass(){
		Vector parentCM = parent.centerCoord;
		if(!parent.centerCoord.equals(centerOfMass)){
			Vector CMDif = centerOfMass.getSubtraction(parentCM);
			RotationMatrices.applyTransform(parent.coordTransform.lToWRotation, CMDif);
			
			parent.wrapper.posX-=CMDif.X;
			parent.wrapper.posY-=CMDif.Y;
			parent.wrapper.posZ-=CMDif.Z;
			
			parent.centerCoord = new Vector(centerOfMass);
			parent.coordTransform.updateTransforms();
		}
	}

	public void rawPhysTickPostCol(double newPhysSpeed,int iters){
		physSpeed = newPhysSpeed;
		iterations = iters;
		
		
	}
	
	//Applies the rotation transform onto the Moment of Inertia to generate the REAL MOI at that given instant
	public void calculateFramedMOITensor(){
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
		multipled.mul(pitch,inertiaBodyFrame);
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
	
	public void writeToNBTTag(NBTTagCompound compound){
		compound.setDouble("mass", mass);
		
		NBTUtils.writeVectorToNBT("linear", linearVelocity, compound);
		NBTUtils.writeVectorToNBT("angularVelocity", angularVelocity, compound);
		NBTUtils.writeVectorToNBT("CM", centerOfMass, compound);

		NBTUtils.write3x3MatrixToNBT("MOI", MoITensor, compound);
	}
	
	public void readFromNBTTag(NBTTagCompound compound){
		mass = compound.getDouble("mass");
		
		linearVelocity = NBTUtils.readVectorFromNBT("linear", compound);
		angularVelocity = NBTUtils.readVectorFromNBT("angularVelocity", compound);
		centerOfMass = NBTUtils.readVectorFromNBT("CM", compound);
		
		MoITensor = NBTUtils.read3x3MatrixFromNBT("MOI", compound);
		
		processNBTRead();
	}
	
	//Calculates the inverses and the framed MOIs
	public void processNBTRead(){
		invMoITensor = RotationMatrices.inverse3by3(MoITensor);
		invMass = 1D/mass;
	}
	
	//Called upon a Ship being created from the World, and generates the physics data for it
	public void processInitialPhysicsData(){
		IBlockState Air = Blocks.AIR.getDefaultState();
		for(BlockPos pos:parent.blockPositions){
			onSetBlockState(Air,parent.chunkCache.getBlockState(pos),pos);
		}
	}
	
}
