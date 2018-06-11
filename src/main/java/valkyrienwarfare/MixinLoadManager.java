/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare;

import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinLoadManager implements IMixinConfigPlugin {

    private static boolean isSpongeEnabled;

    @Override
    public void onLoad(String mixinPackage) {
    	isSpongeEnabled = isSpongeEnabledSlow();
        if (isSpongeEnabled()) {
            FMLLog.bigWarning(
                    "SpongeForge has been detected. This will cause problems with Valkyrien Warfare, no doubt.");
        } else {
            FMLLog.info("Sponge ain't here! Everything is good and also nice");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (isSpongeEnabled()) {
            if (mixinClassName.startsWith("valkyrienwarfare.mixin.world.MixinExplosion")) {
                FMLLog.bigWarning(
                        "Not loading valkyrienwarfare.mixin.world.MixinExplosion because SpongeForge is enabled!");
                return false;
            }
        } else {
            if (mixinClassName.contains("spongepowered")) {
                FMLLog.bigWarning("Not applying" + mixinClassName + " because Sponge isn't enabled!");
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
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

	/**
	 * @return the isSpongeEnabled
	 */
	public static boolean isSpongeEnabled() {
		return isSpongeEnabled;
	}

}
