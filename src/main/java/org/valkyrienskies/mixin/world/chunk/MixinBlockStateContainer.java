package org.valkyrienskies.mixin.world.chunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.IBlockStatePalette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.mod.common.physics.collision.optimization.IBitOctree;
import org.valkyrienskies.mod.common.physics.collision.optimization.IBitOctreeProvider;
import org.valkyrienskies.mod.common.physics.collision.optimization.SimpleBitOctree;

@Mixin(BlockStateContainer.class)
public class MixinBlockStateContainer implements IBitOctreeProvider {

    @Shadow
    @Final
    public static IBlockState AIR_BLOCK_STATE;
    private final IBitOctree bitOctree = new SimpleBitOctree();
    @Shadow
    public IBlockStatePalette palette;
    @Shadow
    public BitArray storage;

    /**
     * @author thebest108
     */
    @Overwrite
    public void set(int index, IBlockState state) {
        if (state == null) {
            state = AIR_BLOCK_STATE;
        }
        int i = this.palette.idFor(state);
        this.storage.setAt(index, i);

        // VS code starts here:
        int x = index & 0xF;
        int z = (index & 0xF0) >> 4;
        int y = (index & 0xF00) >> 8;
        boolean isStateSolid = state.getMaterial().isSolid();
        bitOctree.set(x & 15, y & 15, z & 15, isStateSolid);
    }

    @Override
    public IBitOctree getBitOctree() {
        return bitOctree;
    }
}