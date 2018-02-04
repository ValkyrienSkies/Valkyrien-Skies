package valkyrienwarfare.mixin.world.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.IBlockStatePalette;
import valkyrienwarfare.physics.collision.optimization.IBitOctree;
import valkyrienwarfare.physics.collision.optimization.IBitOctreeProvider;
import valkyrienwarfare.physics.collision.optimization.SimpleBitOctree;

@Mixin(BlockStateContainer.class)
public class MixinBlockStateContainer implements IBitOctreeProvider {

	private static final int BOTTOM_FOUR_BITMASK = 0xF;
	private static final int MIDDLE_FOUR_BITMASK = 0xF0;
	private static final int TOP_FOUR_BITMASK = 0xF00;
	private IBitOctree bitOctree;
	@Shadow
	IBlockStatePalette palette;
	@Shadow
	BitArray storage;

	@Inject(method = "<init>()V", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		bitOctree = new SimpleBitOctree();
	}

	@Overwrite
	protected void set(int index, IBlockState state) {
		int i = this.palette.idFor(state);
		this.storage.setAt(index, i);
		// VW code starts here:
		int x = index & BOTTOM_FOUR_BITMASK;
		int z = (index & MIDDLE_FOUR_BITMASK) >> 4;
		int y = (index & TOP_FOUR_BITMASK) >> 8;
		boolean isStateSolid = state.getMaterial().isSolid();
		bitOctree.set(x & 15, y & 15, z & 15, isStateSolid);
	}

	@Override
	public IBitOctree getBitOctree() {
		return bitOctree;
	}
}