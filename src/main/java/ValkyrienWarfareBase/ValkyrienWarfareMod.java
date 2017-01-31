package ValkyrienWarfareBase;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import ValkyrienWarfareBase.API.DataTag;
import ValkyrienWarfareBase.API.ValkyrienWarfareHooks;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Block.BlockPhysicsInfuser;
import ValkyrienWarfareBase.Block.BlockPhysicsInfuserCreative;
import ValkyrienWarfareBase.Capability.IAirshipCounterCapability;
import ValkyrienWarfareBase.Capability.ImplAirshipCounterCapability;
import ValkyrienWarfareBase.Capability.StorageAirshipCounter;
import ValkyrienWarfareBase.ChunkManagement.DimensionPhysicsChunkManager;
import ValkyrienWarfareBase.PhysicsManagement.DimensionPhysObjectManager;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.Network.PhysWrapperPositionHandler;
import ValkyrienWarfareBase.PhysicsManagement.Network.PhysWrapperPositionMessage;
import ValkyrienWarfareBase.Proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = ValkyrienWarfareMod.MODID, name = ValkyrienWarfareMod.MODNAME, version = ValkyrienWarfareMod.MODVER, guiFactory = "ValkyrienWarfareBase.GUI.GuiFactoryValkyrienWarfare", updateJSON = "https://raw.githubusercontent.com/BigBastard/Valkyrien-Warfare-Revamped/update.json")
public class ValkyrienWarfareMod {

	@SidedProxy(clientSide = "ValkyrienWarfareBase.Proxy.ClientProxy", serverSide = "ValkyrienWarfareBase.Proxy.ServerProxy")
	public static CommonProxy proxy;

	public static final String MODID = "valkyrienwarfare";
	public static final String MODNAME = "Valkyrien Warfare";
	public static final String MODVER = "0.87a";

	public static File configFile;
	public static Configuration config;
	public static boolean dynamicLighting, spawnParticles;
	public static int shipTickDelay, maxMissedPackets;

	public static int threadCount;
	public static boolean multiThreadedPhysics;
	public static boolean doSplitting = false;
	public static boolean doShipCollision = false;

	public static Vector gravity = new Vector(0, -9.8D, 0);
	public static int physIter = 10;
	public static double physSpeed = .05D;

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
	public static int maxAirships = -1;

	// NOTE: These only calculate physics, so they are only relevant to the Server end
	public static ExecutorService MultiThreadExecutor;

	public DataTag tag = null;
	public static Logger VWLogger;
	
	@CapabilityInject(IAirshipCounterCapability.class)
	public static final Capability<IAirshipCounterCapability> airshipCounter = null;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
		instance = this;
		registerBlocks(event);
		registerRecipies(event);
		registerNetworks(event);
		runConfiguration(event);
		registerCapibilities();
		ValkyrienWarfareHooks.methods = new RealMethods();
		ValkyrienWarfareHooks.isValkyrienWarfareInstalled = true;
		VWLogger = Logger.getLogger("ValkyrienWarfare");
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
		EntityRegistry.registerModEntity(PhysicsWrapperEntity.class, "PhysWrapper", 70, this, 120, 1, false);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
		airStateIndex = Block.getStateId(Blocks.AIR.getDefaultState());
		BlockPhysicsRegistration.registerCustomBlockMasses();
		BlockPhysicsRegistration.registerVanillaBlockForces();
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		MinecraftServer server = event.getServer();
		ExtraRegistry.registerCommands(server);
	}

	private void registerNetworks(FMLStateEvent event) {
		physWrapperNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("physChannel");
		physWrapperNetwork.registerMessage(PhysWrapperPositionHandler.class, PhysWrapperPositionMessage.class, 0, Side.CLIENT);
	}

	private void registerBlocks(FMLStateEvent event) {
		physicsInfuser = new BlockPhysicsInfuser(Material.ROCK).setHardness(12f).setUnlocalizedName("shipblock").setRegistryName(MODID, "shipblock").setCreativeTab(CreativeTabs.TRANSPORTATION);
		physicsInfuserCreative = new BlockPhysicsInfuserCreative(Material.ROCK).setHardness(12f).setUnlocalizedName("shipblockcreative").setRegistryName(MODID, "shipblockcreative").setCreativeTab(CreativeTabs.TRANSPORTATION);
		;

		GameRegistry.registerBlock(physicsInfuser);
		GameRegistry.registerBlock(physicsInfuserCreative);
	}

	private void registerRecipies(FMLStateEvent event) {
		GameRegistry.addRecipe(new ItemStack(physicsInfuser), new Object[] { "RRR", "RDR", "RRR", 'R', Items.REDSTONE, 'D', Item.getItemFromBlock(Blocks.DIAMOND_BLOCK) });
	}

	private void runConfiguration(FMLPreInitializationEvent event) {
		configFile = event.getSuggestedConfigurationFile();
		config = new Configuration(configFile);
		config.load();
		applyConfig(config);
		config.save();
	}

	public static void applyConfig(Configuration conf) {
		// dynamicLighting = config.get(Configuration.CATEGORY_GENERAL, "DynamicLighting", false).getBoolean();
		shipTickDelay = config.get(Configuration.CATEGORY_GENERAL, "Ticks Delay Between Client and Server", 1, "Tick delay between client and server physics; raise if physics look choppy").getInt() % 20;
		maxMissedPackets = config.get(Configuration.CATEGORY_GENERAL, "Missed packets threshold", 1, "Higher values gaurantee virutally no choppyness, but also comes with a large delay. Only change if you have unstable internet").getInt();
		// Property spawnParticlesParticle = config.get(Configuration.CATEGORY_GENERAL, "Ships spawn particles", false).getBoolean();
		multiThreadedPhysics = config.get(Configuration.CATEGORY_GENERAL, "Multi-Threaded Physics", true, "Use Multi-Threaded Physics").getBoolean();
		threadCount = config.get(Configuration.CATEGORY_GENERAL, "Physics Thread Count", (int) Math.max(1, Runtime.getRuntime().availableProcessors() - 2), "Number of threads to run physics on").getInt();

		doShipCollision = config.get(Configuration.CATEGORY_GENERAL, "Enable Ship Collision", true).getBoolean();

		shipUpperLimit = config.get(Configuration.CATEGORY_GENERAL, "Ship Y-Height Maximum", 1000D).getDouble();
		shipLowerLimit = config.get(Configuration.CATEGORY_GENERAL, "Ship Y-Height Minimum", -30D).getDouble();

		maxAirships = config.get(Configuration.CATEGORY_GENERAL, "Max airships per player", -1, "Players can't own more than this many airships at once. Set to -1 to disable.").getInt();

		if (MultiThreadExecutor != null) {
			MultiThreadExecutor.shutdown();
		}
		if (multiThreadedPhysics) {
			MultiThreadExecutor = Executors.newFixedThreadPool(threadCount);
		}
	}

	@EventHandler
	public void onServerStarted(FMLServerStartedEvent event) {
		this.loadConfig();
	}

	@EventHandler
	public void onServerStopping(FMLServerStoppingEvent event) {
		this.saveConfig();
	}

	public void loadConfig() {
		File file = new File(ValkyrienWarfareMod.getWorkingFolder(), "/valkyrienwarfaresettings.dat");

		if (!file.exists()) {
			tag = new DataTag(file);
			tag.setBoolean("doGravity", true);
			tag.setBoolean("doPhysicsBlocks", true);
			tag.setBoolean("doBalloons", true);
			tag.setBoolean("doAirshipRotation", true);
			tag.setBoolean("doAirshipMovement", true);
			tag.setBoolean("doSplitting", false);
			tag.setInteger("maxShipSize", 15000);
			tag.setDouble("gravityVecX", 0);
			tag.setDouble("gravityVecY", -9.8);
			tag.setDouble("gravityVecZ", 0);
			tag.setInteger("physicsIterations", 10);
			tag.setDouble("physicsSpeed", 0.05);
			tag.save();
		} else {
			tag = new DataTag(file);
		}

		PhysicsSettings.doGravity = tag.getBoolean("doGravity", true);
		PhysicsSettings.doPhysicsBlocks = tag.getBoolean("doPhysicsBlocks", true);
		PhysicsSettings.doBalloons = tag.getBoolean("doBalloons", true);
		PhysicsSettings.doAirshipRotation = tag.getBoolean("doAirshipRotation", true);
		PhysicsSettings.doAirshipMovement = tag.getBoolean("doAirshipMovement", true);
		ValkyrienWarfareMod.doSplitting = tag.getBoolean("doSplitting", false);
		ValkyrienWarfareMod.maxShipSize = tag.getInteger("maxShipSize", 15000);
		ValkyrienWarfareMod.physIter = tag.getInteger("physicsIterations", 8);
		ValkyrienWarfareMod.physSpeed = tag.getDouble("physicsSpeed", 0.05);
		ValkyrienWarfareMod.gravity = new Vector(tag.getDouble("gravityVecX", 0.0), tag.getDouble("gravityVecY", -9.8), tag.getDouble("gravityVecZ", 0.0));
	}

	public void saveConfig() {
		tag.setBoolean("doGravity", PhysicsSettings.doGravity);
		tag.setBoolean("doPhysicsBlocks", PhysicsSettings.doPhysicsBlocks);
		tag.setBoolean("doBalloons", PhysicsSettings.doBalloons);
		tag.setBoolean("doAirshipRotation", PhysicsSettings.doAirshipRotation);
		tag.setBoolean("doAirshipMovement", PhysicsSettings.doAirshipMovement);
		tag.setBoolean("doSplitting", ValkyrienWarfareMod.doSplitting);
		tag.setInteger("maxShipSize", ValkyrienWarfareMod.maxShipSize);
		tag.setDouble("gravityVecX", ValkyrienWarfareMod.gravity.X);
		tag.setDouble("gravityVecY", ValkyrienWarfareMod.gravity.Y);
		tag.setDouble("gravityVecZ", ValkyrienWarfareMod.gravity.Z);
		tag.setInteger("physicsIterations", ValkyrienWarfareMod.physIter);
		tag.setDouble("physicsSpeed", ValkyrienWarfareMod.physSpeed);
		tag.save();
	}
	
	public static File getWorkingFolder() {
		File toBeReturned;
		try {
			if (FMLCommonHandler.instance().getSide().isClient()) {
				toBeReturned = Minecraft.getMinecraft().mcDataDir;
			} else {
				toBeReturned = FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
			}
			return toBeReturned;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void registerCapibilities()	{
		CapabilityManager.INSTANCE.register(IAirshipCounterCapability.class, new StorageAirshipCounter(), ImplAirshipCounterCapability.class);
	}
	
	/**
	 * Checks to see if a player's airship counter can be changed.
	 * @param isAdding Should be true if you are adding a player, false if removing the player.
	 * @param player The player to check for
	 * @return
	 */
	public static boolean canChangeAirshipCounter(boolean isAdding, EntityPlayer player)	{
		if (isAdding)	{
			if (ValkyrienWarfareMod.maxAirships == -1)	{
				return true;
			}
			
			if (player.getCapability(ValkyrienWarfareMod.airshipCounter, null).getAirshipCount() >= ValkyrienWarfareMod.maxAirships)	{
				return false;
			} else {
				return true;
			}
		} else {
			if (player.getCapability(ValkyrienWarfareMod.airshipCounter, null).getAirshipCount() > 0)	{
				return true;
			} else {
				return false;
			}
		}
	}
}