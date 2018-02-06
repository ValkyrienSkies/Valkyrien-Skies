/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
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
import net.minecraftforge.fml.relauncher.Side;
import valkyrienwarfare.addon.combat.ValkyrienWarfareCombat;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.opencomputers.ValkyrienWarfareOC;
import valkyrienwarfare.addon.world.ValkyrienWarfareWorld;
import valkyrienwarfare.api.DataTag;
import valkyrienwarfare.api.ValkyrienWarfareHooks;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.api.addons.VWAddon;
import valkyrienwarfare.mixin.MixinLoaderForge;
import valkyrienwarfare.mod.BlockPhysicsRegistration;
import valkyrienwarfare.mod.block.BlockPhysicsInfuser;
import valkyrienwarfare.mod.block.BlockPhysicsInfuserCreative;
import valkyrienwarfare.mod.capability.IAirshipCounterCapability;
import valkyrienwarfare.mod.capability.ImplAirshipCounterCapability;
import valkyrienwarfare.mod.capability.StorageAirshipCounter;
import valkyrienwarfare.mod.command.ModCommands;
import valkyrienwarfare.mod.gui.TabValkyrienWarfare;
import valkyrienwarfare.mod.network.EntityRelativePositionMessage;
import valkyrienwarfare.mod.network.EntityRelativePositionMessageHandler;
import valkyrienwarfare.mod.network.PhysWrapperPositionHandler;
import valkyrienwarfare.mod.network.PhysWrapperPositionMessage;
import valkyrienwarfare.mod.network.PlayerShipRefrenceHandler;
import valkyrienwarfare.mod.network.PlayerShipRefrenceMessage;
import valkyrienwarfare.mod.physmanagement.chunk.DimensionPhysicsChunkManager;
import valkyrienwarfare.mod.proxy.CommonProxy;
import valkyrienwarfare.mod.proxy.ServerProxy;
import valkyrienwarfare.physics.management.DimensionPhysObjectManager;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.util.PhysicsSettings;
import valkyrienwarfare.util.RealMethods;

@Mod(modid = ValkyrienWarfareMod.MODID, name = ValkyrienWarfareMod.MODNAME, version = ValkyrienWarfareMod.MODVER, guiFactory = "valkyrienwarfare.mod.gui.GuiFactoryValkyrienWarfare", updateJSON = "https://raw.githubusercontent.com/BigBastard/Valkyrien-Warfare-Revamped/update.json")
public class ValkyrienWarfareMod {
	public static final ArrayList<Module> addons = new ArrayList<>();
	public static final String MODID = "valkyrienwarfare";
	public static final String MODNAME = "Valkyrien Warfare";
	public static final String MODVER = "0.9_alpha";
	@CapabilityInject(IAirshipCounterCapability.class)
	public static final Capability<IAirshipCounterCapability> airshipCounter = null;
	// NOTE: These only calculate physics, so they are only relevant to the Server
	// end
	public static final ExecutorService MultiThreadExecutor = Executors.newWorkStealingPool();
	public static final ExecutorService PhysicsMasterThread = Executors.newCachedThreadPool();
	@SidedProxy(clientSide = "valkyrienwarfare.mod.proxy.ClientProxy", serverSide = "valkyrienwarfare.mod.proxy.ServerProxy")
	public static CommonProxy proxy;
	public static File configFile;
	public static Configuration config;
	public static boolean dynamicLighting;
	public static boolean multiThreadedPhysics;
	public static boolean doSplitting = false;
	public static boolean doShipCollision = false;
	public static boolean shipsSpawnParticles = false;
	public static Vector gravity = new Vector(0, -9.8D, 0);
	public static int physIter = 10;
	public static double physSpeed = .05D;
	public static Block physicsInfuser;
	public static Block physicsInfuserCreative;
	public static SimpleNetworkWrapper physWrapperNetwork;
	public static DimensionPhysicsChunkManager chunkManager;
	public static DimensionPhysObjectManager physicsManager;
	public static CreativeTabs vwTab = new TabValkyrienWarfare();
	@Instance(MODID)
	public static ValkyrienWarfareMod INSTANCE = new ValkyrienWarfareMod();
	public static int airStateIndex;
	public static double standingTolerance = .42D;
	public static int maxShipSize = 1500000;
	public static double shipUpperLimit = 1000D;
	public static double shipLowerLimit = -30D;
	public static int maxAirships = -1;
	public static boolean highAccuracyCollisions = false;
	public static boolean accurateRain = false;
	public static boolean runAirshipPermissions = false;
	public static double shipmobs_spawnrate = .01D;
	public static Logger VWLogger;
	private static boolean hasAddonRegistrationEnded = false;
	public DataTag tag = null;

	/**
	 * Called by the game when loading the configuration file, also called whenever
	 * the player makes a change in the MOD OPTIONS menu, effectively reloading all
	 * the configuration values
	 *
	 * @param conf
	 */
	public static void applyConfig(Configuration conf) {
		// dynamicLighting = config.get(Configuration.CATEGORY_GENERAL,
		// "DynamicLighting", false).getBoolean();
		// Property spawnParticlesParticle = config.get(Configuration.CATEGORY_GENERAL,
		// "Ships spawn particles", false).getBoolean();
		multiThreadedPhysics = config
				.get(Configuration.CATEGORY_GENERAL, "Multi-Threaded physics", true, "Use Multi-Threaded physics")
				.getBoolean();
		shipUpperLimit = config.get(Configuration.CATEGORY_GENERAL, "Ship Y-Height Maximum", 1000D).getDouble();
		shipLowerLimit = config.get(Configuration.CATEGORY_GENERAL, "Ship Y-Height Minimum", -30D).getDouble();
		maxAirships = config.get(Configuration.CATEGORY_GENERAL, "Max airships per player", -1,
				"Players can't own more than this many airships at once. Set to -1 to disable.").getInt();
		accurateRain = config.get(Configuration.CATEGORY_GENERAL, "Enable accurate rain on ships", false,
				"Debug feature, takes a lot of processing power").getBoolean();
		shipsSpawnParticles = config
				.get(Configuration.CATEGORY_GENERAL, "Enable particle spawns on Ships", true, "Ex. Torch Particles")
				.getBoolean();
		runAirshipPermissions = config.get(Configuration.CATEGORY_GENERAL, "Enable airship permissions", false,
				"Enables the airship permissions system").getBoolean();
		shipmobs_spawnrate = config.get(Configuration.CATEGORY_GENERAL, "The spawn rate for ship mobs", .01D,
				"The spawn rate for ship mobs").getDouble();
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

	/**
	 * Checks to see if a player's airship counter can be changed.
	 *
	 * @param isAdding
	 *            Should be true if you are adding a player, false if removing the
	 *            player.
	 * @param player
	 *            The player to check for
	 * @return
	 */
	public static boolean canChangeAirshipCounter(boolean isAdding, EntityPlayer player) {
		if (isAdding) {
			if (ValkyrienWarfareMod.maxAirships == -1) {
				return true;
			}

			return player.getCapability(ValkyrienWarfareMod.airshipCounter, null)
					.getAirshipCount() < ValkyrienWarfareMod.maxAirships;
		} else {
			return player.getCapability(ValkyrienWarfareMod.airshipCounter, null).getAirshipCount() > 0;
		}
	}

	public static void registerAddon(Module module) {
		if (hasAddonRegistrationEnded) {
			throw new IllegalStateException("Attempting to register addon after FMLConstructionEvent");
		} else {
			System.out.println("[VW Addon System] Registering addon: " + module.getClass().getCanonicalName());
			for (Module registered : addons) {
				if (registered.getClass().getCanonicalName().equals(module.getClass().getCanonicalName())) {
					System.out.println(
							"Addon " + module.getClass().getCanonicalName() + " already registered, skipping...");
					return;
				}
			}
			addons.add(module);
		}
	}

	@EventHandler
	public void fmlConstruct(FMLConstructionEvent event) {
		URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
		ArrayList<String> allAddons = new ArrayList<>();
		final boolean isAddonBugFixed = false;

		if (!isAddonBugFixed) {
			ValkyrienWarfareCombat combatModule = new ValkyrienWarfareCombat();
			ValkyrienWarfareControl controlModule = new ValkyrienWarfareControl();
			ValkyrienWarfareWorld worldModule = new ValkyrienWarfareWorld();
			ValkyrienWarfareOC opencomputersModule = new ValkyrienWarfareOC();
			registerAddon(combatModule);
			registerAddon(controlModule);
			registerAddon(worldModule);
			registerAddon(opencomputersModule);
		}

		if (!MixinLoaderForge.isObfuscatedEnvironment) { // if in dev, read default addons from gradle output folder
			File f = ValkyrienWarfareMod.getWorkingFolder();
			File defaultAddons;
			String[] list = f.list();
			boolean rootDir = false;
			for (String s : list) {
				if (s.endsWith("build.gradle")) {
					rootDir = true;
				}
			}
			if (rootDir) { // assume root directory
				defaultAddons = new File(f.getPath() + File.separatorChar + "src" + File.separatorChar + "main"
						+ File.separatorChar + "resources" + File.separatorChar + "vwAddon_default");
			} else { // assume run/ directory or similar
				defaultAddons = new File(f.getAbsoluteFile().getParentFile().getParent() + File.separatorChar + "src"
						+ File.separatorChar + "main" + File.separatorChar + "resources" + File.separatorChar
						+ "vwAddon_default");
			}
			System.out.println(defaultAddons.getAbsolutePath());
			try {
				InputStream inputStream = new FileInputStream(defaultAddons);
				Scanner scanner = new Scanner(inputStream);
				while (scanner.hasNextLine()) {
					String className = scanner.nextLine().trim();
					allAddons.add(className);
					System.out.println("Found addon " + className);
				}
				scanner.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (URL url : classLoader.getURLs()) {
			try {
				// ZipFile file = new ZipFile(new File(url.toURI()));
				ZipInputStream zis = new ZipInputStream(
						new BufferedInputStream(new FileInputStream(new File(url.getPath()))));
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					if (entry.getName().startsWith("vwAddon_")) {
						try {
							ZipFile file = new ZipFile(new File(url.getPath()));
							InputStream inputStream = file.getInputStream(file.getEntry(entry.getName()));
							Scanner scanner = new Scanner(inputStream);
							while (scanner.hasNextLine()) {
								String className = scanner.nextLine().trim();
								allAddons.add(className);
								System.out.println("Found addon " + className);
							}
							scanner.close();
							inputStream.close();
						} catch (IOException e) {
							// wtf java
						}
						break;
					}
				}
				zis.close();
			} catch (IOException e) {
				// wtf java
			}
		}

		ALLADDONS: for (String className : allAddons) {
			try {
				Class<?> abstractclass = Class.forName(className);
				if (abstractclass.isAnnotationPresent(VWAddon.class)) {
					for (Module registered : addons) {
						if (registered.getClass().getCanonicalName().equals(abstractclass.getCanonicalName())) {
							System.out.println(
									"Addon " + abstractclass.getCanonicalName() + " already registered, skipping...");
							continue ALLADDONS;
						}
					}
					Module module = (Module) abstractclass.newInstance();
					registerAddon(module);
				} else {
					System.out.println("Class " + className + " does not have @VWAddon annonation, not loading");
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				System.out.println("Not loading addon: " + className);
			}
		}
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		hasAddonRegistrationEnded = true;

		proxy.preInit(event);
		registerNetworks(event);
		runConfiguration(event);
		registerCapibilities();
		ValkyrienWarfareHooks.methods = new RealMethods();
		ValkyrienWarfareHooks.isValkyrienWarfareInstalled = true;
		VWLogger = Logger.getLogger("ValkyrienWarfare");

		for (Module addon : addons) {
			addon.doPreInit(event);
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "PhysWrapper"), PhysicsWrapperEntity.class,
				"PhysWrapper", 70, this, 120, 1, false);

		for (Module addon : addons) {
			addon.doInit(event);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
		airStateIndex = Block.getStateId(Blocks.AIR.getDefaultState());
		BlockPhysicsRegistration.registerCustomBlockMasses();
		BlockPhysicsRegistration.registerVanillaBlockForces();
		BlockPhysicsRegistration.registerBlocksToNotPhysicise();

		ForgeChunkManager.setForcedChunkLoadingCallback(INSTANCE, new VWChunkLoadingCallback());
		//// We're stealing these tickets bois!////
		try {
			Field ticketConstraintsField = ForgeChunkManager.class.getDeclaredField("ticketConstraints");
			Field chunkConstraintsField = ForgeChunkManager.class.getDeclaredField("chunkConstraints");

			ticketConstraintsField.setAccessible(true);
			chunkConstraintsField.setAccessible(true);

			Object ticketConstraints = ticketConstraintsField.get(null);
			Object chunkConstraints = chunkConstraintsField.get(null);

			Map<String, Integer> ticketsMap = (Map<String, Integer>) ticketConstraints;
			Map<String, Integer> chunksMap = (Map<String, Integer>) chunkConstraints;

			ticketsMap.put(MODID, Integer.MAX_VALUE);
			chunksMap.put(MODID, Integer.MAX_VALUE);
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("DAMMIT LEX!");
		}

		for (Module addon : addons) {
			addon.doPostInit(event);
		}
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		MinecraftServer server = event.getServer();
		ModCommands.registerCommands(server);
	}

	private void registerNetworks(FMLStateEvent event) {
		physWrapperNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("physChannel");
		physWrapperNetwork.registerMessage(PhysWrapperPositionHandler.class, PhysWrapperPositionMessage.class, 0,
				Side.CLIENT);
		physWrapperNetwork.registerMessage(PlayerShipRefrenceHandler.class, PlayerShipRefrenceMessage.class, 1,
				Side.SERVER);
		physWrapperNetwork.registerMessage(EntityRelativePositionMessageHandler.class,
				EntityRelativePositionMessage.class, 2, Side.CLIENT);
	}

	public void registerBlocks(RegistryEvent.Register<Block> event) {
		physicsInfuser = new BlockPhysicsInfuser(Material.ROCK).setHardness(12f).setUnlocalizedName("shipblock")
				.setRegistryName(MODID, "shipblock").setCreativeTab(vwTab);
		physicsInfuserCreative = new BlockPhysicsInfuserCreative(Material.ROCK).setHardness(12f)
				.setUnlocalizedName("shipblockcreative").setRegistryName(MODID, "shipblockcreative")
				.setCreativeTab(vwTab);

		event.getRegistry().register(physicsInfuser);
		event.getRegistry().register(physicsInfuserCreative);
	}

	public void registerItems(RegistryEvent.Register<Item> event) {
		Module.registerItemBlock(event, physicsInfuser);
		Module.registerItemBlock(event, physicsInfuserCreative);
	}

	public void registerRecipies(RegistryEvent.Register<IRecipe> event) {
		Module.registerRecipe(event, new ItemStack(physicsInfuser), "IEI", "ODO", "IEI", 'E', Items.ENDER_PEARL, 'D',
				Items.DIAMOND, 'O', Item.getItemFromBlock(Blocks.OBSIDIAN), 'I', Items.IRON_INGOT);
	}

	private void runConfiguration(FMLPreInitializationEvent event) {
		configFile = event.getSuggestedConfigurationFile();
		config = new Configuration(configFile);
		config.load();
		applyConfig(config);
		config.save();
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
			tag.setBoolean("doEtheriumLifting", true);
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
		ValkyrienWarfareMod.gravity = new Vector(tag.getDouble("gravityVecX", 0.0), tag.getDouble("gravityVecY", -9.8),
				tag.getDouble("gravityVecZ", 0.0));
		PhysicsSettings.doEtheriumLifting = tag.getBoolean("doEtheriumLifting", true);

		// save the tag in case new fields are added, this way they are saved right away
		tag.save();
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

	public void registerCapibilities() {
		CapabilityManager.INSTANCE.register(IAirshipCounterCapability.class, new StorageAirshipCounter(),
				ImplAirshipCounterCapability.class);
	}

	/**
	 * Checks instance of ServerProxy to avoid calling client code on server side
	 *
	 * @return
	 */
	public boolean isRunningOnClient() {
		return !(proxy instanceof ServerProxy);
	}
}
