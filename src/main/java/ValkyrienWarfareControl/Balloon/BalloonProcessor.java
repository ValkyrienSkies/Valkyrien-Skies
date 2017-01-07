package ValkyrienWarfareControl.Balloon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import gnu.trove.iterator.TIntIterator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

public class BalloonProcessor {

	public PhysicsWrapperEntity parent;

	public HashSet<BlockPos> balloonWalls;
	public HashSet<BlockPos> internalAirPositions;
	public HashSet<BlockPos> balloonHoles;

	public int minX, minY, minZ, maxX, maxY, maxZ;

	public Vector currentBalloonCenter = new Vector();
	public int currentBalloonSize;

	// This determines thrust given, affected by burners and holes
	private double balloonTemperature = 295D;

	public BalloonProcessor(PhysicsWrapperEntity parent, HashSet<BlockPos> balloonWalls, HashSet<BlockPos> internalAirPositons) {
		this.parent = parent;
		this.balloonWalls = balloonWalls;
		this.internalAirPositions = internalAirPositons;
		balloonHoles = new HashSet<BlockPos>();
		updateBalloonCenter();
	}

	public Vector getBalloonForce(double secondsToSimulate, PhysicsCalculations processor) {
		Vector forceVector = new Vector(processor.gravity);

		double displacedMass = Math.max(getBalloonAirMassAtAmbient() - getBalloonAirMass(), 0D);

		// TODO: Do math here
		forceVector.multiply(-displacedMass);

		// System.out.println(forceVector);

		return forceVector;
	}

	public Vector getForceCenter() {
		Vector adjustedCenter = new Vector(currentBalloonCenter);

		return adjustedCenter;
	}

	public void tickBalloonTemperatures(double secondsToSimulate, PhysicsCalculations processor) {
		balloonTemperature = 320D;

		processBalloonCooling(secondsToSimulate);
		// System.out.println(internalAirPositions.size());
	}

	private void processBalloonCooling(double secondsToSimulate) {

		for (BlockPos holes : balloonHoles) {

		}

	}

	/**
	 * Gas Mass in Grams! Gas Temp in Celcius!
	 * 
	 * @param gasMass
	 * @param gasTemp
	 */
	public void addGasAtTemp(double gasMass, double gasTemp) {
		double currentMass = getBalloonAirMass();

		double currentTemp = getBalloonTemperature();

		double temperatureKg = currentMass * currentTemp + gasMass * (gasTemp - currentTemp);

		balloonTemperature = temperatureKg / getBalloonAirMass();
	}

	public double getBalloonAirMassAtAmbient() {
		double airMassAtAtmosphere = 15.25D;

		return airMassAtAtmosphere * getAmbientPressure() * internalAirPositions.size();

	}

	public double getBalloonAirMass() {
		double massRatio = getAmbientTemperature() / getBalloonTemperature();

		return massRatio * getBalloonAirMassAtAmbient();
	}

	public double getAmbientTemperature() {
		return 295D;
	}

	/**
	 * @return Returns the ambient pressure in Atmosphere units
	 */
	public double getAmbientPressure() {
		Vector centerPos = new Vector((minX + maxX) / 2D, (minY + maxY) / 2D, (minZ + maxZ) / 2D);

		parent.wrapping.coordTransform.fromLocalToGlobal(centerPos);

		double yPos = centerPos.Y;

		double pressure = 60D / yPos;

		return Math.pow(pressure, .25D);
	}

	/**
	 * This returns a temperature in Kelvin
	 * 
	 * @return
	 */
	public double getBalloonTemperature() {
		return balloonTemperature;
	}

	public void processBlockUpdates(ArrayList<BlockPos> updates) {

		// System.out.println("Original "+internalAirPositions.size());

		for (BlockPos pos : updates) {
			if (isBlockPosInRange(pos)) {
				IBlockState state = parent.wrapping.VKChunkCache.getBlockState(pos);
				Block block = state.getBlock();
				if (block.blockMaterial.blocksMovement()) {
					if (internalAirPositions.contains(pos)) {
						// No longer an air position
						internalAirPositions.remove(pos);
						balloonWalls.add(pos);
					} else {
						// Possibly add it to internalAirPositions?
						// Or maybe fill in a hole?
					}
					balloonHoles.remove(pos);
				} else {
					if (balloonWalls.contains(pos)) {
						// Just created a hole
						// System.out.println("Hole Created");
						balloonHoles.add(pos);
					} else {
						if (doSecondHoleCheck(pos, getAdjacentPositions(pos))) {
							internalAirPositions.add(pos);
						}
					}
				}
			}
		}

		if (!updates.isEmpty()) {

			checkBalloonForSplit();

			checkHolesForFixFull();

			updateBalloonCenter();
			updateBalloonRange();
		}

		// System.out.println("Post "+internalAirPositions.size());
	}

	// Loop through all balloon air positions, and if some are split; remove the smaller group
	private void checkBalloonForSplit() {
		HashSet<BlockPos> posititionsNeedingAtatchment = new HashSet<BlockPos>();
		ArrayList<BalloonAirDetector> foundSets = new ArrayList<BalloonAirDetector>();
		MutableBlockPos mutable = new MutableBlockPos();

		posititionsNeedingAtatchment.addAll(internalAirPositions);
		// posititionsNeedingAtatchment.addAll(balloonHoles);

		while (posititionsNeedingAtatchment.size() > 0) {
			BlockPos start = getRandomPosFromSet(posititionsNeedingAtatchment);

			BalloonAirDetector detector = new BalloonAirDetector(start, parent.worldObj, currentBalloonSize, this, posititionsNeedingAtatchment);

			foundSets.add(detector);

			TIntIterator iterator = detector.foundSet.iterator();

			while (iterator.hasNext()) {
				int hash = iterator.next();

				detector.setPosWithRespectTo(hash, start, mutable);

				posititionsNeedingAtatchment.remove(mutable);
			}
		}

		if (foundSets.size() > 1) {
			// System.out.println("Original: "+internalAirPositions.size());

			for (BalloonAirDetector split : foundSets) {
				System.out.println(split.foundSet.size());
			}

			processFoundSplits(foundSets);

			// System.out.println("Post: "+internalAirPositions.size());
		}
	}

	private void processFoundSplits(ArrayList<BalloonAirDetector> foundSets) {
		// System.out.println("Running a split");

		int maxSplitSize = -1;

		for (BalloonAirDetector split : foundSets) {
			// System.out.println(split.foundSet.size());
			if (split.foundSet.size() > maxSplitSize) {
				maxSplitSize = split.foundSet.size();
			}
		}

		for (BalloonAirDetector split : foundSets) {
			if (split.foundSet.size() != maxSplitSize) {
				TIntIterator airIterator = split.foundSet.iterator();
				TIntIterator wallIterator = split.foundBalloonWalls.iterator();

				// System.out.println("Set to remove "+split.foundSet.size() +" positions");

				while (airIterator.hasNext()) {
					int hash = airIterator.next();

					BlockPos pos = split.getPosWithRespectTo(hash, split.firstBlock);

					internalAirPositions.remove(pos);
					balloonHoles.remove(pos);
					// balloonWalls.remove(pos);
				}

				while (wallIterator.hasNext()) {
					int hash = wallIterator.next();

					BlockPos pos = split.getPosWithRespectTo(hash, split.firstBlock);

					balloonWalls.add(pos);
					internalAirPositions.remove(pos);
					balloonHoles.remove(pos);
				}
			}
		}
	}

	private static BlockPos getRandomPosFromSet(HashSet<BlockPos> positions) {
		for (BlockPos pos : positions) {
			return pos;
		}
		return null;
	}

	public void checkHolesForFixFull() {
		ArrayList<BlockPos> balloonHoleCopy = new ArrayList<BlockPos>(balloonHoles);
		for (BlockPos pos : balloonHoleCopy) {
			BlockPos[] adjacentPositions = getAdjacentPositions(pos);
			if (balloonHoles.contains(pos)) {
				if (doFirstHoleCheck(pos, adjacentPositions)) {
					removeHole(pos);
				} else {
					if (doSecondHoleCheck(pos, adjacentPositions)) {
						removeHole(pos);
					} else {
						if (doLastHoleCheck(pos, adjacentPositions)) {
							removeHole(pos);
						}
					}
				}
			}
		}
		// System.out.println("balloonHoles size is "+balloonHoles.size());
	}

	public void checkHolesForFixPartial() {
		ArrayList<BlockPos> balloonHoleCopy = new ArrayList<BlockPos>(balloonHoles);
		for (BlockPos pos : balloonHoleCopy) {
			BlockPos[] adjacentPositions = getAdjacentPositions(pos);
			if (balloonHoles.contains(pos)) {
				if (doFirstHoleCheck(pos, adjacentPositions)) {
					removeHole(pos);
				} else {
					if (doSecondHoleCheck(pos, adjacentPositions)) {
						removeHole(pos);
					} else {
						// if(doLastHoleCheck(pos,adjacentPositions)){
						// removeHole(pos);
						// }
					}
				}
			}
		}
		// System.out.println("balloonHoles size is "+balloonHoles.size());
	}

	private void removeHole(BlockPos holePosition) {
		balloonHoles.remove(holePosition);
	}

	// Just check if the hole is even connected to the internal air of the ballon; if not, get rid of it!
	// return true if you want to verify hole has been fixed (or if you just want it to be removed)
	private boolean doFirstHoleCheck(BlockPos holeToCheck, BlockPos[] adjacentPositions) {
		for (BlockPos nearbyPosition : adjacentPositions) {
			if (/* balloonWalls.contains(nearbyPosition)|| */internalAirPositions.contains(nearbyPosition)/* ||balloonHoles.contains(nearbyPosition) */) {
				// Connected to balloon, go onto next check
				return false;
			}
		}
		// Not connected, hole should be removed
		return true;
	}

	// This checks if the adjacentPositions are completely filled
	private boolean doSecondHoleCheck(BlockPos holeToCheck, BlockPos[] adjacentPositions) {
		for (BlockPos nearbyPosition : adjacentPositions) {
			if (!(balloonWalls.contains(nearbyPosition) || internalAirPositions.contains(nearbyPosition))) {
				// A nearby position isnt included in the balloon, KEEP GOING!
				return false;

				// TODO: Maybe re-add this shit, probably not though
				// IBlockState nearbyState = parent.wrapping.VKChunkCache.getBlockState(nearbyPosition);
				// if(nearbyState.getBlock().blockMaterial.blocksMovement()){
				//
				// }
			}
		}
		return true;
	}

	private boolean doLastHoleCheck(BlockPos holeToCheck, BlockPos[] adjacentPositions) {
		BalloonHoleDetector holeDetector = new BalloonHoleDetector(holeToCheck, parent.worldObj, 2500, this);

		// System.out.println("Original "+internalAirPositions.size());

		if (!holeDetector.cleanHouse) {
			// Wow the hole is actually filled! Add the new positions here!

			TIntIterator newBallonWallIterator = holeDetector.newBalloonWalls.iterator();
			TIntIterator newAirPostitionsIterator = holeDetector.foundSet.iterator();

			while (newBallonWallIterator.hasNext()) {
				int hash = newBallonWallIterator.next();
				BlockPos fromHash = holeDetector.getPosWithRespectTo(hash, holeDetector.firstBlock);
				balloonWalls.add(fromHash);
				internalAirPositions.remove(fromHash);
				balloonHoles.remove(fromHash);
			}

			while (newAirPostitionsIterator.hasNext()) {
				int hash = newAirPostitionsIterator.next();
				BlockPos fromHash = holeDetector.getPosWithRespectTo(hash, holeDetector.firstBlock);
				internalAirPositions.add(fromHash);
				balloonWalls.remove(fromHash);
				balloonHoles.remove(fromHash);
			}

			// System.out.println("Post "+internalAirPositions.size());

			return true;
		}
		return false;
	}

	private BlockPos[] getAdjacentPositions(BlockPos pos) {
		BlockPos up = pos.up();
		BlockPos down = pos.down();
		BlockPos north = pos.north();
		BlockPos east = pos.east();
		BlockPos south = pos.south();
		BlockPos west = pos.west();

		BlockPos[] positions = new BlockPos[6];

		positions[0] = up;
		positions[1] = down;
		positions[2] = north;
		positions[3] = east;
		positions[4] = south;
		positions[5] = west;

		return positions;
	}

	public void updateBalloonCenter() {
		currentBalloonCenter.zero();
		currentBalloonSize = internalAirPositions.size();
		Iterator<BlockPos> blockPosIterator = internalAirPositions.iterator();
		while (blockPosIterator.hasNext()) {
			BlockPos current = blockPosIterator.next();
			currentBalloonCenter.X += current.getX();
			currentBalloonCenter.Y += current.getY();
			currentBalloonCenter.Z += current.getZ();
		}
		currentBalloonCenter.multiply(1D / currentBalloonSize);
		currentBalloonCenter.X += .5D;
		currentBalloonCenter.Y += .5D;
		currentBalloonCenter.Z += .5D;
	}

	public void updateBalloonRange() {
		Iterator<BlockPos> blockPosIterator = balloonWalls.iterator();

		BlockPos firstPos = blockPosIterator.next();

		minX = maxX = firstPos.getX();
		minY = maxY = firstPos.getY();
		minZ = maxZ = firstPos.getZ();

		while (blockPosIterator.hasNext()) {
			BlockPos pos = blockPosIterator.next();
			minX = Math.min(minX, pos.getX());
			minY = Math.min(minY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxX = Math.max(maxX, pos.getX());
			maxY = Math.max(maxY, pos.getY());
			maxZ = Math.max(maxZ, pos.getZ());
		}
	}

	// A fast way to rule out most block positions when looking through the HashSets
	public boolean isBlockPosInRange(BlockPos toCheck) {
		if (toCheck.getX() >= minX && toCheck.getX() <= maxX) {
			if (toCheck.getY() >= minY && toCheck.getY() <= maxY) {
				if (toCheck.getZ() >= minZ && toCheck.getZ() <= maxZ) {
					return true;
				}
			}
		}
		return false;
	}

	public static BalloonProcessor makeProcessorForDetector(PhysicsWrapperEntity wrapper, BalloonDetector detector) {
		TIntIterator ballonWallIterator = detector.balloonWalls.iterator();
		TIntIterator airPostitionsIterator = detector.foundSet.iterator();

		HashSet<BlockPos> staticBalloonWalls = new HashSet<BlockPos>();
		HashSet<BlockPos> staticInternalPositions = new HashSet<BlockPos>();

		int minX, maxX, minY, maxY, minZ, maxZ;

		minX = maxX = detector.firstBlock.getX();
		minY = maxY = detector.firstBlock.getY();
		minZ = maxZ = detector.firstBlock.getZ();

		while (ballonWallIterator.hasNext()) {
			int hash = ballonWallIterator.next();
			BlockPos fromHash = detector.getPosWithRespectTo(hash, detector.firstBlock);
			staticBalloonWalls.add(fromHash);

			minX = Math.min(minX, fromHash.getX());
			minY = Math.min(minY, fromHash.getY());
			minZ = Math.min(minZ, fromHash.getZ());
			maxX = Math.max(maxX, fromHash.getX());
			maxY = Math.max(maxY, fromHash.getY());
			maxZ = Math.max(maxZ, fromHash.getZ());
		}

		while (airPostitionsIterator.hasNext()) {
			int hash = airPostitionsIterator.next();
			BlockPos fromHash = detector.getPosWithRespectTo(hash, detector.firstBlock);
			staticInternalPositions.add(fromHash);
		}

		BalloonProcessor toReturn = new BalloonProcessor(wrapper, staticBalloonWalls, staticInternalPositions);

		toReturn.minX = minX;
		toReturn.minY = minY;
		toReturn.minZ = minZ;
		toReturn.maxX = maxX;
		toReturn.maxY = maxY;
		toReturn.maxZ = maxZ;

		return toReturn;
	}

}