package ValkyrienWarfareBase.Coordinates;

import ValkyrienWarfareBase.Math.RotationMatrices;
import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;

/**
 * Handles ALL functions for moving between Ship coordinates and world coordinates
 * @author thebest108
 *
 */
public class CoordTransformObject {
	
	public PhysicsObject parent;
	public double[] lToWTransform;
	public double[] wToLTransform;
	
	public CoordTransformObject(PhysicsObject object){
		parent = object;
		updateTransforms();
	}
	//TODO: Implement this
	public void updateTransforms(){
		lToWTransform = RotationMatrices.getDoubleIdentity();
		
		
		
		wToLTransform = RotationMatrices.inverse(lToWTransform);
	}
	
	public void fromGlobalToLocal(Vector inGlobal){
		RotationMatrices.applyTransform(wToLTransform, inGlobal);
	}
	
	public void fromLocalToGlobal(Vector inLocal){
		RotationMatrices.applyTransform(lToWTransform, inLocal);
	}
	
}
