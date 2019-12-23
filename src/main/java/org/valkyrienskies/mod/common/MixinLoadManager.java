package org.valkyrienskies.mod.common;

import java.util.List;
import java.util.Set;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinLoadManager implements IMixinConfigPlugin {

    private static boolean isSpongeEnabled;

    /**
     * @return the isSpongeEnabled
     */
    public static boolean isSpongeEnabled() {
        return isSpongeEnabled;
    }

    @Override
    public void onLoad(String mixinPackage) {
        isSpongeEnabled = isSpongeEnabledSlow();
        if (isSpongeEnabled()) {
            FMLLog.bigWarning(
                "Valkyrien Skies has detected SpongeForge!");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!isSpongeEnabled()) {
            if (mixinClassName.contains("spongepowered")) {
                FMLLog
                    .bigWarning("Not applying" + mixinClassName + " because Sponge isn't loaded!");
                return false;
            }
        }

        return true;
    }

    private boolean isSpongeEnabledSlow() {
        try {
            if (Class.forName("org.spongepowered.common.mixin.core.world.MixinExplosion") != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            // nobody cares!
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName,
        IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName,
        IMixinInfo mixinInfo) {
    }

}
