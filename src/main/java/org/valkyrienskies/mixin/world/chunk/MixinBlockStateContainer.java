package org.valkyrienskies.mixin.world.chunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.IBlockStatePalette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.mod.common.util.datastructures.IBitOctree;
import org.valkyrienskies.mod.common.util.datastructures.ITerrainOctreeProvider;
import org.valkyrienskies.mod.common.util.datastructures.SimpleBitOctree;

@Mixin(BlockStateContainer.class)
public class MixinBlockStateContainer implements ITerrainOctreeProvider {

    @Shadow
    @Final
    public static IBlockState AIR_BLOCK_STATE;
    private final IBitOctree solidOctree = new SimpleBitOctree();
    private final IBitOctree liquidOctree = new SimpleBitOctree();
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
        final int i = this.palette.idFor(state);
        this.storage.setAt(index, i);

        // VS code starts here:
        final int x = index & 0xF;
        final int z = (index & 0xF0) >> 4;
        final int y = (index & 0xF00) >> 8;
        final boolean isStateSolid = state.getMaterial().isSolid();
        solidOctree.set(x & 15, y & 15, z & 15, isStateSolid);
        final boolean isStateLiquid = state.getMaterial().isLiquid();
        liquidOctree.set(x & 15, y & 15, z & 15, isStateLiquid);
    }

    @Override
    public IBitOctree getSolidOctree() {
        return solidOctree;
    }

    @Override
    public IBitOctree getLiquidOctree() {
        return liquidOctree;
    }
}