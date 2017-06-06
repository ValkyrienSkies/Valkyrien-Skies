package ValkyrienWarfareBase.Mixin;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

public class MixinLoaderForge implements IFMLLoadingPlugin {

    public MixinLoaderForge() {
        System.out.println("\n\n\nValkyrien Warfare Mixin init\n\n\n");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.valkyrienwarfare.json");
        if (FMLLaunchHandler.side() == Side.CLIENT) {
            System.out.println("Client side mixins coming right along!!!");
            Mixins.addConfiguration("mixins.valkyrienwarfare.client.json"); //now load in client mixins too
        } else {
            System.out.println("We're running on a server side, don't bother loading client mixins");
        }
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
