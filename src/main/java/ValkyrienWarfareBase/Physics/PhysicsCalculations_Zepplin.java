package ValkyrienWarfareBase.Physics;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.nbt.NBTTagCompound;

public class PhysicsCalculations_Zepplin extends PhysicsCalculations {

	public double yawRate;
	public double forwardRate = 1D;
	public double upRate;

	public PhysicsCalculations_Zepplin(PhysicsObject toProcess) {
		super(toProcess);
	}

	@Override
	public void calculateForces() {
		double modifiedDrag = Math.pow(drag, physTickSpeed / .05D);
		linearMomentum.multiply(modifiedDrag);
		angularVelocity.multiply(modifiedDrag);

//		System.out.println("hi?");

//		parent.wrapper.isDead = true;

//		forwardRate = 1D;

		double[] existingRotationMatrix = RotationMatrices.getRotationMatrix(0, parent.wrapper.yaw, 0);

		double reduction = .001D;

		Vector linearForce = new Vector(forwardRate, upRate, 0);
		RotationMatrices.applyTransform(existingRotationMatrix, linearForce);
		linearForce.multiply(mass);
		linearForce.multiply(reduction);
		this.linearMomentum.add(linearForce);;




		double[] rotationVelocityMatrix = RotationMatrices.getRotationMatrix(0, yawRate, 0);

//		angularVelocity = new Vector(0, yawRate, 0);

//		angularVelocity.add(0, yawRate * reduction, 0);

//		if(parent.wrapper.yaw)

		if(parent.wrapper.yaw > 0) {
//			angularVelocity.multiply(-1D);
		}
	}

	@Override
	public void applyGravity() {

	}

	@Override
	public void rawPhysTickPostCol() {
//		super.rawPhysTickPostCol();

		applyLinearVelocity();

		parent.wrapper.pitch = 0;
		parent.wrapper.roll = 0;

		yawRate = 0;// 10D/20D;

		parent.wrapper.yaw -= yawRate;

		if(parent.wrapper.yaw > 180) {

//			parent.wrapper.yaw -= 360;
		}

		if(parent.wrapper.yaw < -180) {
//			System.out.println("shit");
//			parent.wrapper.yaw += 360;
		}

//		parent.wrapper.yaw*= -1;

		parent.coordTransform.updateAllTransforms();
	}

	@Override
	public void writeToNBTTag(NBTTagCompound compound) {
		super.writeToNBTTag(compound);
		compound.setDouble("yawRate", yawRate);
		compound.setDouble("forwardRate", forwardRate);
		compound.setDouble("upRate", upRate);
	}

	@Override
	public void readFromNBTTag(NBTTagCompound compound) {
		super.readFromNBTTag(compound);
		yawRate = compound.getDouble("yawRate");
		forwardRate = compound.getDouble("forwardRate");
		upRate = compound.getDouble("upRate");
	}

}
