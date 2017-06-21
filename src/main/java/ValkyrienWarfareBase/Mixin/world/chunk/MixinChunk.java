package ValkyrienWarfareBase.Mixin.world.chunk;

import ValkyrienWarfareBase.ChunkManagement.PhysicsChunkManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public abstract class MixinChunk {

    @Shadow
    @Final
    public int x;

    @Shadow
    @Final
    public int z;

    @Shadow
    @Final
    public World world;

    {
//        x = 0;
//        z = 0;
//        world = null;
        // why do these have to be final lol
    }

    @Inject(method = "populateChunk(Lnet/minecraft/world/chunk/IChunkGenerator;)V", at = @At("HEAD"), cancellable = true)
    public void prePopulateChunk(IChunkGenerator generator, CallbackInfo callbackInfo) {
        if (PhysicsChunkManager.isLikelyShipChunk(this.x, this.z)) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "addEntity(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    public void preAddEntity(Entity entityIn, CallbackInfo callbackInfo) {
        World world = this.world;

        int i = MathHelper.floor(entityIn.posX / 16.0D);
        int j = MathHelper.floor(entityIn.posZ / 16.0D);

        if (i == this.x && j == this.z) {
            //do nothing, and let vanilla code take over after our injected code is done (now)
        } else {
            Chunk realChunkFor = world.getChunkFromChunkCoords(i, j);
            if (!realChunkFor.isEmpty() && realChunkFor.isChunkLoaded) {
                realChunkFor.addEntity(entityIn);
                callbackInfo.cancel(); //don't run the code on this chunk!!!
            }
        }
    }
}
