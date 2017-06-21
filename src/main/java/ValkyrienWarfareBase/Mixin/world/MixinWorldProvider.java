package ValkyrienWarfareBase.Mixin.world;

import ValkyrienWarfareBase.Fixes.WorldBorderFixWrapper;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(WorldProvider.class)
public class MixinWorldProvider {
    @Overwrite
    public WorldBorder createWorldBorder() {
        //TODO: add that class
        WorldBorderFixWrapper wrapper = new WorldBorderFixWrapper(new WorldBorder());
        return wrapper;
    }
}
