package ValkyrienWarfareControl.Balloon;

import java.util.HashSet;

import ValkyrienWarfareBase.Relocation.SpatialDetector;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

public class BalloonAirDetector extends SpatialDetector {

	private final BalloonProcessor holeOwner;

	private final MutableBlockPos mutable = new MutableBlockPos();
	public final TIntHashSet foundBalloonWalls = new TIntHashSet(250);

	private final HashSet<BlockPos> possiblePositions;

	public BalloonAirDetector(BlockPos start, World worldIn, int maximum, BalloonProcessor processor, HashSet<BlockPos> avaliablePositions) {
		super(start, worldIn, maximum, false);
		holeOwner = processor;
		possiblePositions = avaliablePositions;
		startDetection();
	}

	@Override
	public boolean isValidExpansion(int x, int y, int z) {
		mutable.setPos(x, y, z);

		boolean isAir = possiblePositions.contains(mutable);
		if (!isAir) {
			if (holeOwner.balloonWalls.contains(mutable)) {
				foundBalloonWalls.add(this.getHashWithRespectTo(x, y, z, firstBlock));
			}
			return false;
		}
		return true;
	}

}
