package ValkyrienWarfareControl.Balloon;

import ValkyrienWarfareBase.Relocation.SpatialDetector;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class BalloonHoleDetector extends SpatialDetector {

	private final BalloonProcessor holeOwner;

	private final MutableBlockPos mutable = new MutableBlockPos();
	public final TIntHashSet newBalloonWalls = new TIntHashSet(250);

	public BalloonHoleDetector(BlockPos start, World worldIn, int maximum, BalloonProcessor processor) {
		super(start, worldIn, maximum, false);
		holeOwner = processor;
		startDetection();
	}

	@Override
	public boolean isValidExpansion(int x, int y, int z) {
		IBlockState state = cache.getBlockState(x, y, z);
		if (!state.getBlock().blockMaterial.blocksMovement()) {
			Chunk chunk = cache.getChunkAt(x >> 4, z >> 4);
			mutable.setPos(x, y, z);
			if (!chunk.canSeeSky(mutable)) {
				if (!holeOwner.internalAirPositions.contains(mutable)) {
					// System.out.println("Internal Air Position Grabbed");
					return true;
				} else {
					// Already an air position owned by the balloon
					return false;
				}
			} else {
				// System.out.println("cleaning house bitch");
				// Balloon hole guaranteed not to be closed, end the process NOW!
				cleanHouse = true;
			}
		} else {
			// Potentially a new balloonWallPosition; better mark it
			int hash = getHashWithRespectTo(x, y, z, firstBlock);
			if (!newBalloonWalls.contains(hash)) {
				newBalloonWalls.add(hash);
			}
		}
		return false;
	}

}
