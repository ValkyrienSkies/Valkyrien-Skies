package ValkyrienWarfareWorld;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareWorld.Block.BlockEtheriumOre;
import ValkyrienWarfareWorld.Block.BlockQuartzFence;
import ValkyrienWarfareWorld.Block.BlockSkyTempleController;
import ValkyrienWarfareWorld.Proxy.CommonProxyWorld;
import ValkyrienWarfareWorld.TileEntity.TileEntitySkyTempleController;
import ValkyrienWarfareWorld.WorldGen.ValkyrienWarfareWorldGen;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = ValkyrienWarfareWorldMod.MODID, name = ValkyrienWarfareWorldMod.MODNAME, version = ValkyrienWarfareWorldMod.MODVER)
public class ValkyrienWarfareWorldMod {

    public static final String MODID = "valkyrienwarfareworld";
    public static final String MODNAME = "Valkyrien Warfare World";
    public static final String MODVER = "0.9_alpha";
    private static final WorldEventsCommon worldEventsCommon = new WorldEventsCommon();
    @SidedProxy(clientSide = "ValkyrienWarfareWorld.Proxy.ClientProxyWorld", serverSide = "ValkyrienWarfareWorld.Proxy.CommonProxyWorld")
    public static CommonProxyWorld proxy;
    @Instance(MODID)
    public static ValkyrienWarfareWorldMod instance = new ValkyrienWarfareWorldMod();
    public Block etheriumOre;
    public Block skydungeon_controller;
    public Block quartz_fence;
    public Item etheriumCrystal;

    private static void registerItemBlock(Block block) {
        GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        registerBlocks(event);
        registerItems(event);
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "FallingUpBlockEntity"), EntityFallingUpBlock.class, "FallingUpBlockEntity", 75, this, 80, 1, true);
        MinecraftForge.EVENT_BUS.register(worldEventsCommon);
        registerTileEntities(event);
        proxy.init(event);

        GameRegistry.registerWorldGenerator(new ValkyrienWarfareWorldGen(), 1);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    private void registerBlocks(FMLStateEvent event) {
        etheriumOre = new BlockEtheriumOre(Material.ROCK).setHardness(3f).setUnlocalizedName("etheriumore").setRegistryName(MODID, "etheriumore").setCreativeTab(ValkyrienWarfareMod.vwTab);
        skydungeon_controller = new BlockSkyTempleController(Material.GLASS).setHardness(15f).setUnlocalizedName("skydungeon_controller").setRegistryName(MODID, "skydungeon_controller").setCreativeTab(ValkyrienWarfareMod.vwTab);
        quartz_fence = new BlockQuartzFence(Material.GLASS).setHardness(8f).setUnlocalizedName("quartz_fence").setRegistryName(MODID, "quartz_fence").setCreativeTab(ValkyrienWarfareMod.vwTab);

        GameRegistry.register(etheriumOre);
        GameRegistry.register(skydungeon_controller);
        GameRegistry.register(quartz_fence);

        registerItemBlock(etheriumOre);
        registerItemBlock(skydungeon_controller);
        registerItemBlock(quartz_fence);
    }

    private void registerItems(FMLStateEvent event) {
        etheriumCrystal = new ItemEtheriumCrystal().setUnlocalizedName("etheriumcrystal").setRegistryName(MODID, "etheriumcrystal").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(16);

        GameRegistry.register(etheriumCrystal);
    }

    private void registerTileEntities(FMLStateEvent event) {
        GameRegistry.registerTileEntity(TileEntitySkyTempleController.class, "skydungeon_controller");
    }

}