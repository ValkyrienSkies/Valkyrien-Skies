package ValkyrienWarfareBase;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ValkyrienWarfareBase.Block.BlockPhysicsInfuser;
import ValkyrienWarfareBase.ChunkManagement.DimensionPhysicsChunkManager;
import ValkyrienWarfareBase.PhysicsManagement.DimensionPhysObjectManager;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.Network.PhysWrapperPositionHandler;
import ValkyrienWarfareBase.PhysicsManagement.Network.PhysWrapperPositionMessage;
import ValkyrienWarfareBase.Proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
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

	@SidedProxy(clientSide="ValkyrienWarfareBase.Proxy.ClientProxy", serverSide="ValkyrienWarfareBase.Proxy.ServerProxy")
	public static CommonProxy proxy;

	public static final String MODID = "valkyrienwarfare";
    public static final String MODNAME = "Valkyrien Warfare";
    public static final String MODVER = "0.3b";

    public static File configFile;
    public static Configuration config;
	public static boolean dynamicLighting,spawnParticles;
    public static int shipTickDelay,maxMissedPackets;
    
    public static int threadCount;
    public static boolean multiThreadedPhysics;
    
    public static Block physicsInfuser;

    public static SimpleNetworkWrapper physWrapperNetwork;

    public static DimensionPhysicsChunkManager chunkManager;
    public static DimensionPhysObjectManager physicsManager;
    
    public static ValkyrienWarfareMod instance;
    
    public static int airStateIndex;
	public static double standingTolerance = .3D;
	public static boolean isObsfucated = false;
	
	//NOTE: These only calculate physics, so they are only relevant to the Server end
	public static ExecutorService MultiThreadExecutor;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	proxy.preInit(event);
    	instance = this;
    	registerBlocks(event);
    	registerNetworks(event);
    	runConfiguration(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
    	proxy.init(event);
    	EntityRegistry.registerModEntity(PhysicsWrapperEntity.class,"PhysWrapper",70,this,120,1,false);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
    	proxy.postInit(event);
    	airStateIndex = Block.getStateId(Blocks.AIR.getDefaultState());
    	BlockPhysicsRegistration.registerVanillaBlocksMass();
    	BlockPhysicsRegistration.registerVanillaBlockForces();
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event){
    	MinecraftServer server = event.getServer();
        ServerCommandManager manager = (ServerCommandManager)server.getCommandManager();
//        manager.registerCommand(command)
    }
    
    private void registerNetworks(FMLStateEvent event){
    	physWrapperNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("physChannel");
    	physWrapperNetwork.registerMessage(PhysWrapperPositionHandler.class, PhysWrapperPositionMessage.class, 0, Side.CLIENT);
    }

    private void registerBlocks(FMLStateEvent event){
    	physicsInfuser = new BlockPhysicsInfuser(Material.ROCK).setUnlocalizedName("shipblock").setRegistryName(MODID, "shipblock").setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    	GameRegistry.registerBlock(physicsInfuser);
    }

    private void runConfiguration(FMLPreInitializationEvent event){
    	configFile = event.getSuggestedConfigurationFile();
    	config = new Configuration(configFile);
    	config.load();
    	applyConfig(config);
    	config.save();
    	MultiThreadExecutor = Executors.newFixedThreadPool(threadCount);
    }

    public static void applyConfig(Configuration conf){
        Property dynamiclightProperty = config.get(Configuration.CATEGORY_GENERAL, "DynamicLighting", false);
        Property shipTickDelayProperty = config.get(Configuration.CATEGORY_GENERAL, "Ticks Delay Between Client and Server", 1);
        Property missedPacketsTolerance = config.get(Configuration.CATEGORY_GENERAL, "Missed packets threshold", 1);
        Property spawnParticlesParticle = config.get(Configuration.CATEGORY_GENERAL, "Ships spawn particles", false);
        Property useMultiThreadedPhysics = config.get(Configuration.CATEGORY_GENERAL, "Multi-Threaded Physics", true);
        Property physicsThreads = config.get(Configuration.CATEGORY_GENERAL, "Physics Thread Count", Runtime.getRuntime().availableProcessors());
        
        dynamiclightProperty.setComment("Dynamic Lighting");
        shipTickDelayProperty.setComment("Tick delay between client and server physics; raise if physics look choppy");
        missedPacketsTolerance.setComment("Higher values gaurantee virutally no choppyness, but also comes with a large delay. Only change if you have unstable internet");
        spawnParticlesParticle.setComment("Ships spawn particles");
        useMultiThreadedPhysics.setComment( "Use Multi-Threaded Physics");
        physicsThreads.setComment( "Number of threads to run physics on; RESTART GAME TO APPLY");
        
        dynamicLighting = dynamiclightProperty.getBoolean();
        shipTickDelay = shipTickDelayProperty.getInt()%20;
        maxMissedPackets = missedPacketsTolerance.getInt();
        spawnParticles = spawnParticlesParticle.getBoolean();
        multiThreadedPhysics = useMultiThreadedPhysics.getBoolean();
        threadCount = physicsThreads.getInt();
    }
}