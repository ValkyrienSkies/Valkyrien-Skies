package valkyrienwarfare.mixin;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

public class MixinLoaderForge implements IFMLLoadingPlugin {
	
	public static boolean isObfuscatedEnvironment = false;
	
	public MixinLoaderForge() {
		System.out.println("\n\n\nValkyrien Warfare mixin init\n\n");
		MixinBootstrap.init();
		Mixins.addConfiguration("mixins.valkyrienwarfare.json");
		try {
			ClientBrandRetriever.class.getDeclaredMethod("getClientModName", null);
			MixinEnvironment.getDefaultEnvironment().setObfuscationContext("mcp");
		} catch (NoSuchMethodException e) {
			//this is not mcp!
			MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
		}
		System.out.println(MixinEnvironment.getDefaultEnvironment().getObfuscationContext());
	}
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}
	
	@Override
	public String getModContainerClass() {
		return null;
	}
	
	@Override
	public String getSetupClass() {
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data) {
		isObfuscatedEnvironment = (boolean) (Boolean) data.get("runtimeDeobfuscationEnabled");
		
	}
	
	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
