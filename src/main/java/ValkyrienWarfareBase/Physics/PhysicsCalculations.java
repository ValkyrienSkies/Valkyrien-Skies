package ValkyrienWarfareBase.Physics;

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
	
	public void addMassAt(double x,double y,double z,double addedMass){
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
	
	public void rawPhysTick(double newPhysSpeed,int iters){
		physSpeed = newPhysSpeed;
		iterations = iters;
		
		
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
	
	public void processInitialPhysicsData(){
		IBlockState Air = Blocks.AIR.getDefaultState();
		for(BlockPos pos:parent.blockPositions){
			onSetBlockState(Air,parent.chunkCache.getBlockState(pos),pos);
		}
	}
	
}
