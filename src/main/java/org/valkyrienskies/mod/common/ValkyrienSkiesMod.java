package org.valkyrienskies.mod.common;

import com.esotericsoftware.kryo.Kryo;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UUIDSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.guava.ArrayListMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ArrayTableSerializer;
import de.javakaffee.kryoserializers.guava.HashBasedTableSerializer;
import de.javakaffee.kryoserializers.guava.HashMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableListSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableTableSerializer;
import de.javakaffee.kryoserializers.guava.LinkedHashMultimapSerializer;
import de.javakaffee.kryoserializers.guava.LinkedListMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ReverseListSerializer;
import de.javakaffee.kryoserializers.guava.TreeBasedTableSerializer;
import de.javakaffee.kryoserializers.guava.TreeMultimapSerializer;
import de.javakaffee.kryoserializers.guava.UnmodifiableNavigableSetSerializer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.valkyrienskies.mixin.MixinLoaderForge;
import org.valkyrienskies.mod.client.gui.TabValkyrienSkies;
import org.valkyrienskies.mod.common.block.BlockPhysicsInfuser;
import org.valkyrienskies.mod.common.block.BlockPhysicsInfuserCreative;
import org.valkyrienskies.mod.common.block.BlockPhysicsInfuserDummy;
import org.valkyrienskies.mod.common.command.framework.VSModCommandRegistry;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.item.ItemPhysicsCore;
import org.valkyrienskies.mod.common.network.PhysWrapperPositionHandler;
import org.valkyrienskies.mod.common.network.SubspacedEntityRecordHandler;
import org.valkyrienskies.mod.common.network.SubspacedEntityRecordMessage;
import org.valkyrienskies.mod.common.network.VSGuiButtonHandler;
import org.valkyrienskies.mod.common.network.VSGuiButtonMessage;
import org.valkyrienskies.mod.common.network.WrapperPositionMessage;
import org.valkyrienskies.mod.common.physics.management.DimensionPhysObjectManager;
import org.valkyrienskies.mod.common.physmanagement.VS_APIPhysicsEntityManager;
import org.valkyrienskies.mod.common.physmanagement.chunk.DimensionPhysicsChunkManager;
import org.valkyrienskies.mod.common.physmanagement.chunk.VSChunkClaim;
import org.valkyrienskies.mod.common.physmanagement.shipdata.IValkyrienSkiesWorldData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.ImplValkyrienSkiesWorldData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.ShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.ShipPositionData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.StorageValkyrienSkiesWorldData;
import org.valkyrienskies.mod.common.tileentity.TileEntityPhysicsInfuser;
import org.valkyrienskies.mod.proxy.CommonProxy;
import valkyrienwarfare.api.IPhysicsEntityManager;

@Mod(
    modid = ValkyrienSkiesMod.MOD_ID,
    name = ValkyrienSkiesMod.MOD_NAME,
    version = ValkyrienSkiesMod.MOD_VERSION,
    updateJSON = "https://raw.githubusercontent.com/ValkyrienSkies/Valkyrien-Skies/master/update.json",
    certificateFingerprint = ValkyrienSkiesMod.MOD_FINGERPRINT
)
@Log4j2
public class ValkyrienSkiesMod {

    // MOD INFO CONSTANTS
    public static final String MOD_ID = "valkyrienskies";
    public static final String MOD_NAME = "Valkyrien Skies";
    public static final String MOD_VERSION = "1.0-alpha-1";
    static final String MOD_FINGERPRINT = "b308676914a5e7d99459c1d2fb298744387899a7";

    // MOD INSTANCE
    @Instance(MOD_ID)
    public static ValkyrienSkiesMod INSTANCE;

    // MOD CLASS MEMBERS
    /**
     * This capability provides data attached to the world.
     */
    @CapabilityInject(IValkyrienSkiesWorldData.class)
    public static final Capability<IValkyrienSkiesWorldData> VS_WORLD_DATA = null;

    @SidedProxy(
        clientSide = "org.valkyrienskies.mod.proxy.ClientProxy",
        serverSide = "org.valkyrienskies.mod.proxy.ServerProxy")
    public static CommonProxy proxy;

    static final int VS_ENTITY_LOAD_DISTANCE = 128;
    public static final DimensionPhysicsChunkManager VS_CHUNK_MANAGER =
        new DimensionPhysicsChunkManager();
    public static final DimensionPhysObjectManager VS_PHYSICS_MANAGER =
        new DimensionPhysObjectManager();
    /**
     * This service is directly responsible for running collision tasks.
     */
    @Getter
    private static ExecutorService PHYSICS_THREADS_EXECUTOR = null;
    public Block physicsInfuser;
    public Block physicsInfuserCreative;
    public Block physicsInfuserDummy;
    public Item physicsCore;
    public static SimpleNetworkWrapper physWrapperNetwork;
    public static final CreativeTabs VS_CREATIVE_TAB = new TabValkyrienSkies(MOD_ID);

    private CompletableFuture<Kryo> kryoInstance;

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        if (MixinLoaderForge.isObfuscatedEnvironment) { //only print signature warning in obf
            FMLLog.bigWarning(
                "Valkyrien Skies JAR fingerprint corrupted, which means this copy of the mod "
                    + "may have come from unofficial sources. Please check out our official website: "
                    + "https://valkyrienskies.org");
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log.debug("Initializing configuration");
        runConfiguration();

        log.debug("Instantiating the physics thread executor");
        ValkyrienSkiesMod.PHYSICS_THREADS_EXECUTOR = Executors
            .newFixedThreadPool(VSConfig.threadCount);

        log.debug("Beginning asynchronous Kryo initialization");
        serializationInitAsync();
        registerNetworks(event);

        registerCapabilities();
        proxy.preInit(event);

        log.debug("Initializing the VS API");
        try {
            Field instanceField = IPhysicsEntityManager.class.getDeclaredField("INSTANCE");
            // Make the field accessible
            instanceField.setAccessible(true);
            // Remove the final modifier
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(instanceField, instanceField.getModifiers() & ~Modifier.FINAL);
            // Finally set the new value of the field.
            instanceField.set(null, new VS_APIPhysicsEntityManager());
        } catch (Exception e) {
            e.printStackTrace();
            log.fatal("FAILED TO INITIALIZE VS API!");
        }
        // Initialize VS API end.
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // Print out a message of core count, we want this to know what AnvilNode is giving us.
        log.info("Valkyrien Skies Initialization:");
        log.info("We are running on " + Runtime.getRuntime().availableProcessors() +
            " threads; 4 or more is recommended!");
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        VSModCommandRegistry.registerCommands(server);
    }

    private void registerNetworks(FMLStateEvent event) {
        physWrapperNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("physChannel");
        physWrapperNetwork.registerMessage(PhysWrapperPositionHandler.class,
            WrapperPositionMessage.class, 0, Side.CLIENT);
        physWrapperNetwork.registerMessage(SubspacedEntityRecordHandler.class,
            SubspacedEntityRecordMessage.class, 1, Side.CLIENT);
        physWrapperNetwork.registerMessage(SubspacedEntityRecordHandler.class,
            SubspacedEntityRecordMessage.class, 2, Side.SERVER);
        physWrapperNetwork.registerMessage(VSGuiButtonHandler.class,
            VSGuiButtonMessage.class, 3, Side.SERVER);
    }

    void registerBlocks(RegistryEvent.Register<Block> event) {
        physicsInfuser = new BlockPhysicsInfuser(Material.ROCK).setHardness(8f)
            .setTranslationKey("physics_infuser")
            .setRegistryName(MOD_ID, "physics_infuser")
            .setCreativeTab(VS_CREATIVE_TAB);
        physicsInfuserCreative = new BlockPhysicsInfuserCreative(Material.ROCK).setHardness(12f)
            .setTranslationKey("creative_physics_infuser")
            .setRegistryName(MOD_ID, "creative_physics_infuser")
            .setCreativeTab(VS_CREATIVE_TAB);
        // // Do not put the VS_CREATIVE_TAB block into the creative tab
        physicsInfuserDummy = new BlockPhysicsInfuserDummy(Material.ROCK).setHardness(12f)
            .setTranslationKey("dummy_physics_infuser")
            .setRegistryName(MOD_ID, "dummy_physics_infuser")
            .setCreativeTab(VS_CREATIVE_TAB);

        event.getRegistry().register(physicsInfuser);
        event.getRegistry().register(physicsInfuserCreative);
        event.getRegistry().register(physicsInfuserDummy);

        registerTileEntities();
    }

    /**
     * Create our new instance of {@link Kryo}. This is done asynchronously with CompletableFuture
     * so as not to slow down initialization. We save a lot of time this way!
     */
    private void serializationInitAsync() {
        kryoInstance = CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();

            Kryo kryo = new Kryo();

            // region More serializers

            //noinspection ArraysAsListWithZeroOrOneArgument
            UnmodifiableCollectionsSerializer.registerSerializers(kryo);
            SynchronizedCollectionsSerializer.registerSerializers(kryo);

            ImmutableListSerializer.registerSerializers(kryo);
            ImmutableSetSerializer.registerSerializers(kryo);
            ImmutableMapSerializer.registerSerializers(kryo);
            ImmutableMultimapSerializer.registerSerializers(kryo);
            ImmutableTableSerializer.registerSerializers(kryo);
            ReverseListSerializer.registerSerializers(kryo);
            UnmodifiableNavigableSetSerializer.registerSerializers(kryo);

            ArrayListMultimapSerializer.registerSerializers(kryo);
            HashMultimapSerializer.registerSerializers(kryo);
            LinkedHashMultimapSerializer.registerSerializers(kryo);
            LinkedListMultimapSerializer.registerSerializers(kryo);
            TreeMultimapSerializer.registerSerializers(kryo);
            ArrayTableSerializer.registerSerializers(kryo);
            HashBasedTableSerializer.registerSerializers(kryo);
            TreeBasedTableSerializer.registerSerializers(kryo);

            // endregion

            kryo.register(ConcurrentIndexedCollection.class);
            kryo.register(ShipData.class);
            kryo.register(ShipPositionData.class);
            kryo.register(VSChunkClaim.class);
            kryo.register(HashSet.class);
            kryo.register(UUID.class, new UUIDSerializer());

            // This should be changed to true but only once we're stable
            kryo.setRegistrationRequired(false);

            log.debug("Kryo initialization: " + (System.currentTimeMillis() - start) + "ms");

            return kryo;
        });
    }

    /**
     * @return The Kryo instance for the mod. This operation is blocking!
     */
    public Kryo getKryo() {
        try {
            return kryoInstance.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void registerItems(RegistryEvent.Register<Item> event) {
        registerItemBlock(event, physicsInfuser);
        registerItemBlock(event, physicsInfuserCreative);

        this.physicsCore = new ItemPhysicsCore().setTranslationKey("physics_core")
            .setRegistryName(MOD_ID, "physics_core")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);
        event.getRegistry()
            .register(this.physicsCore);
    }

    private static void registerItemBlock(RegistryEvent.Register<Item> event, Block block) {
        event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
    }

    void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        registerRecipe(event, "recipe_physics_infuser", new ItemStack(physicsInfuser),
            "IEI", "ODO", "IEI", 'E', Items.ENDER_PEARL, 'D',
            Items.DIAMOND, 'O', Item.getItemFromBlock(Blocks.OBSIDIAN), 'I', Items.IRON_INGOT);
    }

    private static void registerRecipe(RegistryEvent.Register<IRecipe> event,
        String registryName, ItemStack out, Object... in) {
        CraftingHelper.ShapedPrimer primer = CraftingHelper.parseShaped(in);
        event.getRegistry()
            .register(new ShapedRecipes(ValkyrienSkiesMod.MOD_ID, primer.width, primer.height,
                primer.input, out)
                .setRegistryName(ValkyrienSkiesMod.MOD_ID, registryName));
    }

    /**
     * Initializes the configuration - {@link VSConfig}
     */
    private void runConfiguration() {
        VSConfig.sync();
    }

    private void registerCapabilities() {
        CapabilityManager.INSTANCE.register(IValkyrienSkiesWorldData.class,
            new StorageValkyrienSkiesWorldData(),
            ImplValkyrienSkiesWorldData::new);
    }

    private void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityPhysicsInfuser.class,
            new ResourceLocation(MOD_ID, "tile_physics_infuser"));
    }

}
