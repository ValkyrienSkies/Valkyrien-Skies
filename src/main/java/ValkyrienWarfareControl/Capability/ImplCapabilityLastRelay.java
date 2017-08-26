package ValkyrienWarfareControl.Capability;

import net.minecraft.util.math.BlockPos;

public class ImplCapabilityLastRelay implements ICapabilityLastRelay {

	BlockPos lastRelay;

	@Override
	public BlockPos getLastRelay() {
		return lastRelay;
	}

	@Override
	public void setLastRelay(BlockPos pos) {
		lastRelay = pos;
	}

	@Override
	public boolean hasLastRelay() {
		return lastRelay != null;
	}

}
