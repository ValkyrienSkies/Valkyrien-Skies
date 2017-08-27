package valkyrienwarfare.addon.world;

import valkyrienwarfare.addon.world.block.BlockEtheriumOre;
import valkyrienwarfare.addon.world.block.BlockQuartzFence;
import valkyrienwarfare.addon.world.proxy.ClientProxyWorld;
import valkyrienwarfare.addon.world.tileentity.TileEntitySkyTempleController;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.world.block.BlockSkyTempleController;
import valkyrienwarfare.addon.world.proxy.CommonProxyWorld;
import valkyrienwarfare.addon.world.worldgen.ValkyrienWarfareWorldGen;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ValkyrienWarfareWorld extends Module<ValkyrienWarfareWorldGen> {
	
	public ValkyrienWarfareWorld()   {
		super("VW_World", new CommonProxyWorld(), new ClientProxyWorld(), null, "valkyrienwarfareworld");
	}
	private static final WorldEventsCommon worldEventsCommon = new WorldEventsCommon();
	public static ValkyrienWarfareWorld INSTANCE;
	public Block etheriumOre;
	public Block skydungeon_controller;
	public Block quartz_fence;
	public Item etheriumCrystal;
	
	@Override
	public void initModule()    {
		INSTANCE = this;
	}

	private static void registerItemBlock(Block block) {
		GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
	}

	@Override
	public void preInit(FMLStateEvent event) {
	}

	@Override
	public void init(FMLStateEvent event) {
		EntityRegistry.registerModEntity(new ResourceLocation(getModID(), "FallingUpBlockEntity"), EntityFallingUpBlock.class, "FallingUpBlockEntity", 75, this, 80, 1, true);
		MinecraftForge.EVENT_BUS.register(worldEventsCommon);

		GameRegistry.registerWorldGenerator(new ValkyrienWarfareWorldGen(), 1);
	}

	@Override
	public void postInit(FMLStateEvent event) {
	}
	
	@Override
	protected void registerBlocks() {
		etheriumOre = new BlockEtheriumOre(Material.ROCK).setHardness(3f).setUnlocalizedName("etheriumore").setRegistryName(getModID(), "etheriumore").setCreativeTab(ValkyrienWarfareMod.vwTab);
		skydungeon_controller = new BlockSkyTempleController(Material.GLASS).setHardness(15f).setUnlocalizedName("skydungeon_controller").setRegistryName(getModID(), "skydungeon_controller").setCreativeTab(ValkyrienWarfareMod.vwTab);
		quartz_fence = new BlockQuartzFence(Material.GLASS).setHardness(8f).setUnlocalizedName("quartz_fence").setRegistryName(getModID(), "quartz_fence").setCreativeTab(ValkyrienWarfareMod.vwTab);

		GameRegistry.register(etheriumOre);
		GameRegistry.register(skydungeon_controller);
		GameRegistry.register(quartz_fence);

		registerItemBlock(etheriumOre);
		registerItemBlock(skydungeon_controller);
		registerItemBlock(quartz_fence);
	}
	
	@Override
	protected void registerItems() {
		etheriumCrystal = new ItemEtheriumCrystal().setUnlocalizedName("etheriumcrystal").setRegistryName(getModID(), "etheriumcrystal").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(16);

		GameRegistry.register(etheriumCrystal);
	}
	
	@Override
	protected void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEntitySkyTempleController.class, "skydungeon_controller");
	}

}