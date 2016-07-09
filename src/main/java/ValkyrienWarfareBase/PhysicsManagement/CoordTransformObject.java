package ValkyrienWarfareBase.PhysicsManagement;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.Math.RotationMatrices;
import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.PhysicsManagement.Network.PhysWrapperPositionMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

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

		lToWTransform = RotationMatrices.getTranslationMatrix(parent.wrapper.posX,parent.wrapper.posY,parent.wrapper.posZ);
		
		
		
		
//		lToWTransform = RotationMatrices.getTranslationMatrix(parent.centerCoord.X,parent.centerCoord.Y,parent.centerCoord.Z);
		
		lToWTransform = RotationMatrices.rotateAndTranslate(lToWTransform,parent.wrapper.pitch, parent.wrapper.yaw, parent.wrapper.roll, parent.centerCoord);
		
		
			
		lToWRotation = RotationMatrices.getDoubleIdentity();
		
		lToWRotation = RotationMatrices.rotateOnly(lToWRotation,parent.wrapper.pitch, parent.wrapper.yaw, parent.wrapper.roll);
		
		wToLTransform = RotationMatrices.inverse(lToWTransform);
		wToLRotation = RotationMatrices.inverse(lToWRotation);
		updateParentAABB();
		updateParentNormals();
	}
	
	public void sendPositionToPlayers(){
		PhysWrapperPositionMessage posMessage = new PhysWrapperPositionMessage(parent.wrapper);
		
		for(EntityPlayerMP player:parent.watchingPlayers){
			ValkyrienWarfareMod.physWrapperNetwork.sendTo(posMessage, player);
		}
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
		double mnX=0,mnY=0,mnZ=0,mxX=0,mxY=0,mxZ=0;
		boolean first = true;
		for(BlockPos pos:parent.blockPositions){
			Vector currentLocation = new Vector(pos.getX()+.5D,pos.getY()+.5D,pos.getZ()+.5D);
			fromLocalToGlobal(currentLocation);
			
			if(first){
				mnX=mxX=currentLocation.X;
				mnY=mxY=currentLocation.Y;
				mnZ=mxZ=currentLocation.Z;
				first = false;
			}
			
			if(currentLocation.X<mnX){
				mnX = currentLocation.X;
			}
			if(currentLocation.X>mxX){
				mxX = currentLocation.X;
			}
			
			if(currentLocation.Y<mnY){
				mnY = currentLocation.Y;
			}
			if(currentLocation.Y>mxY){
				mxY = currentLocation.Y;
			}
			
			if(currentLocation.Z<mnZ){
				mnZ = currentLocation.Z;
			}
			if(currentLocation.Z>mxZ){
				mxZ = currentLocation.Z;
			}
		}
		AxisAlignedBB enclosingBB = new AxisAlignedBB(mnX,mnY,mnZ,mxX,mxY,mxZ).expand(3, 3, 3);
		parent.collisionBB = enclosingBB;
	}
	
	public void fromGlobalToLocal(Vector inGlobal){
		RotationMatrices.applyTransform(wToLTransform, inGlobal);
	}
	
	public void fromLocalToGlobal(Vector inLocal){
		RotationMatrices.applyTransform(lToWTransform, inLocal);
	}

}
