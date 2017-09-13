package valkyrienwarfare.physics;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ShipFluidProcessor {

	public final PhysicsObject parent;
	public ArrayList<BlockPos> submergedAirPositions = new ArrayList<BlockPos>();
	public ArrayList<BlockPos>[] solidPositionsAtYLevel = new ArrayList[256];

	public ShipFluidProcessor(PhysicsObject parent) {
		this.parent = parent;
	}

	public void updateSubmergedPositionsCalculations(float maxTime) {
		int yOceanLevel = getWaterLevelAtShip();
		Vector shipUpNormal = new Vector(0D, 1D, 0D, parent.coordTransform.lToWRotation);

		int maxYToCheck = yOceanLevel;
		int minYToCheck = Math.max(MathHelper.floor(parent.wrapper.getEntityBoundingBox().minY), 0);

		for (int currentY = maxYToCheck; currentY > minYToCheck; currentY++) {

		}
	}

	private int getWaterLevelAtShip() {
		return 45;
	}

	private boolean doesStateBlockWater(IBlockState state) {
		return state.isFullBlock();
	}

	public void generateYLevelData() {
		for (BlockPos pos : parent.blockPositions) {
			IBlockState state = parent.VKChunkCache.getBlockState(pos);
			if (doesStateBlockWater(state)) {
				int yPos = pos.getY();
				ArrayList<BlockPos> dataAtY = solidPositionsAtYLevel[yPos];
				if (dataAtY == null) {
					dataAtY = new ArrayList<BlockPos>();
					solidPositionsAtYLevel[yPos] = dataAtY;
				}
				dataAtY.add(pos);
			}
		}
	}

	public void onSetBlockState(BlockPos pos, IBlockState oldState, IBlockState newState) {
		int yPos = pos.getY();
		ArrayList<BlockPos> dataAtY = solidPositionsAtYLevel[yPos];
		if (dataAtY == null) {
			dataAtY = new ArrayList<BlockPos>();
			solidPositionsAtYLevel[yPos] = dataAtY;
		}
		boolean doesOldStateBlockWater = doesStateBlockWater(oldState);
		boolean doesNewStateBlockWater = doesStateBlockWater(newState);
		if (doesOldStateBlockWater != doesNewStateBlockWater) {
			if (doesNewStateBlockWater) {
				dataAtY.add(pos);
			} else {
				dataAtY.remove(pos);
			}
		}
	}

	class NestedCrossSection {
		public final ArrayList<BlockPos> normalizedSolidPositions = new ArrayList<BlockPos>();
		public final ArrayList<BlockPos> airPositions = new ArrayList<BlockPos>();
		//A 2-dimensional shape made of the solid block positions
		final int yLevelToSimulate;
		final Vector shipUpNormalVector;
		private final AxisAlignedBB shipBB;
		private final ArrayList<BlockPos>[] solidPositionsAtYLevel;
		private final double[] lToWTransform;
		public List<BlockPos> blockPositionsOfCrossSection;

		public NestedCrossSection(int yLevelToSimulate, Vector shipUpNormalVector, ArrayList<BlockPos>[] solidPositionsAtYLevel, AxisAlignedBB shipBB, double[] lToWTransform) {
			this.yLevelToSimulate = yLevelToSimulate;
			this.shipUpNormalVector = shipUpNormalVector;
			this.solidPositionsAtYLevel = solidPositionsAtYLevel;
			this.shipBB = shipBB;
			this.lToWTransform = lToWTransform;
		}

		public void runSimulation() {

		}


	}

}
