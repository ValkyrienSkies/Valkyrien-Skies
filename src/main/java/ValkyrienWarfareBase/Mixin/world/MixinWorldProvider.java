package ValkyrienWarfareBase.Mixin.world;

import ValkyrienWarfareBase.Fixes.WorldBorderFixWrapper;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(WorldProvider.class)
public abstract class MixinWorldProvider {
    @Overwrite
    public WorldBorder createWorldBorder() {
        WorldBorderFixWrapper wrapper = new WorldBorderFixWrapper(new WorldBorder());
        return wrapper;
    }
}
