package ValkyrienWarfareBase;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid=ValkyrienWarfareMod.MODID, name=ValkyrienWarfareMod.MODNAME, version=ValkyrienWarfareMod.MODVER, guiFactory = "ValkyrienWarfareBase.GUI.GuiFactoryValkyrienWarfare")
public class ValkyrienWarfareMod{

	public static final String MODID = "valkyrienwarfare";
    public static final String MODNAME = "Valkyrien Warfare";
    public static final String MODVER = "0.0";
    public static File configFile;
    public static Configuration config;
	public static boolean dynamicLighting;
    public static boolean spawnParticles;
    public static int shipTickDelay,maxMissedPackets;

    public static ValkyrienWarfareMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	instance = this;
    	runConfiguration(event);
    	System.out.println("test");
    }

    @EventHandler
    public void init(FMLInitializationEvent event){

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){

    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event){
    	MinecraftServer server = event.getServer();
        ServerCommandManager manager = (ServerCommandManager)server.getCommandManager();
//        manager.registerCommand(command)
    }

    public void runConfiguration(FMLPreInitializationEvent event){
    	configFile = event.getSuggestedConfigurationFile();
    	config = new Configuration(configFile);
    	config.load();
    	applyConfig(config);
    	config.save();
    }

    public static void applyConfig(Configuration conf){
        Property dynamiclightProperty = config.get(Configuration.CATEGORY_GENERAL, "DynamicLighting", false);
        Property shipTickDelayProperty = config.get(Configuration.CATEGORY_GENERAL, "Ticks Delay Between Client and Server", 1);
        Property missedPacketsTolerance = config.get(Configuration.CATEGORY_GENERAL, "Missed packets threshold", 1);
        Property spawnParticlesParticle = config.get(Configuration.CATEGORY_GENERAL, "Ships spawn particles", false);
        
        dynamiclightProperty.setComment("Dynamic Lighting");
        shipTickDelayProperty.setComment("Tick delay between client and server physics; raise if physics loop choppy");
        missedPacketsTolerance.setComment("Higher values gaurantee virutally no choppyness, but also comes with a large delay. Only change if you have unstable internet");
        spawnParticlesParticle.setComment("Ships spawn particles");
        
        dynamicLighting = dynamiclightProperty.getBoolean();
        shipTickDelay = shipTickDelayProperty.getInt()%20;
        maxMissedPackets = missedPacketsTolerance.getInt();
        spawnParticles = spawnParticlesParticle.getBoolean();
    }
}