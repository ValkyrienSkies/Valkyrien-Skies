package ValkyrienWarfareControl;

import java.io.File;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.Block.BlockAirshipController_Zepplin;
import ValkyrienWarfareControl.Block.BlockBalloonBurner;
import ValkyrienWarfareControl.Block.BlockDopedEtherium;
import ValkyrienWarfareControl.Block.BlockHovercraftController;
import ValkyrienWarfareControl.Block.BlockShipHelm;
import ValkyrienWarfareControl.Block.BlockShipPassengerChair;
import ValkyrienWarfareControl.Block.BlockShipPilotsChair;
import ValkyrienWarfareControl.Block.BlockShipTelegraph;
import ValkyrienWarfareControl.Block.BlockShipWheel;
import ValkyrienWarfareControl.Block.BlockThrustModulator;
import ValkyrienWarfareControl.Block.BlockThrustRelay;
import ValkyrienWarfareControl.Block.Engine.BlockNormalEngine;
import ValkyrienWarfareControl.Block.Engine.BlockRedstoneEngine;
import ValkyrienWarfareControl.Block.EtherCompressor.BlockCreativeEtherCompressor;
import ValkyrienWarfareControl.Block.EtherCompressor.BlockNormalEtherCompressor;
import ValkyrienWarfareControl.Capability.ICapabilityLastRelay;
import ValkyrienWarfareControl.Capability.ImplCapabilityLastRelay;
import ValkyrienWarfareControl.Capability.StorageLastRelay;
import ValkyrienWarfareControl.GUI.ControlGUIHandler;
import ValkyrienWarfareControl.Item.ItemRelayWire;
import ValkyrienWarfareControl.Item.ItemShipStealer;
import ValkyrienWarfareControl.Item.ItemSystemLinker;
import ValkyrienWarfareControl.Network.EntityFixMessage;
import ValkyrienWarfareControl.Network.EntityFixMessageHandler;
import ValkyrienWarfareControl.Network.HovercraftControllerGUIInputHandler;
import ValkyrienWarfareControl.Network.HovercraftControllerGUIInputMessage;
import ValkyrienWarfareControl.Network.MessagePlayerStoppedPiloting;
import ValkyrienWarfareControl.Network.MessagePlayerStoppedPilotingHandler;
import ValkyrienWarfareControl.Network.MessageStartPiloting;
import ValkyrienWarfareControl.Network.MessageStartPilotingHandler;
import ValkyrienWarfareControl.Network.MessageStopPiloting;
import ValkyrienWarfareControl.Network.MessageStopPilotingHandler;
import ValkyrienWarfareControl.Piloting.PilotControlsMessage;
import ValkyrienWarfareControl.Piloting.PilotControlsMessageHandler;
import ValkyrienWarfareControl.Proxy.CommonProxyControl;
import ValkyrienWarfareControl.TileEntity.BalloonBurnerTileEntity;
import ValkyrienWarfareControl.TileEntity.ThrustModulatorTileEntity;
import ValkyrienWarfareControl.TileEntity.ThrustRelayTileEntity;
import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import ValkyrienWarfareControl.TileEntity.TileEntityNormalEtherCompressor;
import ValkyrienWarfareControl.TileEntity.TileEntityPilotsChair;
import ValkyrienWarfareControl.TileEntity.TileEntityShipHelm;
import ValkyrienWarfareControl.TileEntity.TileEntityShipTelegraph;
import ValkyrienWarfareWorld.ValkyrienWarfareWorldMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = ValkyrienWarfareControlMod.MODID, name = ValkyrienWarfareControlMod.MODNAME, version = ValkyrienWarfareControlMod.MODVER)
public class ValkyrienWarfareControlMod {

    public static final String MODID = "valkyrienwarfarecontrol";
    public static final String MODNAME = "Valkyrien Warfare Control";
    public static final String MODVER = "0.9_alpha";
    @CapabilityInject(ICapabilityLastRelay.class)
    public static final Capability<ICapabilityLastRelay> lastRelayCapability = null;
    public static Configuration config;
    @Instance(MODID)
    public static ValkyrienWarfareControlMod instance = new ValkyrienWarfareControlMod();
    public static SimpleNetworkWrapper controlNetwork;
    @SidedProxy(clientSide = "ValkyrienWarfareControl.Proxy.ClientProxyControl", serverSide = "ValkyrienWarfareControl.Proxy.CommonProxyControl")
    public static CommonProxyControl proxy;
    public Block basicEngine;
    public Block advancedEngine;
    public Block eliteEngine;
    public Block ultimateEngine; // Couldn't think of what to name these, so I went with the Mekanism naming style
    public Block redstoneEngine;
    public Block basicHoverController;
    public Block dopedEtherium;
    public Block balloonBurner;
    public Block pilotsChair;
    public Block passengerChair;
    public Block shipHelm;
    public Block shipWheel;
    public Block shipTelegraph;
    public Block antigravityEngine; //leaving it with the old name to prevent blocks dissapearing
    public Block advancedEtherCompressor;
    public Block eliteEtherCompressor;
    public Block ultimateEtherCompressor;
    public Block creativeEtherCompressor;
    public Block thrustRelay;
    public Block thrustModulator;
    public Block airshipController_zepplin;

    public Item systemLinker;
    public Item airshipStealer;
    public Item relayWire;

    private static void registerBlock(Block block) {
        GameRegistry.register(block);
        registerItemBlock(block);
    }

    private static void registerItemBlock(Block block) {
        GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(new File(ValkyrienWarfareMod.getWorkingFolder() + "/config/valkyrienwarfarecontrol.cfg"));
        config.load();
        proxy.preInit(event);
        config.save();

        registerBlocks(event);
        registerItems(event);
        registerCapibilities(event);

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        registerTileEntities(event);
        registerRecipies(event);
        registerNetworks(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ControlGUIHandler());

        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    private void registerBlocks(FMLStateEvent event) {
        double basicEnginePower = config.get(Configuration.CATEGORY_GENERAL, "basicEnginePower", 4000D, "Engine power for the basic Engine").getDouble();
        double advancedEnginePower = config.get(Configuration.CATEGORY_GENERAL, "advancedEnginePower", 6000D, "Engine power for the advanced Engine").getDouble();
        double eliteEnginePower = config.get(Configuration.CATEGORY_GENERAL, "eliteEnginePower", 8000D, "Engine power for the elite Engine").getDouble();
        double ultimateEnginePower = config.get(Configuration.CATEGORY_GENERAL, "ultimateEnginePower", 16000D, "Engine power for the ultimate Engine").getDouble();
        double redstoneEnginePower = config.get(Configuration.CATEGORY_GENERAL, "redstoneEnginePower", 500D, "Multiplied by the redstone power (0-15) to the Redstone Engine").getDouble();

        double basicEtherCompressorPower = config.get(Configuration.CATEGORY_GENERAL, "basicEtherCompressorPower", 25000D, "Engine power for the basic Ether Compressor").getDouble();
        double advancedEtherCompressorPower = config.get(Configuration.CATEGORY_GENERAL, "advancedEtherCompressorPower", 45000D, "Engine power for the advanced Ether Compressor").getDouble();
        double eliteEtherCompressorPower = config.get(Configuration.CATEGORY_GENERAL, "eliteEtherCompressorPower", 80000D, "Engine power for the elite Ether Compressor").getDouble();
        double ultimateEtherCompressorPower = config.get(Configuration.CATEGORY_GENERAL, "ultimateEtherCompressorPower", 100000D, "Engine power for the ultimate Ether Compressor").getDouble();

        basicEngine = new BlockNormalEngine(Material.WOOD, basicEnginePower).setHardness(5f).setUnlocalizedName("basicengine").setRegistryName(MODID, "basicengine").setCreativeTab(ValkyrienWarfareMod.vwTab);
        advancedEngine = new BlockNormalEngine(Material.ROCK, advancedEnginePower).setHardness(6f).setUnlocalizedName("advancedengine").setRegistryName(MODID, "advancedengine").setCreativeTab(ValkyrienWarfareMod.vwTab);
        eliteEngine = new BlockNormalEngine(Material.IRON, eliteEnginePower).setHardness(8f).setUnlocalizedName("eliteengine").setRegistryName(MODID, "eliteengine").setCreativeTab(ValkyrienWarfareMod.vwTab);
        ultimateEngine = new BlockNormalEngine(Material.GROUND, ultimateEnginePower).setHardness(10f).setUnlocalizedName("ultimateengine").setRegistryName(MODID, "ultimateengine").setCreativeTab(ValkyrienWarfareMod.vwTab);
        redstoneEngine = new BlockRedstoneEngine(Material.REDSTONE_LIGHT, redstoneEnginePower).setHardness(7.0f).setUnlocalizedName("redstoneengine").setRegistryName(MODID, "redstoneengine").setCreativeTab(ValkyrienWarfareMod.vwTab);

        antigravityEngine = new BlockNormalEtherCompressor(Material.WOOD, basicEtherCompressorPower).setHardness(8f).setUnlocalizedName("antigravengine").setRegistryName(MODID, "antigravengine").setCreativeTab(ValkyrienWarfareMod.vwTab);
        advancedEtherCompressor = new BlockNormalEtherCompressor(Material.ROCK, advancedEtherCompressorPower).setHardness(8f).setUnlocalizedName("advancedethercompressor").setRegistryName(MODID, "advancedethercompressor").setCreativeTab(ValkyrienWarfareMod.vwTab);
        eliteEtherCompressor = new BlockNormalEtherCompressor(Material.IRON, eliteEtherCompressorPower).setHardness(8f).setUnlocalizedName("eliteethercompressor").setRegistryName(MODID, "eliteethercompressor").setCreativeTab(ValkyrienWarfareMod.vwTab);
        ultimateEtherCompressor = new BlockNormalEtherCompressor(Material.GROUND, ultimateEtherCompressorPower).setHardness(8f).setUnlocalizedName("ultimateethercompressor").setRegistryName(MODID, "ultimateethercompressor").setCreativeTab(ValkyrienWarfareMod.vwTab);
        creativeEtherCompressor = new BlockCreativeEtherCompressor(Material.BARRIER, Double.MAX_VALUE / 4).setHardness(0.0f).setUnlocalizedName("creativeethercompressor").setRegistryName(MODID, "creativeethercompressor").setCreativeTab(ValkyrienWarfareMod.vwTab);

        basicHoverController = new BlockHovercraftController(Material.IRON).setHardness(10f).setUnlocalizedName("basichovercraftcontroller").setRegistryName(MODID, "basichovercraftcontroller").setCreativeTab(ValkyrienWarfareMod.vwTab);
        dopedEtherium = new BlockDopedEtherium(Material.GLASS).setHardness(4f).setUnlocalizedName("dopedetherium").setRegistryName(MODID, "dopedetherium").setCreativeTab(ValkyrienWarfareMod.vwTab);
        balloonBurner = new BlockBalloonBurner(Material.IRON).setHardness(4f).setUnlocalizedName("balloonburner").setRegistryName(MODID, "balloonburner").setCreativeTab(ValkyrienWarfareMod.vwTab);
        pilotsChair = new BlockShipPilotsChair(Material.IRON).setHardness(4f).setUnlocalizedName("shippilotschair").setRegistryName(MODID, "shippilotschair").setCreativeTab(ValkyrienWarfareMod.vwTab);

        passengerChair = new BlockShipPassengerChair(Material.IRON).setHardness(4f).setUnlocalizedName("shippassengerchair").setRegistryName(MODID, "shippassengerchair").setCreativeTab(ValkyrienWarfareMod.vwTab);
        shipHelm = new BlockShipHelm(Material.WOOD).setHardness(4f).setUnlocalizedName("shiphelm").setRegistryName(MODID, "shiphelm").setCreativeTab(ValkyrienWarfareMod.vwTab);
        shipWheel = new BlockShipWheel(Material.WOOD).setHardness(5f).setUnlocalizedName("shiphelmwheel").setRegistryName(MODID, "shiphelmwheel").setCreativeTab(ValkyrienWarfareMod.vwTab);
        shipTelegraph = new BlockShipTelegraph(Material.WOOD).setHardness(5f).setUnlocalizedName("shiptelegraph").setRegistryName(MODID, "shiptelegraph").setCreativeTab(ValkyrienWarfareMod.vwTab);

        thrustRelay = new BlockThrustRelay(Material.IRON).setHardness(5f).setUnlocalizedName("thrustrelay").setRegistryName(MODID, "thrustrelay").setCreativeTab(ValkyrienWarfareMod.vwTab);
        thrustModulator = new BlockThrustModulator(Material.IRON).setHardness(8f).setUnlocalizedName("thrustmodulator").setRegistryName(MODID, "thrustmodulator").setCreativeTab(ValkyrienWarfareMod.vwTab);

        airshipController_zepplin = new BlockAirshipController_Zepplin(Material.WOOD).setHardness(5f).setUnlocalizedName("airshipcontroller_zepplin").setRegistryName(MODID, "airshipcontroller_zepplin").setCreativeTab(ValkyrienWarfareMod.vwTab);

        registerBlock(basicEngine);
        registerBlock(advancedEngine);
        registerBlock(eliteEngine);
        registerBlock(ultimateEngine);
        registerBlock(redstoneEngine);

        registerBlock(antigravityEngine);
        registerBlock(advancedEtherCompressor);
        registerBlock(eliteEtherCompressor);
        registerBlock(ultimateEtherCompressor);
        registerBlock(creativeEtherCompressor);

        registerBlock(basicHoverController);
        registerBlock(dopedEtherium);
        registerBlock(balloonBurner);
        registerBlock(pilotsChair);

        registerBlock(passengerChair);
        registerBlock(shipHelm);
        registerBlock(shipWheel);
        registerBlock(shipTelegraph);

        registerBlock(thrustRelay);
        registerBlock(thrustModulator);

        registerBlock(airshipController_zepplin);
    }

    private void registerTileEntities(FMLStateEvent event) {
        GameRegistry.registerTileEntity(TileEntityHoverController.class, "tilehovercontroller");
        GameRegistry.registerTileEntity(TileEntityNormalEtherCompressor.class, "tileantigravengine");
        GameRegistry.registerTileEntity(BalloonBurnerTileEntity.class, "tileballoonburner");
        GameRegistry.registerTileEntity(TileEntityPilotsChair.class, "tilemanualshipcontroller");
        GameRegistry.registerTileEntity(ThrustRelayTileEntity.class, "tilethrustrelay");
        GameRegistry.registerTileEntity(ThrustModulatorTileEntity.class, "tilethrustmodulator");
        GameRegistry.registerTileEntity(TileEntityShipHelm.class, "tileshiphelm");
        GameRegistry.registerTileEntity(TileEntityShipTelegraph.class, "tileshiptelegraph");
    }

    private void registerItems(FMLStateEvent event) {
        systemLinker = new ItemSystemLinker().setUnlocalizedName("systemlinker").setRegistryName(MODID, "systemlinker").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(1);

        airshipStealer = new ItemShipStealer().setUnlocalizedName("airshipStealer").setRegistryName(MODID, "airshipStealer").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(1);
        relayWire = new ItemRelayWire().setUnlocalizedName("relaywire").setRegistryName(MODID, "relaywire").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(1);

        GameRegistry.register(systemLinker);
        GameRegistry.register(airshipStealer);
        GameRegistry.register(relayWire);
    }

    private void registerRecipies(FMLStateEvent event) {
        GameRegistry.addRecipe(new ItemStack(pilotsChair), new Object[]{"SLS", "EWE", " S ", 'S', Items.STICK, 'L', Items.LEATHER, 'W', Item.getItemFromBlock(Blocks.LOG), 'E', ValkyrienWarfareWorldMod.etheriumCrystal});
        GameRegistry.addRecipe(new ItemStack(systemLinker), new Object[]{"IR ", " DR", "I I", 'I', Items.IRON_INGOT, 'D', Items.DIAMOND, 'R', Items.REDSTONE});
        GameRegistry.addRecipe(new ItemStack(balloonBurner), new Object[]{"IFI", "WIW", "PPP", 'I', Items.IRON_INGOT, 'F', Items.FLINT_AND_STEEL, 'W', Item.getItemFromBlock(Blocks.LOG), 'P', Item.getItemFromBlock(Blocks.PLANKS)});

        GameRegistry.addRecipe(new ItemStack(basicHoverController), new Object[]{"III", "TCT", "III", 'I', Item.getItemFromBlock(Blocks.IRON_BLOCK), 'C', Items.COMPASS, 'T', Item.getItemFromBlock(Blocks.CRAFTING_TABLE)});

        GameRegistry.addRecipe(new ItemStack(antigravityEngine, 4), new Object[]{"#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.PLANKS), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorldMod.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG)});
        GameRegistry.addRecipe(new ItemStack(advancedEtherCompressor, 4), new Object[]{"#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.STONE), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorldMod.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG)});
        GameRegistry.addRecipe(new ItemStack(advancedEtherCompressor, 2), new Object[]{"#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.COBBLESTONE), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorldMod.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG)});
        GameRegistry.addRecipe(new ItemStack(eliteEtherCompressor, 4), new Object[]{"III", "IEI", "WEW", 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorldMod.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG)});
        GameRegistry.addRecipe(new ItemStack(ultimateEtherCompressor, 4), new Object[]{"#I#", "#E#", "WEW", '#', Item.getItemFromBlock(Blocks.OBSIDIAN), 'I', Items.IRON_INGOT, 'E', ValkyrienWarfareWorldMod.etheriumCrystal, 'W', Item.getItemFromBlock(Blocks.LOG)});

        GameRegistry.addRecipe(new ItemStack(basicEngine, 4), new Object[]{"I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.PLANKS), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT});
        GameRegistry.addRecipe(new ItemStack(advancedEngine, 4), new Object[]{"I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.STONE), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT});
        GameRegistry.addRecipe(new ItemStack(advancedEngine, 2), new Object[]{"I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.COBBLESTONE), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT});
        GameRegistry.addRecipe(new ItemStack(eliteEngine, 4), new Object[]{"III", "IPP", "III", 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT});
        GameRegistry.addRecipe(new ItemStack(ultimateEngine, 4), new Object[]{"I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.OBSIDIAN), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT});

    }

    private void registerNetworks(FMLStateEvent event) {
        controlNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("controlnetwork");
        controlNetwork.registerMessage(EntityFixMessageHandler.class, EntityFixMessage.class, 0, Side.CLIENT);
        controlNetwork.registerMessage(HovercraftControllerGUIInputHandler.class, HovercraftControllerGUIInputMessage.class, 1, Side.SERVER);
        controlNetwork.registerMessage(PilotControlsMessageHandler.class, PilotControlsMessage.class, 2, Side.SERVER);
        controlNetwork.registerMessage(MessageStartPilotingHandler.class, MessageStartPiloting.class, 3, Side.CLIENT);
        controlNetwork.registerMessage(MessageStopPilotingHandler.class, MessageStopPiloting.class, 4, Side.CLIENT);
        controlNetwork.registerMessage(MessagePlayerStoppedPilotingHandler.class, MessagePlayerStoppedPiloting.class, 5, Side.SERVER);
    }

    public void registerCapibilities(FMLStateEvent event) {
        CapabilityManager.INSTANCE.register(ICapabilityLastRelay.class, new StorageLastRelay(), ImplCapabilityLastRelay.class);
    }

}
