package org.valkyrienskies.mixin.world.gen;

import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.ships.ship_world.WorldServerShipManager;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.Set;

@Mixin(ChunkProviderServer.class)
public class MixinChunkProviderServer {

    @Shadow @Final
    private Set<Long> droppedChunks;
    @Shadow @Final
    public WorldServer world;

    /**
     * Used to prevent the world from unloading the chunks of ships being loaded in the background
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void preTick(CallbackInfoReturnable<Boolean> cir) {
        WorldServerShipManager physObjectWorld = (WorldServerShipManager) ValkyrienUtils.getPhysObjWorld(world);
        for (Long chunkPos : physObjectWorld.getBackgroundShipChunks()) {
            droppedChunks.remove(chunkPos);
        }
    }
}
