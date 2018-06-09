package valkyrienwarfare.mixin.client.renderer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

@Mixin(ViewFrustum.class)
public class MixinViewFrustum {

	@Shadow @Final
    protected World world;
	
	@Inject(at = { @At("HEAD") }, method = { "markBlocksForUpdate(IIIIIIZ)V" })
    public void pre_markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately, CallbackInfo info) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, new BlockPos(minX, minY, minZ));
		if (wrapper != null) {
			// System.out.println("Update");
			wrapper.getPhysicsObject().getShipRenderer().updateRange(minX, minY, minZ, maxX, maxY, maxZ, updateImmediately);
		}
	}
}
