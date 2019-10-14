/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Warfare team
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

package valkyrienwarfare.mod.common;

import com.esotericsoftware.kryo.Kryo;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import de.javakaffee.kryoserializers.UUIDSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.opencomputers.ValkyrienWarfareOC;
import valkyrienwarfare.addon.world.ValkyrienWarfareWorld;
import valkyrienwarfare.api.IPhysicsEntityManager;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.api.addons.VWAddon;
import valkyrienwarfare.mixin.MixinLoaderForge;
import valkyrienwarfare.mod.client.gui.TabValkyrienWarfare;
import valkyrienwarfare.mod.common.block.BlockPhysicsInfuser;
import valkyrienwarfare.mod.common.block.BlockPhysicsInfuserCreative;
import valkyrienwarfare.mod.common.block.BlockPhysicsInfuserDummy;
import valkyrienwarfare.mod.common.command.framework.VWModCommandRegistry;
import valkyrienwarfare.mod.common.config.VWConfig;
import valkyrienwarfare.mod.common.item.ItemPhysicsCore;
import valkyrienwarfare.mod.common.network.*;
import valkyrienwarfare.mod.common.physics.management.DimensionPhysObjectManager;
import valkyrienwarfare.mod.common.physmanagement.VW_APIPhysicsEntityManager;
import valkyrienwarfare.mod.common.physmanagement.chunk.*;
import valkyrienwarfare.mod.common.physmanagement.interaction.ShipData;
import valkyrienwarfare.mod.common.physmanagement.interaction.ShipPositionData;
import valkyrienwarfare.mod.common.tileentity.TileEntityPhysicsInfuser;
import valkyrienwarfare.mod.proxy.CommonProxy;
import valkyrienwarfare.mod.proxy.ServerProxy;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@Mod(
        modid = ValkyrienWarfareMod.MOD_ID,
        name = ValkyrienWarfareMod.MOD_NAME,
        version = ValkyrienWarfareMod.MOD_VERSION,
        updateJSON = "https://raw.githubusercontent.com/ValkyrienWarfare/Valkyrien-Warfare-Revamped/master/update.json",
        certificateFingerprint = ValkyrienWarfareMod.MOD_FINGERPRINT
)
public class ValkyrienWarfareMod {
    public static final List<Module> addons = new ArrayList<Module>();
    public static final String MOD_ID = "valkyrienwarfare";
    public static final String MOD_NAME = "Valkyrien Warfare";
    public static final String MOD_VERSION = "0.9.2";
    public static final String MOD_FINGERPRINT = "b308676914a5e7d99459c1d2fb298744387899a7";
    public static final int VW_ENTITY_LOAD_DISTANCE = 128;
    @CapabilityInject(IVWWorldDataCapability.class)
    public static final Capability<IVWWorldDataCapability> vwWorldData = null;
    public static final DimensionPhysicsChunkManager VW_CHUNK_MANAGER = new DimensionPhysicsChunkManager();
    public static final DimensionPhysObjectManager VW_PHYSICS_MANAGER = new DimensionPhysObjectManager();
    // This service is directly responsible for running collision tasks.
    public static ExecutorService PHYSICS_THREADS_EXECUTOR = null;
    @SidedProxy(clientSide = "valkyrienwarfare.mod.proxy.ClientProxy", serverSide = "valkyrienwarfare.mod.proxy.ServerProxy")
    public static CommonProxy proxy;
    public Block physicsInfuser;
    public Block physicsInfuserCreative;
    public Block physicsInfuserDummy;
    public Item physicsCore;
    public static SimpleNetworkWrapper physWrapperNetwork;
    public static CreativeTabs vwTab = new TabValkyrienWarfare();
    @Instance(MOD_ID)
    public static ValkyrienWarfareMod INSTANCE = new ValkyrienWarfareMod();
    public static int airStateIndex;
    public static double standingTolerance = .42D;
    public static Logger VW_LOGGER;
    private static boolean hasAddonRegistrationEnded = false;
    private CompletableFuture<Kryo> kryoInstance;

    private static File getWorkingFolder() {
        File toBeReturned;

        if (FMLCommonHandler.instance().getSide().isClient()) {
            toBeReturned = Minecraft.getMinecraft().gameDir;
        } else {
            toBeReturned = FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
        }

        return toBeReturned;
    }

    private static void registerAddon(Module module) {
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

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        if (MixinLoaderForge.isObfuscatedEnvironment) {
            FMLLog.bigWarning("Valkyrien Warfare JAR fingerprint corrupted, which means this copy of the mod may have come from unofficial sources. Download the mod from CurseForge: https://minecraft.curseforge.com/projects/valkyrien-warfare");
        }
    }

    @EventHandler
    public void fmlConstruct(FMLConstructionEvent event) {
        runConfiguration();
        URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
        ArrayList<String> allAddons = new ArrayList<>();
        final boolean isAddonBugFixed = false;

        if (!isAddonBugFixed) {
            // ValkyrienWarfareCombat combatModule = new ValkyrienWarfareCombat();
            ValkyrienWarfareControl controlModule = new ValkyrienWarfareControl();
            ValkyrienWarfareWorld worldModule = new ValkyrienWarfareWorld();
            ValkyrienWarfareOC opencomputersModule = new ValkyrienWarfareOC();
            // registerAddon(combatModule);
            registerAddon(controlModule);
            registerAddon(worldModule);
            registerAddon(opencomputersModule);
        }

        if (!MixinLoaderForge.isObfuscatedEnvironment) { // if in dev, read default addons from gradle output folder
            File f = ValkyrienWarfareMod.getWorkingFolder();
            File defaultAddons;
            String[] list = Objects.requireNonNull(f.list());
            boolean rootDir = Arrays.stream(list).anyMatch(s -> s.endsWith("build.gradle"));

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

        ALL_ADDONS:
        for (String className : allAddons) {
            try {
                Class<?> abstractclass = Class.forName(className);
                if (abstractclass.isAnnotationPresent(VWAddon.class)) {
                    for (Module registered : addons) {
                        if (registered.getClass().getCanonicalName().equals(abstractclass.getCanonicalName())) {
                            System.out.println(
                                    "Addon " + abstractclass.getCanonicalName() + " already registered, skipping...");
                            continue ALL_ADDONS;
                        }
                    }
                    Module module = (Module) abstractclass.newInstance();
                    // registerAddon(module);
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

        serializationInitAsync();
        proxy.preInit(event);
        registerNetworks(event);
        ValkyrienWarfareMod.PHYSICS_THREADS_EXECUTOR = Executors.newFixedThreadPool(VWConfig.threadCount);
        registerCapabilities();
        VW_LOGGER = Logger.getLogger("ValkyrienWarfare");

        addons.forEach(m -> m.doPreInit(event));

        // Initialize the VW API here:
        try {
            Field instanceField = IPhysicsEntityManager.class.getDeclaredField("INSTANCE");
            // Make the field accessible
            instanceField.setAccessible(true);
            // Remove the final modifier
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(instanceField, instanceField.getModifiers() & ~Modifier.FINAL);
            // Finally set the new value of the field.
            instanceField.set(null, new VW_APIPhysicsEntityManager());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("FAILED TO INITIALIZE VW API!");
        }
        // Initialize VW API end.
        /*
        try {
            Field chunkCache = ForgeChunkManager.class.getDeclaredField("dormantChunkCacheSize");
            chunkCache.setAccessible(true);
            chunkCache.set(null, new Integer(1000));
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // Print out a message of core count, we want this to know what AnvilNode is giving us.
        System.out.println("Valyrien Warfare Initilization:");
        System.out.println("We are running on " + Runtime.getRuntime().availableProcessors() + " threads; 4 or more is recommended!");
        proxy.init(event);

        addons.forEach(m -> m.doInit(event));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        airStateIndex = Block.getStateId(Blocks.AIR.getDefaultState());

        addons.forEach(m -> m.doPostInit(event));
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        VWModCommandRegistry.registerCommands(server);
    }

    private void registerNetworks(FMLStateEvent event) {
        physWrapperNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("physChannel");
        physWrapperNetwork.registerMessage(PhysWrapperPositionHandler.class, PhysWrapperPositionMessage.class, 0,
                Side.CLIENT);
        physWrapperNetwork.registerMessage(SubspacedEntityRecordHandler.class, SubspacedEntityRecordMessage.class, 1, Side.CLIENT);
        physWrapperNetwork.registerMessage(SubspacedEntityRecordHandler.class, SubspacedEntityRecordMessage.class, 2, Side.SERVER);
        physWrapperNetwork.registerMessage(VWGuiButtonHandler.class, VWGuiButtonMessage.class, 3, Side.SERVER);
    }

    void registerBlocks(RegistryEvent.Register<Block> event) {
        physicsInfuser = new BlockPhysicsInfuser(Material.ROCK).setHardness(8f)
                .setTranslationKey("shipblock")
                .setRegistryName(MOD_ID, "shipblock")
                .setCreativeTab(vwTab);
        physicsInfuserCreative = new BlockPhysicsInfuserCreative(Material.ROCK).setHardness(12f)
                .setTranslationKey("shipblockcreative")
                .setRegistryName(MOD_ID, "shipblockcreative")
                .setCreativeTab(vwTab);
        // Do not put the dummy block into the creative tab
        physicsInfuserDummy = new BlockPhysicsInfuserDummy(Material.ROCK).setHardness(12f)
                .setTranslationKey("physics_infuser_dummy")
                .setRegistryName(MOD_ID, "physics_infuser_dummy");

        event.getRegistry().register(physicsInfuser);
        event.getRegistry().register(physicsInfuserCreative);
        event.getRegistry()
                .register(physicsInfuserDummy);

        registerTileEntities();
    }

    private void serializationInitAsync() {
        kryoInstance = CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();

            Kryo kryo = new Kryo();
            kryo.register(ConcurrentIndexedCollection.class);
            kryo.register(ShipData.class);
            kryo.register(ShipPositionData.class);
            kryo.register(VWChunkClaim.class);
            kryo.register(HashSet.class);
            kryo.register(UUID.class, new UUIDSerializer());
            UnmodifiableCollectionsSerializer.registerSerializers(kryo);

            kryo.setRegistrationRequired(false);

            System.out.println("Kryo initialization: " + (System.currentTimeMillis() - start) + "ms");

            return kryo;
        });
    }

    public Kryo getKryo() {
        try {
            return kryoInstance.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void registerItems(RegistryEvent.Register<Item> event) {
        Module.registerItemBlock(event, physicsInfuser);
        Module.registerItemBlock(event, physicsInfuserCreative);

        this.physicsCore = new ItemPhysicsCore().setTranslationKey("vw_phys_core")
                .setRegistryName(MOD_ID, "vw_phys_core")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        event.getRegistry()
                .register(this.physicsCore);
    }

    void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        Module.registerRecipe(event, "recipe_physics_infuser", new ItemStack(physicsInfuser), "IEI", "ODO", "IEI", 'E', Items.ENDER_PEARL, 'D',
                Items.DIAMOND, 'O', Item.getItemFromBlock(Blocks.OBSIDIAN), 'I', Items.IRON_INGOT);
    }

	private void runConfiguration() {
        VWConfig.sync();
	}

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) { }

    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) { }

    private void registerCapabilities() {
        CapabilityManager.INSTANCE.register(IVWWorldDataCapability.class, new StorageVWWorldData(),
                ImplVWWorldDataCapability.class);
    }

    /**
     * Checks instance of ServerProxy to avoid calling client code on server side
     */
    public boolean isRunningOnClient() {
        return !(proxy instanceof ServerProxy);
    }

    private void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityPhysicsInfuser.class, new ResourceLocation(MOD_ID, "tile_phys_infuser"));
    }

}
