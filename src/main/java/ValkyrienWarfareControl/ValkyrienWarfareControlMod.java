package ValkyrienWarfareControl;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.Block.AirShipEngine;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid=ValkyrienWarfareControlMod.MODID, name=ValkyrienWarfareControlMod.MODNAME, version=ValkyrienWarfareControlMod.MODVER)
public class ValkyrienWarfareControlMod {
	
	public static final String MODID = "valkyrienwarfarecontrol";
    public static final String MODNAME = "Valkyrien Warfare Control";
    public static final String MODVER = "0.1";
    
    public Block airshipEngine;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	registerBlocks(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
    	
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
    	
    }
    
    private void registerBlocks(FMLStateEvent event){
    	airshipEngine = new AirShipEngine(Material.ROCK).setUnlocalizedName("airshipengine").setRegistryName(ValkyrienWarfareMod.MODID, "airshipengine").setCreativeTab(CreativeTabs.REDSTONE);
    	GameRegistry.registerBlock(airshipEngine);
    }

    
}
