package ValkyrienWarfareBase.CoreMod;

import java.util.Map;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@Name("ValkyrienWarfareBase CoreMod")
@IFMLLoadingPlugin.SortingIndex(0)
@TransformerExclusions({"ValkyrienWarfareBase"})
public class ValkyrienWarfarePlugin implements IFMLLoadingPlugin{

	public static Boolean isObfuscatedEnvironment = null;
	public static final String PathClient = "ValkyrienWarfareBase/CoreMod/CallRunnerClient";
	public static final String PathCommon = "ValkyrienWarfareBase/CoreMod/CallRunner";
	
    @Override
    public String[] getASMTransformerClass() {
    	return new String[] {"ValkyrienWarfareBase.CoreMod.ValkyrienWarfareTransformer", 
    			"ValkyrienWarfareBase.CoreMod.ValkyrienWarfareTransformerHack"};
    }

    @Override
    public String getModContainerClass(){
    	return null;
    }

    @Override
    public String getSetupClass(){
    	return null;
    }

    @Override
    public void injectData(Map<String, Object> data){
    	isObfuscatedEnvironment = (Boolean)data.get( "runtimeDeobfuscationEnabled" );
    	ValkyrienWarfareMod.isObsfucated = isObfuscatedEnvironment;
    }

    @Override
    public String getAccessTransformerClass(){
    	return "ValkyrienWarfareBase.CoreMod.ValkyrienWarfareAccessTransformer";
    }

}