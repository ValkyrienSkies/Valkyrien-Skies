package ValkyrienWarfareCombat;

import ValkyrienWarfareBase.API.Addons.Module;
import ValkyrienWarfareBase.API.Addons.VWAddon;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareCombat.Entity.EntityCannonBall;
import ValkyrienWarfareCombat.Entity.EntityCannonBasic;
import ValkyrienWarfareCombat.Item.ItemBasicCannon;
import ValkyrienWarfareCombat.Item.ItemCannonBall;
import ValkyrienWarfareCombat.Item.ItemExplosiveArrow;
import ValkyrienWarfareCombat.Item.ItemPowderPouch;
import ValkyrienWarfareCombat.Proxy.ClientProxyCombat;
import ValkyrienWarfareCombat.Proxy.CommonProxyCombat;
import ValkyrienWarfareWorld.ValkyrienWarfareWorldMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@VWAddon
public class ValkyrienWarfareCombatMod extends Module<ValkyrienWarfareCombatMod> {
	public static ValkyrienWarfareCombatMod INSTANCE;
	
	public ValkyrienWarfareCombatMod()  {
		super("VW_Combat", new CommonProxyCombat(), new ClientProxyCombat(), null, "valkyrienwarfarecombat");
	}
	
	@Override
	public void initModule()    {
		INSTANCE = this;
	}

	public Item basicCannonSpawner;
	public Item cannonBall;
	public Item powderPouch;
	public Item explosiveArrow;

	public Block fakecannonblock;

	@Override
	public void preInit(FMLStateEvent event) {
	}

	@Override
	public void init(FMLStateEvent event) {
	}

	@Override
	public void postInit(FMLStateEvent event) {
	}

	@Override
	protected void registerItems() {
		basicCannonSpawner = new ItemBasicCannon().setUnlocalizedName("basiccannonspawner").setRegistryName(getModID(), "basiccannonspawner").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(4);
		cannonBall = new ItemCannonBall().setUnlocalizedName("turretcannonball").setRegistryName(getModID(), "turretcannonball").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(32);
		powderPouch = new ItemPowderPouch().setUnlocalizedName("powderpouch").setRegistryName(getModID(), "powderpouch").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(32);
		explosiveArrow = new ItemExplosiveArrow().setUnlocalizedName("explosivearrow").setRegistryName(getModID(), "explosivearrow").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(64);

		GameRegistry.register(basicCannonSpawner);
		GameRegistry.register(cannonBall);
		GameRegistry.register(powderPouch);
		GameRegistry.register(explosiveArrow);
	}
	
	@Override
	protected void registerEntities() {
		EntityRegistry.registerModEntity(new ResourceLocation(getModID(), "EntityCannonBasic"), EntityCannonBasic.class, "EntityCannonBasic", 71, ValkyrienWarfareMod.instance, 120, 1, false);
		EntityRegistry.registerModEntity(new ResourceLocation(getModID(), "EntityCannonBall"), EntityCannonBall.class, "EntityCannonBall", 72, ValkyrienWarfareMod.instance, 120, 5, true);
	}
	
	@Override
	protected void registerBlocks() {
		fakecannonblock = new FakeCannonBlock(Material.IRON).setHardness(5f).setUnlocalizedName("fakecannonblock").setRegistryName(getModID(), "fakecannonblock");

		GameRegistry.register(fakecannonblock);
	}
	
	@Override
	protected void registerRecipes() {
		GameRegistry.addRecipe(new ItemStack(cannonBall, 4), new Object[]{"II ", "II ", "   ", 'I', Items.IRON_INGOT});
		GameRegistry.addRecipe(new ItemStack(powderPouch, 4), new Object[]{" S ", "SGS", " S ", 'S', Items.STRING, 'G', Items.GUNPOWDER});
	}

}
