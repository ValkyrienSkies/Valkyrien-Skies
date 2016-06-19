package ValkyrienWarfareBase.Coordinates;

import ValkyrienWarfareBase.Math.RotationMatrices;
import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.util.Rotation;

/**
 * Handles ALL functions for moving between Ship coordinates and world coordinates
 * @author thebest108
 *
 */
public class CoordTransformObject {
	
	public PhysicsObject parent;
	public double[] lToWTransform = RotationMatrices.getDoubleIdentity();
	public double[] wToLTransform = RotationMatrices.getDoubleIdentity();
	
	public double[] prevlToWTransform;
	public double[] prevwToLTransform;
	
	public double[] lToWRotation = RotationMatrices.getDoubleIdentity();
	public double[] wToLRotation = RotationMatrices.getDoubleIdentity();
	
	public Vector[] normals = Vector.generateAxisAlignedNorms();
	
	public CoordTransformObject(PhysicsObject object){
		parent = object;
		updateTransforms();
		prevlToWTransform = lToWTransform;
		prevwToLTransform = wToLTransform;
	}
	//TODO: Implement this
	public void updateTransforms(){
		prevlToWTransform = lToWTransform;
		prevwToLTransform = wToLTransform;
		
//		lToWTransform = RotationMatrices.getTranslationMatrix(parent.centerCoord.X,parent.centerCoord.Y,parent.centerCoord.Z);
		
		lToWTransform = RotationMatrices.rotateAndTranslate(lToWTransform,parent.pitch, parent.yaw, parent.roll, parent.centerCoord);
		
		lToWTransform = RotationMatrices.getMatrixProduct(lToWTransform, RotationMatrices.getTranslationMatrix(parent.wrapper.posX,parent.wrapper.posY,parent.wrapper.posZ));
		
		
		
		lToWRotation = RotationMatrices.rotateOnly(lToWRotation,parent.pitch, parent.yaw, parent.roll);
		
		
		
		wToLTransform = RotationMatrices.inverse(lToWTransform);
		wToLRotation = RotationMatrices.inverse(lToWRotation);
		updateParentAABB();
		updateParentNormals();
	}
	
	public void updateParentNormals(){
		normals = new Vector[15];
		//Used to generate Normals for the Axis Aligned World
		Vector[] alignedNorms = Vector.generateAxisAlignedNorms();
		Vector[] rotatedNorms = generateRotationNormals();
		for(int i = 0;i<6;i++){
			Vector currentNorm=null;
			if(i<3){
				currentNorm = alignedNorms[i];
			}else{
				currentNorm = rotatedNorms[i-3];
			}
			normals[i] = currentNorm;
		}
		int cont = 6;
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				Vector norm = normals[i].crossAndUnit(normals[j+3]);
				normals[cont] = norm;
				cont++;
			}
		}
		for(int i=0;i<normals.length;i++){
			if(normals[i].isZero()){
				normals[i] = new Vector(0.0D,1.0D,0.0D);
			}
		}
		normals[0] = new Vector(1.0D,0.0D,0.0D);
		normals[1] = new Vector(0.0D,1.0D,0.0D);
		normals[2] = new Vector(0.0D,0.0D,1.0D);
	}
	
	public Vector[] generateRotationNormals() {
		Vector[] norms = Vector.generateAxisAlignedNorms();
		for(int i=0;i<3;i++){
			RotationMatrices.applyTransform(lToWRotation, norms[i]);
		}
		return norms;
	}
	
	//TODO: FinishME
	public void updateParentAABB(){
		double mnX,mnY,mnZ,mxX,mxY,mxZ;
		Vector currentLocation = new Vector();
	}
	
	public void fromGlobalToLocal(Vector inGlobal){
		RotationMatrices.applyTransform(wToLTransform, inGlobal);
	}
	
	public void fromLocalToGlobal(Vector inLocal){
		RotationMatrices.applyTransform(lToWTransform, inLocal);
	}
	
}
