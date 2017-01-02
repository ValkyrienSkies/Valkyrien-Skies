package ValkyrienWarfareBase;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ValkyrienWarfareBase.API.PhysicsEntityHooks;
import ValkyrienWarfareBase.Block.BlockPhysicsInfuser;
import ValkyrienWarfareBase.Block.BlockPhysicsInfuserCreative;
import ValkyrienWarfareBase.ChunkManagement.DimensionPhysicsChunkManager;
import ValkyrienWarfareBase.PhysicsManagement.DimensionPhysObjectManager;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.Network.PhysWrapperPositionHandler;
import ValkyrienWarfareBase.PhysicsManagement.Network.PhysWrapperPositionMessage;
import ValkyrienWarfareBase.Proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
    public static final String MODVER = "0.86c";

    public static File configFile;
    public static Configuration config;
	public static boolean dynamicLighting,spawnParticles;
    public static int shipTickDelay,maxMissedPackets;
    
    public static int threadCount;
    public static boolean multiThreadedPhysics;
    public static boolean doSplitting = false;
    public static boolean doShipCollision = false;
    
    public static Block physicsInfuser;
    public static Block physicsInfuserCreative;

    public static SimpleNetworkWrapper physWrapperNetwork;

    public static DimensionPhysicsChunkManager chunkManager;
    public static DimensionPhysObjectManager physicsManager;
    
    public static ValkyrienWarfareMod instance;
    
    public static int airStateIndex;
	public static double standingTolerance = .42D;
	public static int maxShipSize = 15000;
	public static double shipUpperLimit = 1000D;
	public static double shipLowerLimit = -30D;
	
	//NOTE: These only calculate physics, so they are only relevant to the Server end
	public static ExecutorService MultiThreadExecutor;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	proxy.preInit(event);
    	instance = this;
    	registerBlocks(event);
    	registerRecipies(event);
    	registerNetworks(event);
    	runConfiguration(event);
    	PhysicsEntityHooks.methods = new RealMethods();
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
    	BlockPhysicsRegistration.registerCustomBlockMasses();
    	BlockPhysicsRegistration.registerVanillaBlockForces();
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event){
    	MinecraftServer server = event.getServer();
        ExtraRegistry.registerCommands(server);
    }
    
    private void registerNetworks(FMLStateEvent event){
    	physWrapperNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("physChannel");
    	physWrapperNetwork.registerMessage(PhysWrapperPositionHandler.class, PhysWrapperPositionMessage.class, 0, Side.CLIENT);
    }

    private void registerBlocks(FMLStateEvent event){
    	physicsInfuser = new BlockPhysicsInfuser(Material.ROCK).setHardness(12f).setUnlocalizedName("shipblock").setRegistryName(MODID, "shipblock").setCreativeTab(CreativeTabs.TRANSPORTATION);
    	physicsInfuserCreative = new BlockPhysicsInfuserCreative(Material.ROCK).setHardness(12f).setUnlocalizedName("shipblockcreative").setRegistryName(MODID, "shipblockcreative").setCreativeTab(CreativeTabs.TRANSPORTATION);;
    	
    	GameRegistry.registerBlock(physicsInfuser);
    	GameRegistry.registerBlock(physicsInfuserCreative);
    }
    
    private void registerRecipies(FMLStateEvent event){
    	GameRegistry.addRecipe(new ItemStack(physicsInfuser), new Object[] {"RRR", "RDR","RRR",'R',Items.REDSTONE, 'D', Item.getItemFromBlock(Blocks.DIAMOND_BLOCK)});
    }

    private void runConfiguration(FMLPreInitializationEvent event){
    	configFile = event.getSuggestedConfigurationFile();
    	config = new Configuration(configFile);
    	config.load();
    	applyConfig(config);
    	config.save();
    }

    public static void applyConfig(Configuration conf){
//        Property dynamiclightProperty = config.get(Configuration.CATEGORY_GENERAL, "DynamicLighting", false);
        Property shipTickDelayProperty = config.get(Configuration.CATEGORY_GENERAL, "Ticks Delay Between Client and Server", 1);
        Property missedPacketsTolerance = config.get(Configuration.CATEGORY_GENERAL, "Missed packets threshold", 1);
//        Property spawnParticlesParticle = config.get(Configuration.CATEGORY_GENERAL, "Ships spawn particles", false);
        Property useMultiThreadedPhysics = config.get(Configuration.CATEGORY_GENERAL, "Multi-Threaded Physics", false);
        Property physicsThreads = config.get(Configuration.CATEGORY_GENERAL, "Physics Thread Count", (int)Math.max(1, Runtime.getRuntime().availableProcessors()-2));
        
        Property doShipCollisionProperty = config.get(Configuration.CATEGORY_GENERAL, "Enable Ship Collision", false);
        
        Property shipUpperHeightLimit = config.get(Configuration.CATEGORY_GENERAL, "Ship Y-Height Maximum", 1000D);
        Property shipLowerHeightLimit = config.get(Configuration.CATEGORY_GENERAL, "Ship Y-Height Minimum", -30D);
        
//        dynamiclightProperty.setComment("Dynamic Lighting");
        shipTickDelayProperty.setComment("Tick delay between client and server physics; raise if physics look choppy");
        missedPacketsTolerance.setComment("Higher values gaurantee virutally no choppyness, but also comes with a large delay. Only change if you have unstable internet");
//        spawnParticlesParticle.setComment("Ships spawn particles");
        useMultiThreadedPhysics.setComment( "Use Multi-Threaded Physics");
        physicsThreads.setComment( "Number of threads to run physics on;");
        
//        dynamicLighting = dynamiclightProperty.getBoolean();
        shipTickDelay = shipTickDelayProperty.getInt()%20;
        maxMissedPackets = missedPacketsTolerance.getInt();
//        spawnParticles = spawnParticlesParticle.getBoolean();
        multiThreadedPhysics = useMultiThreadedPhysics.getBoolean();
        threadCount = physicsThreads.getInt();
        
        doShipCollision = doShipCollisionProperty.getBoolean();
        
        shipUpperLimit = shipUpperHeightLimit.getDouble();
        shipLowerLimit = shipLowerHeightLimit.getDouble();
        
        if(MultiThreadExecutor!=null){
        	MultiThreadExecutor.shutdown();
        }
        if(multiThreadedPhysics){
        	MultiThreadExecutor = Executors.newFixedThreadPool(threadCount);
        }
    }
}