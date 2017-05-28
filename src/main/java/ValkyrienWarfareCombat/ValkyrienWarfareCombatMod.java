package ValkyrienWarfareCombat;

import ValkyrienWarfareCombat.Entity.EntityCannonBall;
import ValkyrienWarfareCombat.Entity.EntityCannonBasic;
import ValkyrienWarfareCombat.Item.ItemBasicCannon;
import ValkyrienWarfareCombat.Item.ItemCannonBall;
import ValkyrienWarfareCombat.Item.ItemExplosiveArrow;
import ValkyrienWarfareCombat.Item.ItemPowderPouch;
import ValkyrienWarfareCombat.Proxy.CommonProxyCombat;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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

@Mod(modid = ValkyrienWarfareCombatMod.MODID, name = ValkyrienWarfareCombatMod.MODNAME, version = ValkyrienWarfareCombatMod.MODVER)
public class ValkyrienWarfareCombatMod {

	@SidedProxy(clientSide = "ValkyrienWarfareCombat.Proxy.ClientProxyCombat", serverSide = "ValkyrienWarfareCombat.Proxy.CommonProxyCombat")
	public static CommonProxyCombat proxy;

	public static final String MODID = "valkyrienwarfarecombat";
	public static final String MODNAME = "Valkyrien Warfare Combat";
	public static final String MODVER = "0.9_alpha";

	@Instance(MODID)
	public static ValkyrienWarfareCombatMod instance = new ValkyrienWarfareCombatMod();

	public Item basicCannonSpawner;
	public Item cannonBall;
	public Item powderPouch;
	public Item explosiveArrow;

	public Block fakecannonblock;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		registerBlocks(event);
		registerItems(event);
		registerRecipies(event);
		registerEntities(event);
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	private void registerItems(FMLStateEvent event) {
		basicCannonSpawner = new ItemBasicCannon().setUnlocalizedName("basiccannonspawner").setRegistryName(MODID, "basiccannonspawner").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(4);
		cannonBall = new ItemCannonBall().setUnlocalizedName("cannonball").setRegistryName(MODID, "cannonball").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(32);
		powderPouch = new ItemPowderPouch().setUnlocalizedName("powderpouch").setRegistryName(MODID, "powderpouch").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(32);
		explosiveArrow = new ItemExplosiveArrow().setUnlocalizedName("explosivearrow").setRegistryName(MODID, "explosivearrow").setCreativeTab(CreativeTabs.COMBAT).setMaxStackSize(64);

		GameRegistry.register(basicCannonSpawner);
		GameRegistry.register(cannonBall);
		GameRegistry.register(powderPouch);
		GameRegistry.register(explosiveArrow);
	}

	private void registerEntities(FMLStateEvent event) {
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "EntityCannonBasic"), EntityCannonBasic.class, "EntityCannonBasic", 71, this, 120, 1, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "EntityCannonBall"), EntityCannonBall.class, "EntityCannonBall", 72, this, 120, 5, true);
	}

	private void registerBlocks(FMLStateEvent event) {
		fakecannonblock = new FakeCannonBlock(Material.IRON).setHardness(5f).setUnlocalizedName("fakecannonblock").setRegistryName(MODID, "fakecannonblock").setCreativeTab(CreativeTabs.REDSTONE);

		GameRegistry.register(fakecannonblock);
	}

	private void registerRecipies(FMLStateEvent event) {
		GameRegistry.addRecipe(new ItemStack(cannonBall, 4), new Object[] { "II ", "II ", "   ", 'I', Items.IRON_INGOT });
		GameRegistry.addRecipe(new ItemStack(powderPouch, 4), new Object[] { " S ", "SGS", " S ", 'S', Items.STRING, 'G', Items.GUNPOWDER });
	}

}
