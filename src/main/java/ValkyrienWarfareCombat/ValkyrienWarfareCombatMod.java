package ValkyrienWarfareCombat;

import ValkyrienWarfareCombat.Proxy.CommonProxyCombat;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid=ValkyrienWarfareCombatMod.MODID, name=ValkyrienWarfareCombatMod.MODNAME, version=ValkyrienWarfareCombatMod.MODVER)
public class ValkyrienWarfareCombatMod {
	
	@SidedProxy(clientSide="ValkyrienWarfareCombat.Proxy.ClientProxyCombat", serverSide="ValkyrienWarfareCombat.Proxy.CommonProxyCombat")
	public static CommonProxyCombat proxy;
	
	public static final String MODID = "valkyrienwarfarecombat";
    public static final String MODNAME = "Valkyrien Warfare Combat";
    public static final String MODVER = "0.0";
    
    public Block fakeCannonBlock;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	registerBlocks(event);
    	proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
    	proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
    	proxy.postInit(event);
    }
    
    private void registerBlocks(FMLStateEvent event){
    	fakeCannonBlock = new FakeCannonBlock(Material.IRON).setHardness(5f).setUnlocalizedName("fakeCannonBlock").setRegistryName(MODID, "fakeCannonBlock").setCreativeTab(CreativeTabs.REDSTONE);
    	
    	GameRegistry.registerBlock(fakeCannonBlock);
    }

}
