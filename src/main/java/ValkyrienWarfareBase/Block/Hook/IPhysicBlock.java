package ValkyrienWarfareBase.Block.Hook;

import net.minecraft.util.math.BlockPos;

public interface IPhysicBlock {

	public void onPhysicTick(BlockPos pos); // TODO add needed parameter such as the ship and things which allows to add forces easy

}
