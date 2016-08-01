package ValkyrienWarfareCombat;

import ValkyrienWarfareCombat.Entity.EntityCannonBall;
import ValkyrienWarfareCombat.Entity.EntityCannonBasic;
import ValkyrienWarfareCombat.Item.ItemBasicCannon;
import ValkyrienWarfareCombat.Proxy.CommonProxyCombat;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid=ValkyrienWarfareCombatMod.MODID, name=ValkyrienWarfareCombatMod.MODNAME, version=ValkyrienWarfareCombatMod.MODVER)
public class ValkyrienWarfareCombatMod {
	
	@SidedProxy(clientSide="ValkyrienWarfareCombat.Proxy.ClientProxyCombat", serverSide="ValkyrienWarfareCombat.Proxy.CommonProxyCombat")
	public static CommonProxyCombat proxy;
	
	public static final String MODID = "valkyrienwarfarecombat";
    public static final String MODNAME = "Valkyrien Warfare Combat";
    public static final String MODVER = "0.0";
    
	public static ValkyrienWarfareCombatMod instance;
    
	public Item basicCannonSpawner;
	
    public static Block fakeCannonBlock;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	instance = this;
    	registerBlocks(event);
    	registerItems(event);
    	registerEntities(event);
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
    
    private void registerItems(FMLStateEvent event){
    	basicCannonSpawner = new ItemBasicCannon().setUnlocalizedName("basicCannonSpawner").setRegistryName(MODID, "basicCannonSpawner").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(4);
    	GameRegistry.registerItem(basicCannonSpawner);
    }
    
    private void registerEntities(FMLStateEvent event){
    	EntityRegistry.registerModEntity(EntityCannonBasic.class,"EntityCannonBasic",71,this,120,1,false);
    	EntityRegistry.registerModEntity(EntityCannonBall.class, "EntityCannonBall", 72, this, 120, 1, true);
    }
    
    private void registerBlocks(FMLStateEvent event){
    	fakeCannonBlock = new FakeCannonBlock(Material.IRON).setHardness(5f).setUnlocalizedName("fakeCannonBlock").setRegistryName(MODID, "fakeCannonBlock").setCreativeTab(CreativeTabs.REDSTONE);
    	
    	GameRegistry.registerBlock(fakeCannonBlock);
    }

}
