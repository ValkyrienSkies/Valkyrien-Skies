package ValkyrienWarfareBase.Mixin.world.border;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import ValkyrienWarfareBase.ChunkManagement.PhysicsChunkManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.border.WorldBorder;

@Mixin(WorldBorder.class)
public abstract class MixinWorldBorder {

	@Overwrite
    public boolean contains(BlockPos pos) {
        if (PhysicsChunkManager.isLikelyShipChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return true;
        }
        return (double)(pos.getX() + 1) > this.minX() && (double)pos.getX() < this.maxX() && (double)(pos.getZ() + 1) > this.minZ() && (double)pos.getZ() < this.maxZ();
    }

	@Overwrite
    public boolean contains(ChunkPos range) {
        if (PhysicsChunkManager.isLikelyShipChunk(range.x, range.z)) {
            return true;
        }
        return (double)range.getXEnd() > this.minX() && (double)range.getXStart() < this.maxX() && (double)range.getZEnd() > this.minZ() && (double)range.getZStart() < this.maxZ();
    }

    @Overwrite
    public boolean contains(AxisAlignedBB bb) {
        int xPos = (int) bb.minX;
        int zPos = (int) bb.minZ;
        if (PhysicsChunkManager.isLikelyShipChunk(xPos >> 4, zPos >> 4)) {
            return true;
        }
        return bb.maxX > this.minX() && bb.minX < this.maxX() && bb.maxZ > this.minZ() && bb.minZ < this.maxZ();
    }

    @Shadow
    public abstract double minX();

    @Shadow
    public abstract double minZ();

    @Shadow
    public abstract double maxX();

    @Shadow
    public abstract double maxZ();

}
