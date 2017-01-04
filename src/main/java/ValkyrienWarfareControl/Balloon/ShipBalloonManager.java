package ValkyrienWarfareControl.Balloon;

import java.util.ArrayList;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class ShipBalloonManager {

	public ArrayList<BalloonProcessor> balloonProcessors = new ArrayList<BalloonProcessor>();
	public ArrayList<BlockPos> recentBlockPositionChanges = new ArrayList<BlockPos>();
	public PhysicsObject parent;
	private int curBalloonTick;

	public ShipBalloonManager(PhysicsObject parent) {
		this.parent = parent;
	}

	// Searches 5 blocks up for a processor, if one cant be found then it returns null
	public BalloonProcessor getProcessorAbovePos(BlockPos burnerPos) {
		for (int i = 1; i <= 5; i++) {
			BlockPos toCheck = burnerPos.up(i);
			IBlockState state = parent.VKChunkCache.getBlockState(toCheck);
			Block block = state.getBlock();
			if (block.blockMaterial.blocksMovement()) {
				// End the loop
				i = 420;
			} else {
				for (BalloonProcessor processor : balloonProcessors) {
					if (processor.isBlockPosInRange(toCheck)) {
						if (processor.internalAirPositions.contains(toCheck)) {
							return processor;
						}
					}
				}
			}
		}
		return null;
	}

	public void addBalloonProcessor(BalloonProcessor toAdd) {
		balloonProcessors.add(toAdd);
	}

	public void onPostTick() {
		curBalloonTick++;
		if (curBalloonTick > 20) {
			curBalloonTick = 0;
			processRecentBlockChanges();
			// System.out.println("updated");
		}
	}

	private void processRecentBlockChanges() {
		if (!recentBlockPositionChanges.isEmpty()) {
			for (BalloonProcessor processor : balloonProcessors) {
				processor.processBlockUpdates(recentBlockPositionChanges);
			}

			// System.out.println("Processed "+recentBlockPositionChanges.size()+" block changes");

			recentBlockPositionChanges.clear();
		}
	}

	public void onBlockPositionRemoved(BlockPos justRemoved) {
		if (!recentBlockPositionChanges.contains(justRemoved)) {
			recentBlockPositionChanges.add(justRemoved);
		}
	}

	public void onBlockPositionAdded(BlockPos justAdded) {
		if (!recentBlockPositionChanges.contains(justAdded)) {
			recentBlockPositionChanges.add(justAdded);
		}
	}

}
