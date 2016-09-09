package ValkyrienWarfareControl.Balloon;

import java.util.HashSet;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import gnu.trove.iterator.TIntIterator;
import net.minecraft.util.math.BlockPos;

public class BalloonProcessor {

	public PhysicsWrapperEntity parent;
	
	public HashSet<BlockPos> balloonWalls;
	public HashSet<BlockPos> internalAirPositons;
	public HashSet<BlockPos> balloonHoles;
	
	public int minX,minY,minZ,maxX,maxY,maxZ;
	
	public BalloonProcessor(PhysicsWrapperEntity parent,HashSet<BlockPos> balloonWalls,HashSet<BlockPos> internalAirPositons){
		this.balloonWalls = balloonWalls;
		this.internalAirPositons = internalAirPositons;
		balloonHoles = new HashSet<BlockPos>();
		parent = this.parent;
	}
	
	//A fast way to rule out most block positions when looking through the HashSets
	public boolean isBlockPosInRange(BlockPos toCheck){
		if(toCheck.getX()>=minX && toCheck.getX()<=maxX){
			if(toCheck.getY()>=minY && toCheck.getY()<=maxY){
				if(toCheck.getZ()>=minZ && toCheck.getZ()<=maxZ){
					return true;
				}
			}
		}
		return false;
	}
	
	public static BalloonProcessor makeProcessorForDetector(PhysicsWrapperEntity wrapper,BalloonDetector detector){
		TIntIterator ballonWallIterator = detector.balloonWalls.iterator();
		TIntIterator airPostitionsIterator = detector.foundSet.iterator();
		
		HashSet<BlockPos> staticBalloonWalls = new HashSet<BlockPos>();
		HashSet<BlockPos> staticInternalPositions = new HashSet<BlockPos>();
		
		int minX,maxX,minY,maxY,minZ,maxZ;
		
		minX = maxX = detector.firstBlock.getX();
		minY = maxY = detector.firstBlock.getY();
		minZ = maxZ = detector.firstBlock.getZ();
		
		while(ballonWallIterator.hasNext()){
			int hash = ballonWallIterator.next();
			BlockPos fromHash = detector.getPosWithRespectTo(hash, detector.firstBlock);
			staticBalloonWalls.add(fromHash);
			
			minX = Math.min(minX, fromHash.getX());minY = Math.min(minY, fromHash.getY());minZ = Math.min(minZ, fromHash.getZ());
			maxX = Math.max(maxX, fromHash.getX());maxY = Math.max(maxY, fromHash.getY());maxZ = Math.max(maxZ, fromHash.getZ());
		}
		
		while(airPostitionsIterator.hasNext()){
			int hash = airPostitionsIterator.next();
			BlockPos fromHash = detector.getPosWithRespectTo(hash, detector.firstBlock);
			staticInternalPositions.add(fromHash);
		}
		
		BalloonProcessor toReturn = new BalloonProcessor(wrapper,staticBalloonWalls,staticInternalPositions);
		
		toReturn.minX = minX;toReturn.minY = minY;toReturn.minZ = minZ;
		toReturn.maxX = maxX;toReturn.maxY = maxY;toReturn.maxZ = maxZ;
		
		return toReturn;
	}
	
}