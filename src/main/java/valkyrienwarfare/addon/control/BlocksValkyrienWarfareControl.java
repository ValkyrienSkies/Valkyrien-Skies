package valkyrienwarfare.addon.control;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.block.BlockAirshipController_Zepplin;
import valkyrienwarfare.addon.control.block.BlockBalloonBurner;
import valkyrienwarfare.addon.control.block.BlockDopedEtherium;
import valkyrienwarfare.addon.control.block.BlockGyroscope;
import valkyrienwarfare.addon.control.block.BlockHovercraftController;
import valkyrienwarfare.addon.control.block.BlockLiftValve;
import valkyrienwarfare.addon.control.block.BlockShipHelm;
import valkyrienwarfare.addon.control.block.BlockShipHullSealer;
import valkyrienwarfare.addon.control.block.BlockShipPassengerChair;
import valkyrienwarfare.addon.control.block.BlockShipPilotsChair;
import valkyrienwarfare.addon.control.block.BlockShipTelegraph;
import valkyrienwarfare.addon.control.block.BlockShipWheel;
import valkyrienwarfare.addon.control.block.BlockThrustModulator;
import valkyrienwarfare.addon.control.block.BlockThrustRelay;
import valkyrienwarfare.addon.control.block.engine.BlockNormalEngine;
import valkyrienwarfare.addon.control.block.engine.BlockRedstoneEngine;
import valkyrienwarfare.addon.control.block.ethercompressor.BlockCreativeEtherCompressor;
import valkyrienwarfare.addon.control.block.ethercompressor.BlockNormalEtherCompressor;

public class BlocksValkyrienWarfareControl {

    private final ValkyrienWarfareControl mod_vwcontrol;

    public Block basicEngine;
    public Block advancedEngine;
    public Block eliteEngine;
    public Block ultimateEngine; // Couldn't think of what to name these, so I went with the Mekanism naming
                                 // style
    public Block redstoneEngine;
    public Block basicHoverController;
    public Block dopedEtherium;
    public Block balloonBurner;
    public Block pilotsChair;
    public Block passengerChair;
    public Block shipHelm;
    public Block shipWheel;
    public Block shipTelegraph;
    public Block antigravityEngine; // leaving it with the old name to prevent blocks disappearing
    public Block advancedEtherCompressor;
    public Block eliteEtherCompressor;
    public Block ultimateEtherCompressor;
    public Block creativeEtherCompressor;
    public Block thrustRelay;
    public Block thrustModulator;
    public Block airshipController_zepplin;
    public Block shipHullSealer;
    public Block gyroscope;
    public Block liftValve;

    public BlocksValkyrienWarfareControl(ValkyrienWarfareControl mod_vwcontrol) {
        this.mod_vwcontrol = mod_vwcontrol;
    }

    protected void registerBlocks(RegistryEvent.Register<Block> event) {
        double basicEnginePower = getConfig()
                .get(Configuration.CATEGORY_GENERAL, "basicEnginePower", 4000D, "engine power for the basic engine")
                .getDouble();
        double advancedEnginePower = getConfig().get(Configuration.CATEGORY_GENERAL, "advancedEnginePower", 6000D,
                "engine power for the advanced engine").getDouble();
        double eliteEnginePower = getConfig()
                .get(Configuration.CATEGORY_GENERAL, "eliteEnginePower", 8000D, "engine power for the elite engine")
                .getDouble();
        double ultimateEnginePower = getConfig().get(Configuration.CATEGORY_GENERAL, "ultimateEnginePower", 16000D,
                "engine power for the ultimate engine").getDouble();
        double redstoneEnginePower = getConfig().get(Configuration.CATEGORY_GENERAL, "redstoneEnginePower", 500D,
                "Multiplied by the redstone power (0-15) to the Redstone engine").getDouble();

        double basicEtherCompressorPower = getConfig().get(Configuration.CATEGORY_GENERAL, "basicEtherCompressorPower",
                25000D, "engine power for the basic Ether Compressor").getDouble();
        double advancedEtherCompressorPower = getConfig().get(Configuration.CATEGORY_GENERAL,
                "advancedEtherCompressorPower", 45000D, "engine power for the advanced Ether Compressor").getDouble();
        double eliteEtherCompressorPower = getConfig().get(Configuration.CATEGORY_GENERAL, "eliteEtherCompressorPower",
                80000D, "engine power for the elite Ether Compressor").getDouble();
        double ultimateEtherCompressorPower = getConfig().get(Configuration.CATEGORY_GENERAL,
                "ultimateEtherCompressorPower", 100000D, "engine power for the ultimate Ether Compressor").getDouble();

        basicEngine = new BlockNormalEngine(Material.WOOD, basicEnginePower).setHardness(5f)
                .setUnlocalizedName("basicengine").setRegistryName(getModID(), "basicengine")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        advancedEngine = new BlockNormalEngine(Material.ROCK, advancedEnginePower).setHardness(6f)
                .setUnlocalizedName("advancedengine").setRegistryName(getModID(), "advancedengine")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        eliteEngine = new BlockNormalEngine(Material.IRON, eliteEnginePower).setHardness(8f)
                .setUnlocalizedName("eliteengine").setRegistryName(getModID(), "eliteengine")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        ultimateEngine = new BlockNormalEngine(Material.GROUND, ultimateEnginePower).setHardness(10f)
                .setUnlocalizedName("ultimateengine").setRegistryName(getModID(), "ultimateengine")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        redstoneEngine = new BlockRedstoneEngine(Material.REDSTONE_LIGHT, redstoneEnginePower).setHardness(7.0f)
                .setUnlocalizedName("redstoneengine").setRegistryName(getModID(), "redstoneengine")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);

        antigravityEngine = new BlockNormalEtherCompressor(Material.WOOD, basicEtherCompressorPower).setHardness(8f)
                .setUnlocalizedName("antigravengine").setRegistryName(getModID(), "antigravengine")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        advancedEtherCompressor = new BlockNormalEtherCompressor(Material.ROCK, advancedEtherCompressorPower)
                .setHardness(8f).setUnlocalizedName("advancedethercompressor")
                .setRegistryName(getModID(), "advancedethercompressor").setCreativeTab(ValkyrienWarfareMod.vwTab);
        eliteEtherCompressor = new BlockNormalEtherCompressor(Material.IRON, eliteEtherCompressorPower).setHardness(8f)
                .setUnlocalizedName("eliteethercompressor").setRegistryName(getModID(), "eliteethercompressor")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        ultimateEtherCompressor = new BlockNormalEtherCompressor(Material.GROUND, ultimateEtherCompressorPower)
                .setHardness(8f).setUnlocalizedName("ultimateethercompressor")
                .setRegistryName(getModID(), "ultimateethercompressor").setCreativeTab(ValkyrienWarfareMod.vwTab);
        creativeEtherCompressor = new BlockCreativeEtherCompressor(Material.BARRIER, Double.MAX_VALUE / 4)
                .setHardness(0.0f).setUnlocalizedName("creativeethercompressor")
                .setRegistryName(getModID(), "creativeethercompressor").setCreativeTab(ValkyrienWarfareMod.vwTab);

        basicHoverController = new BlockHovercraftController(Material.IRON).setHardness(10f)
                .setUnlocalizedName("basichovercraftcontroller")
                .setRegistryName(getModID(), "basichovercraftcontroller").setCreativeTab(ValkyrienWarfareMod.vwTab);
        dopedEtherium = new BlockDopedEtherium(Material.GLASS).setHardness(4f).setUnlocalizedName("dopedetherium")
                .setRegistryName(getModID(), "dopedetherium").setCreativeTab(ValkyrienWarfareMod.vwTab);
        balloonBurner = new BlockBalloonBurner(Material.IRON).setHardness(4f).setUnlocalizedName("balloonburner")
                .setRegistryName(getModID(), "balloonburner").setCreativeTab(ValkyrienWarfareMod.vwTab);
        pilotsChair = new BlockShipPilotsChair(Material.IRON).setHardness(4f).setUnlocalizedName("shippilotschair")
                .setRegistryName(getModID(), "shippilotschair").setCreativeTab(ValkyrienWarfareMod.vwTab);

        passengerChair = new BlockShipPassengerChair(Material.IRON).setHardness(4f)
                .setUnlocalizedName("shippassengerchair").setRegistryName(getModID(), "shippassengerchair")
                .setCreativeTab(ValkyrienWarfareMod.vwTab);
        shipHelm = new BlockShipHelm(Material.WOOD).setHardness(4f).setUnlocalizedName("shiphelm")
                .setRegistryName(getModID(), "shiphelm").setCreativeTab(ValkyrienWarfareMod.vwTab);
        shipWheel = new BlockShipWheel(Material.WOOD).setHardness(5f).setUnlocalizedName("shiphelmwheel")
                .setRegistryName(getModID(), "shiphelmwheel");
        shipTelegraph = new BlockShipTelegraph(Material.WOOD).setHardness(5f).setUnlocalizedName("shiptelegraph")
                .setRegistryName(getModID(), "shiptelegraph").setCreativeTab(ValkyrienWarfareMod.vwTab);

        thrustRelay = new BlockThrustRelay(Material.IRON).setHardness(5f).setUnlocalizedName("thrustrelay")
                .setRegistryName(getModID(), "thrustrelay").setCreativeTab(ValkyrienWarfareMod.vwTab);
        thrustModulator = new BlockThrustModulator(Material.IRON).setHardness(8f).setUnlocalizedName("thrustmodulator")
                .setRegistryName(getModID(), "thrustmodulator").setCreativeTab(ValkyrienWarfareMod.vwTab);

        shipHullSealer = new BlockShipHullSealer(Material.IRON).setHardness(5f).setUnlocalizedName("shiphullsealer")
                .setRegistryName(getModID(), "shiphullsealer").setCreativeTab(ValkyrienWarfareMod.vwTab);

        airshipController_zepplin = new BlockAirshipController_Zepplin(Material.WOOD).setHardness(5f)
                .setUnlocalizedName("airshipcontroller_zepplin")
                .setRegistryName(getModID(), "airshipcontroller_zepplin").setCreativeTab(ValkyrienWarfareMod.vwTab);

        gyroscope = new BlockGyroscope(Material.IRON).setHardness(5f).setUnlocalizedName("vw_gyroscope")
                .setRegistryName(getModID(), "vw_gyroscope").setCreativeTab(ValkyrienWarfareMod.vwTab);

        liftValve = new BlockLiftValve(Material.IRON).setHardness(7f).setUnlocalizedName("vw_liftvalve")
                .setRegistryName(getModID(), "vw_liftvalve").setCreativeTab(ValkyrienWarfareMod.vwTab);
        
        event.getRegistry().register(basicEngine);
        event.getRegistry().register(advancedEngine);
        event.getRegistry().register(eliteEngine);
        event.getRegistry().register(ultimateEngine);
        event.getRegistry().register(redstoneEngine);

        event.getRegistry().register(antigravityEngine);
        event.getRegistry().register(advancedEtherCompressor);
        event.getRegistry().register(eliteEtherCompressor);
        event.getRegistry().register(ultimateEtherCompressor);
        event.getRegistry().register(creativeEtherCompressor);

        event.getRegistry().register(basicHoverController);
        event.getRegistry().register(dopedEtherium);
        event.getRegistry().register(balloonBurner);
        event.getRegistry().register(pilotsChair);
        event.getRegistry().register(passengerChair);

        event.getRegistry().register(shipHelm);
        event.getRegistry().register(shipWheel);
        event.getRegistry().register(shipTelegraph);
        event.getRegistry().register(thrustRelay);
        event.getRegistry().register(thrustModulator);

        event.getRegistry().register(shipHullSealer);
        event.getRegistry().register(airshipController_zepplin);
        event.getRegistry().register(gyroscope);
        event.getRegistry().register(liftValve);
    }

    protected void registerBlockItems(RegistryEvent.Register<Item> event) {
        registerItemBlock(event, basicEngine);
        registerItemBlock(event, advancedEngine);
        registerItemBlock(event, eliteEngine);
        registerItemBlock(event, ultimateEngine);
        registerItemBlock(event, redstoneEngine);

        registerItemBlock(event, antigravityEngine);
        registerItemBlock(event, advancedEtherCompressor);
        registerItemBlock(event, eliteEtherCompressor);
        registerItemBlock(event, ultimateEtherCompressor);
        registerItemBlock(event, creativeEtherCompressor);

        registerItemBlock(event, basicHoverController);
        registerItemBlock(event, dopedEtherium);
        registerItemBlock(event, balloonBurner);
        registerItemBlock(event, pilotsChair);
        registerItemBlock(event, passengerChair);

        registerItemBlock(event, shipHelm);
        registerItemBlock(event, shipWheel);
        registerItemBlock(event, shipTelegraph);
        registerItemBlock(event, thrustRelay);
        registerItemBlock(event, thrustModulator);

        registerItemBlock(event, shipHullSealer);
        registerItemBlock(event, airshipController_zepplin);
        registerItemBlock(event, gyroscope);
        registerItemBlock(event, liftValve);
    }

    private void registerItemBlock(RegistryEvent.Register<Item> event, Block block) {
        mod_vwcontrol.registerItemBlock(event, block);
    }

    private Configuration getConfig() {
        return mod_vwcontrol.config;
    }

    private String getModID() {
        return mod_vwcontrol.getModID();
    }

}
