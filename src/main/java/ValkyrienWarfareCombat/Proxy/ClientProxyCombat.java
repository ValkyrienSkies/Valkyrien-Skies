package ValkyrienWarfareCombat.Proxy;

import ValkyrienWarfareCombat.Entity.EntityCannonBall;
import ValkyrienWarfareCombat.Entity.EntityCannonBasic;
import ValkyrienWarfareCombat.Render.EntityCannonBasicRenderFactory;
import ValkyrienWarfareCombat.ValkyrienWarfareCombatMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;

public class ClientProxyCombat extends CommonProxyCombat {

	@Override
	public void preInit(FMLStateEvent e) {
		super.preInit(e);
		OBJLoader.INSTANCE.addDomain(ValkyrienWarfareCombatMod.INSTANCE.getModID().toLowerCase());
		RenderingRegistry.registerEntityRenderingHandler(EntityCannonBasic.class, new EntityCannonBasicRenderFactory());
		RenderingRegistry.registerEntityRenderingHandler(EntityCannonBall.class, new EntityCannonBasicRenderFactory.EntityCannonBallRenderFactory());
	}

	@Override
	public void init(FMLStateEvent e) {
		super.init(e);
	}

	@Override
	public void postInit(FMLStateEvent e) {
		super.postInit(e);
		registerItemModel(ValkyrienWarfareCombatMod.INSTANCE.basicCannonSpawner);
		registerItemModel(ValkyrienWarfareCombatMod.INSTANCE.cannonBall);
		registerItemModel(ValkyrienWarfareCombatMod.INSTANCE.powderPouch);
		registerItemModel(ValkyrienWarfareCombatMod.INSTANCE.explosiveArrow);
	}

	private void registerItemModel(Item toRegister) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(toRegister, 0, new ModelResourceLocation(ValkyrienWarfareCombatMod.INSTANCE.getModID() + ":" + toRegister.getUnlocalizedName().substring(5), "inventory"));
		;
	}
}
