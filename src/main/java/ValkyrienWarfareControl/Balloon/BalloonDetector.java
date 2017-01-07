package ValkyrienWarfareControl.Balloon;

import ValkyrienWarfareBase.Relocation.SpatialDetector;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class BalloonDetector extends SpatialDetector {

	private final MutableBlockPos mutable = new MutableBlockPos();
	public final TIntHashSet balloonWalls = new TIntHashSet(250);

	public BalloonDetector(BlockPos start, World worldIn, int maximum) {
		super(start, worldIn, maximum, false);
		startDetection();
	}

	@Override
	public void calculateSpatialOccupation() {
		nextQueue.add(firstBlock.getY() + maxRange * maxRangeHalved + maxRangeSquared * maxRangeHalved);
		MutableBlockPos inRealWorld = new MutableBlockPos();
		int hash;
		while (!nextQueue.isEmpty() && !cleanHouse) {
			TIntIterator queueIter = nextQueue.iterator();
			foundSet.addAll(nextQueue);
			nextQueue = new TIntHashSet();
			while (queueIter.hasNext()) {
				hash = queueIter.next();
				setPosWithRespectTo(hash, firstBlock, inRealWorld);

				tryExpanding(inRealWorld.getX() + 1, inRealWorld.getY(), inRealWorld.getZ(), hash + maxRange);
				tryExpanding(inRealWorld.getX() - 1, inRealWorld.getY(), inRealWorld.getZ(), hash - maxRange);
				tryExpanding(inRealWorld.getX(), inRealWorld.getY() + 1, inRealWorld.getZ(), hash + 1);
				// tryExpanding(inRealWorld.getX(),inRealWorld.getY()-1,inRealWorld.getZ(),hash-1);
				tryExpanding(inRealWorld.getX(), inRealWorld.getY(), inRealWorld.getZ() + 1, hash + maxRangeSquared);
				tryExpanding(inRealWorld.getX(), inRealWorld.getY(), inRealWorld.getZ() - 1, hash - maxRangeSquared);
			}
		}
	}

	@Override
	public void tryExpanding(int x, int y, int z, int hash) {
		if (isValidExpansion(x, y, z)) {
			if (!foundSet.contains(hash) && (foundSet.size() + nextQueue.size() < maxSize)) {
				nextQueue.add(hash);
			}
		} else {
			if (!balloonWalls.contains(hash)) {
				balloonWalls.add(hash);
			}
		}
	}

	@Override
	public boolean isValidExpansion(int x, int y, int z) {
		IBlockState state = cache.getBlockState(x, y, z);
		if (!state.getBlock().blockMaterial.blocksMovement()) {
			Chunk chunk = cache.getChunkAt(x >> 4, z >> 4);
			mutable.setPos(x, y, z);
			if (!chunk.canSeeSky(mutable)) {
				return true;
			} else {
				cleanHouse = true;
				// System.out.println("Found a Hole in the Air-Balloon");
			}
		}
		return false;
	}

}
