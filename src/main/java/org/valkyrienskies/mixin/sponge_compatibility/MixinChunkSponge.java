package org.valkyrienskies.mixin.sponge_compatibility;

import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;

/**
 * This Mixin MUST load after MixinChunk from SpongeForge, otherwise the @Intrinsic displacement won't work.
 * So priority is set to 1001 to make this mixin load after SpongeForge's MixinChunk.
 */
@Mixin(value = Chunk.class, priority = 1001)
public abstract class MixinChunkSponge {

}
