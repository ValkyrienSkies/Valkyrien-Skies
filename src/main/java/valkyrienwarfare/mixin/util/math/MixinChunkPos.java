package valkyrienwarfare.mixin.util.math;

import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.ValkyrienWarfareMod;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkPos.class)
public abstract class MixinChunkPos {

	@Shadow
	@Final
	public int x;

	@Shadow
	@Final
	public int z;

	@Overwrite
	public double getDistanceSq(Entity entityIn) {
		double d0 = (double) (this.x * 16 + 8);
		double d1 = (double) (this.z * 16 + 8);
		double d2 = d0 - entityIn.posX;
		double d3 = d1 - entityIn.posZ;
		double vanilla = d2 * d2 + d3 * d3;

		if (vanilla < 91111) {
			return vanilla;
		}

		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(entityIn.world, new BlockPos(d0, 127, d1));

		if (wrapper != null) {
			Vector entityPosInLocal = new Vector(entityIn);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, entityPosInLocal);
			entityPosInLocal.subtract(d0, entityPosInLocal.Y, d1);
			return entityPosInLocal.lengthSq();
		}

		return vanilla;
	}

}
