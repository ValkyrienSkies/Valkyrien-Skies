package org.valkyrienskies.addon.control;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.GameData;
import org.valkyrienskies.addon.control.block.multiblocks.*;
import org.valkyrienskies.addon.control.block.torque.TileEntityRotationAxle;
import org.valkyrienskies.addon.control.capability.ICapabilityLastRelay;
import org.valkyrienskies.addon.control.capability.ImplCapabilityLastRelay;
import org.valkyrienskies.addon.control.capability.StorageLastRelay;
import org.valkyrienskies.addon.control.item.ItemPhysicsCore;
import org.valkyrienskies.addon.control.item.ItemRelayWire;
import org.valkyrienskies.addon.control.item.ItemVSWrench;
import org.valkyrienskies.addon.control.item.ItemVanishingWire;
import org.valkyrienskies.addon.control.network.VSGuiButtonHandler;
import org.valkyrienskies.addon.control.network.VSGuiButtonMessage;
import org.valkyrienskies.addon.control.proxy.CommonProxyControl;
import org.valkyrienskies.addon.control.tileentity.*;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

import java.util.ArrayList;
import java.util.List;

@Mod(
    name = ValkyrienSkiesControl.MOD_NAME,
    modid = ValkyrienSkiesControl.MOD_ID,
    version = ValkyrienSkiesControl.MOD_VERSION
    // dependencies = "required-after:" + ValkyrienSkiesControl.VS_WORLD_MOD_ID
)
@Mod.EventBusSubscriber(modid = ValkyrienSkiesControl.MOD_ID)
public class ValkyrienSkiesControl {
    // Used for registering stuff
    public static final List<Block> BLOCKS = new ArrayList<>();
    public static final List<Item> ITEMS = new ArrayList<>();

    // MOD INFO CONSTANTS
    public static final String MOD_ID = "vs_control";
    public static final String MOD_NAME = "Valkyrien Skies Control";
    public static final String MOD_VERSION = ValkyrienSkiesMod.MOD_VERSION;

    public static final String VS_WORLD_MOD_ID = "vs_world";

    public static SimpleNetworkWrapper controlGuiNetwork;

    // MOD INSTANCE
    @Instance(MOD_ID)
    public static ValkyrienSkiesControl INSTANCE;

    @SidedProxy(
        clientSide = "org.valkyrienskies.addon.control.proxy.ClientProxyControl",
        serverSide = "org.valkyrienskies.addon.control.proxy.CommonProxyControl")
    private static CommonProxyControl proxy;

    @CapabilityInject(ICapabilityLastRelay.class)
    public static final Capability<ICapabilityLastRelay> lastRelayCapability = null;

    public BlocksValkyrienSkiesControl vsControlBlocks;
    public Item relayWire;
    public Item vanishingWire;
    public Item vsWrench;
    public Item physicsCore;

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
        Block[] blockArray = BLOCKS.toArray(new Block[0]);
        event.getRegistry().registerAll(blockArray);
	}

	public void addBlocks() {
		INSTANCE.vsControlBlocks = new BlocksValkyrienSkiesControl();
	}

	public void registerMultiblocks() {
        MultiblockRegistry
        .registerAllPossibleSchematicVariants(ValkyriumEngineMultiblockSchematic.class);
        MultiblockRegistry
        .registerAllPossibleSchematicVariants(ValkyriumCompressorMultiblockSchematic.class);
        MultiblockRegistry
        .registerAllPossibleSchematicVariants(RudderAxleMultiblockSchematic.class);
        MultiblockRegistry
        .registerAllPossibleSchematicVariants(GiantPropellerMultiblockSchematic.class);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ITEMS.toArray(new Item[0]));
    }

    public void addItems() {
		INSTANCE.relayWire = new ItemRelayWire();
		INSTANCE.vanishingWire = new ItemVanishingWire();
		INSTANCE.vsWrench = new ItemVSWrench();
		INSTANCE.physicsCore = new ItemPhysicsCore();
	}

    public void registerRecipes() {
		addEngineRecipe(INSTANCE.vsControlBlocks.basicEngine, Blocks.PLANKS);
		addEngineRecipe(INSTANCE.vsControlBlocks.advancedEngine, Blocks.STONE);
		addEngineRecipe(INSTANCE.vsControlBlocks.advancedEngine, Blocks.COBBLESTONE);
		addEngineRecipe(INSTANCE.vsControlBlocks.eliteEngine, Items.IRON_INGOT);
		addEngineRecipe(INSTANCE.vsControlBlocks.ultimateEngine, Blocks.OBSIDIAN);
		addEngineRecipe(INSTANCE.vsControlBlocks.redstoneEngine, Blocks.REDSTONE_BLOCK);
        Item relayWireIngot = Items.IRON_INGOT;
        // TODO: Code to check for copper and set relayWireIngot

        addShapedRecipe(INSTANCE.vsControlBlocks.physicsInfuser, 1,
                "IEI",
                "ODO",
                "IEI",
                'E', Items.ENDER_PEARL,
                'D', Items.DIAMOND,
                'O', Item.getItemFromBlock(Blocks.OBSIDIAN),
                'I', Items.IRON_INGOT);

        addShapedRecipe(INSTANCE.relayWire, 4, // 1 per copper/iron ingot
            " I ",
            "ISI",
            " I ",
            'I', relayWireIngot,
            'S', Items.STICK);

        if (Loader.isModLoaded(VS_WORLD_MOD_ID)) {
            addVsWorldRecipes();
        }
    }

    @Optional.Method(modid = VS_WORLD_MOD_ID)
    public void addVsWorldRecipes() {
	    /*
        addShapedRecipe(INSTANCE.vanishingWire, 8,
            "WWW",
            "WVW",
            "WWW",
            'W', INSTANCE.relayWire,
            'V', ValkyrienSkiesWorld.INSTANCE.valkyriumCrystal);

        addShapedRecipe(INSTANCE.vsControlBlocks.compactedValkyrium, 1,
            "VVV",
            "VVV",
            "VVV",
            'V', ValkyrienSkiesWorld.INSTANCE.valkyriumCrystal);

        addShapedRecipe(INSTANCE.vsControlBlocks.valkyriumEnginePart, 1,
            "IVI",
            "VFV",
            "IVI",
            'I', Items.IRON_INGOT,
            'F', Item.getItemFromBlock(Blocks.FURNACE),
            'V', ValkyrienSkiesWorld.INSTANCE.valkyriumCrystal);

        addShapedRecipe(INSTANCE.vsControlBlocks.valkyriumCompressorPart, 1,
            "GVG",
            "VFV",
            "GVG",
            'G', Items.GOLD_INGOT,
            'F', Item.getItemFromBlock(Blocks.PISTON),
            'V', ValkyrienSkiesWorld.INSTANCE.valkyriumCrystal);

	     */
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
		addItems();
		addBlocks();
        registerNetworks();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
		registerRecipes();
		registerMultiblocks();
        registerTileEntities();
        registerCapabilities();
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    private void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityNetworkRelay.class,
            new ResourceLocation(MOD_ID, "tile_network_relay"));
        GameRegistry.registerTileEntity(TileEntityShipHelm.class,
            new ResourceLocation(MOD_ID, "tile_ship_helm"));
        GameRegistry.registerTileEntity(TileEntitySpeedTelegraph.class,
            new ResourceLocation(MOD_ID, "tile_speed_telegraph"));
        GameRegistry.registerTileEntity(TileEntityPropellerEngine.class,
            new ResourceLocation(MOD_ID, "tile_propeller_engine"));
        GameRegistry.registerTileEntity(TileEntityGyroscopeStabilizer.class,
            new ResourceLocation(MOD_ID, "tile_gyroscope_stabilizer"));
        GameRegistry.registerTileEntity(TileEntityLiftValve.class,
            new ResourceLocation(MOD_ID, "tile_lift_valve"));
        GameRegistry.registerTileEntity(TileEntityNetworkDisplay.class,
            new ResourceLocation(MOD_ID, "tile_network_display"));
        GameRegistry.registerTileEntity(TileEntityLiftLever.class,
            new ResourceLocation(MOD_ID, "tile_lift_lever"));

        GameRegistry.registerTileEntity(TileEntityGyroscopeDampener.class,
            new ResourceLocation(MOD_ID, "tile_gyroscope_dampener"));
        GameRegistry.registerTileEntity(TileEntityValkyriumEnginePart.class,
            new ResourceLocation(MOD_ID, "tile_valkyrium_engine_part"));
        GameRegistry.registerTileEntity(TileEntityGearbox.class,
            new ResourceLocation(MOD_ID, "tile_gearbox"));
        GameRegistry.registerTileEntity(TileEntityValkyriumCompressorPart.class,
            new ResourceLocation(MOD_ID, "tile_valkyrium_compressor_part"));
        GameRegistry.registerTileEntity(TileEntityRudderPart.class,
            new ResourceLocation(MOD_ID, "tile_rudder_part"));
        GameRegistry.registerTileEntity(TileEntityGiantPropellerPart.class,
            new ResourceLocation(MOD_ID, "tile_giant_propeller_part"));
        GameRegistry.registerTileEntity(TileEntityRotationAxle.class,
            new ResourceLocation(MOD_ID, "tile_rotation_axle"));

        GameRegistry.registerTileEntity(TileEntityPhysicsInfuser.class,
                new ResourceLocation(MOD_ID, "tile_physics_infuser"));
    }

    private void registerCapabilities() {
        CapabilityManager.INSTANCE.register(ICapabilityLastRelay.class, new StorageLastRelay(),
            ImplCapabilityLastRelay::new);
    }

    public void addShapedRecipe(ItemStack output, Object... params) {
		ResourceLocation location = getNameForRecipe(output);
		CraftingHelper.ShapedPrimer primer = CraftingHelper.parseShaped(params);
		ShapedRecipes recipe = new ShapedRecipes(
			output.getItem().getRegistryName().toString(),
			primer.width, primer.height, primer.input, output);
		recipe.setRegistryName(location);
		GameData.register_impl(recipe);
	}

	public void addShapedRecipe(Item output, int outputCount, Object... params) {
		addShapedRecipe(new ItemStack(output, outputCount), params);
	}
	public void addShapedRecipe(Block output, int outputCount, Object... params) {
		addShapedRecipe(new ItemStack(output, outputCount), params);
	}

	// Engine recipe helpers
	public void addEngineRecipe(Block output, Item type) {
		addShapedRecipe(output, 4,
			"I##",
			"IPP",
			"I##",
			'#', type,
			'P', Item.getItemFromBlock(Blocks.PISTON),
			'I', Items.IRON_INGOT);
	}

	public void addEngineRecipe(Block output, Block type) {
		addEngineRecipe(output, Item.getItemFromBlock(type));
	}

	// If a recipe already exists, increment number
	/* eg:
	  vs_control:item_0
	  vs_control:item_1
	*/
	private static ResourceLocation getNameForRecipe(ItemStack output) {
		ResourceLocation baseLoc = new ResourceLocation(MOD_ID, output.getItem().getRegistryName().getPath());
		ResourceLocation recipeLoc = baseLoc;
		int index = 0;
		while (CraftingManager.REGISTRY.containsKey(recipeLoc)) {
			index++;
			recipeLoc = new ResourceLocation(MOD_ID, baseLoc.getPath() + "_" + index);
		}
		return recipeLoc;
	}

	private void registerNetworks() {
        controlGuiNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("vs-control");
        controlGuiNetwork.registerMessage(VSGuiButtonHandler.class,
                VSGuiButtonMessage.class, 1, Side.SERVER);
    }
}
