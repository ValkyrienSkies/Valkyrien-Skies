package valkyrienwarfare.addon.control.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.RotationMatrices;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.PhysicsCalculations;

public class TileEntityGyroscopeDampener extends TileEntity {

	private static final Vector GRAVITY_UP = new Vector(0, 1, 0);
	// 300,000 newton-meters maximum of torque.
	private double maximumTorque = 300000;
	
	public Vector getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
		Vector shipLevelNormal = new Vector(GRAVITY_UP);
		physicsCalculations.getParent().getShipTransformationManager().getCurrentPhysicsTransform().rotate(shipLevelNormal, TransformType.SUBSPACE_TO_GLOBAL);
		// Do not change this value!
		physicsCalculations.convertTorqueToVelocity();
		Vector angulerVelocityAsTorque = new Vector(physicsCalculations.angularVelocity);
		// RotationMatrices.applyTransform3by3(physicsCalculations.getPhysMOITensor(), angulerVelocityAsTorque);
		
		Vector upwardsAngularVelocityPlaneNormal = GRAVITY_UP.cross(shipLevelNormal);
		
		RotationMatrices.applyTransform3by3(physicsCalculations.getPhysMOITensor(), angulerVelocityAsTorque);
		RotationMatrices.applyTransform3by3(physicsCalculations.getPhysMOITensor(), upwardsAngularVelocityPlaneNormal);
		
		upwardsAngularVelocityPlaneNormal.normalize();
		// We want to first remove the component in the direction of the plane normal, that isn't within the plane.
		double angularTorquePlaneNormalComponent = upwardsAngularVelocityPlaneNormal.dot(angulerVelocityAsTorque);
		Vector componentToRemove = upwardsAngularVelocityPlaneNormal.getProduct(angularTorquePlaneNormalComponent);
		
		Vector componentToDampen = angulerVelocityAsTorque.getSubtraction(componentToRemove);
		
		Vector toReturn = componentToDampen.getProduct(.9D);
		
		double magnitude = toReturn.length();
		maximumTorque = 10000;
		
		if (magnitude > maximumTorque) {
			toReturn.multiply(maximumTorque / magnitude);
		}
		
		// System.out.println(magnitude);
		
		if (toReturn.dot(angulerVelocityAsTorque) > 0) {
			System.out.println("thats not right");
			// return null;
		}
		return toReturn.getProduct(physicsCalculations.getPhysicsTimeDeltaPerPhysTick());
	}
}
