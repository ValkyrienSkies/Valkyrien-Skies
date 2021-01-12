package org.valkyrienskies.mod.common;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.valkyrienskies.mixin.MixinLoaderForge;
import org.valkyrienskies.mod.client.gui.TabValkyrienSkies;
import org.valkyrienskies.mod.common.block.BlockCaptainsChair;
import org.valkyrienskies.mod.common.block.BlockPassengerChair;
import org.valkyrienskies.mod.common.block.BlockWaterPump;
import org.valkyrienskies.mod.common.capability.VSCapabilityRegistry;
import org.valkyrienskies.mod.common.command.framework.VSCommandRegistry;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.item.ItemShipTracker;
import org.valkyrienskies.mod.common.network.*;
import org.valkyrienskies.mod.common.piloting.PilotControlsMessage;
import org.valkyrienskies.mod.common.piloting.PilotControlsMessageHandler;
import org.valkyrienskies.mod.common.ships.deprecated_api.VS_APIPhysicsEntityManager;
import org.valkyrienskies.mod.common.tileentity.TileEntityCaptainsChair;
import org.valkyrienskies.mod.common.tileentity.TileEntityPassengerChair;
import org.valkyrienskies.mod.common.tileentity.TileEntityWaterPump;
import org.valkyrienskies.mod.fixes.darkness_lib_fix.VSDarknessLibAPILightProvider;
import org.valkyrienskies.mod.proxy.CommonProxy;
import valkyrienwarfare.api.IPhysicsEntityManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

@Mod(
    modid = ValkyrienSkiesMod.MOD_ID,
    useMetadata = true,
    updateJSON = "https://raw.githubusercontent.com/ValkyrienSkies/Valkyrien-Skies/master/update.json",
    certificateFingerprint = ValkyrienSkiesMod.MOD_FINGERPRINT
)
@Log4j2
public class ValkyrienSkiesMod {
    // Used for registering stuff
    public static final List<Block> BLOCKS = new ArrayList<>();
    public static final List<Item> ITEMS = new ArrayList<>();

    // MOD INFO CONSTANTS
    public static final String MOD_ID = "valkyrienskies";
    static final String MOD_FINGERPRINT = "b308676914a5e7d99459c1d2fb298744387899a7";

    // MOD INSTANCE
    @Instance(MOD_ID)
    public static ValkyrienSkiesMod INSTANCE;

    @SidedProxy(
        clientSide = "org.valkyrienskies.mod.proxy.ClientProxy",
        serverSide = "org.valkyrienskies.mod.proxy.ServerProxy")
    public static CommonProxy proxy;

    static final int VS_ENTITY_LOAD_DISTANCE = 128;

    /**
     * This service is directly responsible for running collision tasks.
     */
    @Getter
    private static ForkJoinPool physicsThreadPool = null;

    public Block captainsChair;
    public Block passengerChair;
    public Block waterPump;
    public Item shipTracker;
    public static SimpleNetworkWrapper physWrapperNetwork;
    public static SimpleNetworkWrapper physWrapperTransformUpdateNetwork;
    public static SimpleNetworkWrapper controlNetwork;
    public static final CreativeTabs VS_CREATIVE_TAB = new TabValkyrienSkies(MOD_ID);

    private static final List<String> MODULES = ImmutableList.of("vs_control", "vs_world");

    /**
     * Whether or not any known dependent mods are loaded.
     */
    @Getter
    private static boolean isAnyModuleLoaded = false;

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
        log.debug("Initializing configuration.");
        runConfiguration();

        log.debug("Instantiating the physics thread executor.");
        ValkyrienSkiesMod.physicsThreadPool = new ForkJoinPool(VSConfig.threadCount);

        log.debug("Initializing networks.");
        registerNetworks(event);

		VSCapabilityRegistry.registerCapabilities();
        proxy.preInit(event);

        log.debug("Initializing the VS API.");
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

        registerItems();
		registerBlocks();

		registerDarknessLib();
    }

    private void registerDarknessLib() {
        // Register light provider with DarknessLib
        try {
            // Why yes, I am using some hacky reflection. I know theres an IMCEvent way to do it, but I can't get it to
            // work no matter what I do! So reflection it is!
            final Class clazz = Class.forName("com.shinoow.darknesslib.api.DarknessLibAPI");
            final Field instanceField = clazz.getDeclaredField("INSTANCE");
            final Field lightProvidersField = clazz.getDeclaredField("LIGHT_PROVIDERS");
            // Remove private from the fields
            instanceField.setAccessible(true);
            lightProvidersField.setAccessible(true);
            // Finally add the light provider
            final Object instance = instanceField.get(null);
            final List<Function<EntityPlayer, Integer>> lightProviders = (List<Function<EntityPlayer, Integer>>) lightProvidersField.get(instance);
            lightProviders.add(new VSDarknessLibAPILightProvider());
        } catch (Exception e) {

        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        log.info("Valkyrien Skies Initialization: We are running on {} threads; 4 or more "
            + "is recommended!", Runtime.getRuntime().availableProcessors());
        proxy.init(event);

        isAnyModuleLoaded = MODULES.stream().anyMatch(Loader::isModLoaded);
        isSpongePresent = Loader.isModLoaded("spongeforge");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        VSCommandRegistry.registerCommands(server);
    }

    private void registerNetworks(FMLStateEvent event) {
        physWrapperNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("valkyrien_skies");
        physWrapperNetwork.registerMessage(ShipIndexDataMessageHandler.class,
                ShipIndexDataMessage.class, 0, Side.CLIENT);

        controlNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("valkyrien_piloting");
        controlNetwork.registerMessage(PilotControlsMessageHandler.class,
                PilotControlsMessage.class, 0, Side.SERVER);
        controlNetwork.registerMessage(MessageStartPilotingHandler.class,
                MessageStartPiloting.class, 1, Side.CLIENT);
        controlNetwork.registerMessage(MessageStopPilotingHandler.class,
                MessageStopPiloting.class, 2, Side.CLIENT);
        controlNetwork.registerMessage(MessagePlayerStoppedPilotingHandler.class,
                MessagePlayerStoppedPiloting.class, 3, Side.SERVER);

        physWrapperTransformUpdateNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("valkyrien_skies_ship_transforms");
        physWrapperTransformUpdateNetwork.registerMessage(ShipTransformUpdateMessageHandler.class,
                ShipTransformUpdateMessage.class, 0, Side.CLIENT);
    }

    void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if (!VSConfig.chairRecipes) return;

        registerRecipe(event, "recipe_captains_chair", new ItemStack(captainsChair),
                    "SLS",
                    "VWV",
                    " S ",
                    'S', Items.STICK,
                    'L', Items.LEATHER,
                    'W', Item.getItemFromBlock(Blocks.LOG),
                    'V', Items.DIAMOND);

        registerRecipe(event, "recipe_passenger_chair", new ItemStack(passengerChair),
                    "SLS",
                    "PWP",
                    " S ",
                    'S', Items.STICK,
                    'L', Items.LEATHER,
                    'W', Item.getItemFromBlock(Blocks.LOG),
                    'P', Item.getItemFromBlock(Blocks.PLANKS));
    }

    private static void registerRecipe(RegistryEvent.Register<IRecipe> event,
        String registryName, ItemStack out, Object... in) {
        CraftingHelper.ShapedPrimer primer = CraftingHelper.parseShaped(in);
        event.getRegistry()
            .register(new ShapedRecipes(
                ValkyrienSkiesMod.MOD_ID, primer.width, primer.height, primer.input, out)
                .setRegistryName(ValkyrienSkiesMod.MOD_ID, registryName));
    }

    /**
     * Initializes the configuration - {@link VSConfig}
     */
    private void runConfiguration() {
        VSConfig.sync();
    }

    private void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityCaptainsChair.class,
                new ResourceLocation(MOD_ID, "tile_captains_chair"));
        GameRegistry.registerTileEntity(TileEntityPassengerChair.class,
                new ResourceLocation(MOD_ID, "tile_passenger_chair"));
        GameRegistry.registerTileEntity(TileEntityWaterPump.class,
                new ResourceLocation(MOD_ID, "tile_water_pump"));
    }

    private void registerBlocks() {
        this.captainsChair = registerBlock(new BlockCaptainsChair());
        this.passengerChair = registerBlock(new BlockPassengerChair());
        this.waterPump = registerBlock(new BlockWaterPump());


        this.registerTileEntities();
    }

    private Block registerBlock(Block block) {
        ValkyrienSkiesMod.BLOCKS.add(block);
        ValkyrienSkiesMod.ITEMS.add(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        return block;
    }

    private Item registerItem(Item item) {
        ValkyrienSkiesMod.ITEMS.add(item);
        return item;
    }

    private void registerItems() {
        this.shipTracker = registerItem(new ItemShipTracker("vs_ship_tracker", true));
    }

    @Getter
    private static boolean isSpongePresent = false;

}
