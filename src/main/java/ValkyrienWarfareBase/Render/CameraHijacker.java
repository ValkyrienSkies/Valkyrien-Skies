package ValkyrienWarfareBase.Render;

import java.util.List;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

public class CameraHijacker {

	//NOTE: THIS VALUE MUST ALWAYS BE CLEARED UPON WORLD UNLOAD, OTHERWISE IT CREATES A MEMEORY LEAK!
	public static PhysicsWrapperEntity mountedEntity;
	
	public static double getThirdPersonViewDist(){
		if(mountedEntity==null){
			return 4D;
		}
		return 14D;
	}
	
	public static PhysicsWrapperEntity getMountedWrapperEntity(){
		return mountedEntity;
	}
	
	public static boolean showFullPhysicsEntity(){
		return true;
	}
	
	public static RayTraceResult rayTraceExcludingWrapper(World world,Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, PhysicsWrapperEntity toExclude){
		RayTraceResult vanillaTrace = world.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
		WorldPhysObjectManager physManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
		AxisAlignedBB playerRangeBB = new AxisAlignedBB(vec31.xCoord-1D,vec31.yCoord-1D,vec31.zCoord-1D,vec31.xCoord+1D,vec31.yCoord+1D,vec31.zCoord+1D);
		List<PhysicsWrapperEntity> nearbyShips = physManager.getNearbyPhysObjects(playerRangeBB);
		boolean changed = false;
		Vec3d playerEyesPos = vec31;
        Vec3d playerReachVector = vec32.subtract(vec31);
        double reachDistance = playerReachVector.lengthVector();
		double worldResultDistFromPlayer = 420D;
		if(vanillaTrace!=null&&vanillaTrace.hitVec!=null){
			worldResultDistFromPlayer = vanillaTrace.hitVec.distanceTo(vec31);
		}
		//Do not rayTrace the specified ship
		nearbyShips.remove(toExclude);
		
		for(PhysicsWrapperEntity wrapper:nearbyShips){
            playerEyesPos = vec31;
            playerReachVector = vec32.subtract(vec31);
            
            //Transform the coordinate system for the player eye pos
            playerEyesPos = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RwToLTransform, playerEyesPos);
            playerReachVector = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RwToLRotation, playerReachVector);
            Vec3d playerEyesReachAdded = playerEyesPos.addVector(playerReachVector.xCoord * reachDistance, playerReachVector.yCoord * reachDistance, playerReachVector.zCoord * reachDistance);
            RayTraceResult resultInShip = world.rayTraceBlocks(playerEyesPos, playerEyesReachAdded, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
            if(resultInShip!=null&&resultInShip.hitVec!=null&&resultInShip.typeOfHit==Type.BLOCK){
	            double shipResultDistFromPlayer = resultInShip.hitVec.distanceTo(playerEyesPos);
	            if(shipResultDistFromPlayer<worldResultDistFromPlayer){
	            	worldResultDistFromPlayer = shipResultDistFromPlayer;
	            	resultInShip.hitVec = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RlToWTransform, resultInShip.hitVec);
	            	vanillaTrace = resultInShip;
	            }
            }
		}
		return vanillaTrace;
    }
	
}
