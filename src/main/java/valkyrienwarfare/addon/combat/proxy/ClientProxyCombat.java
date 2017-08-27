package valkyrienwarfare.addon.combat.proxy;

import valkyrienwarfare.addon.combat.entity.EntityCannonBall;
import valkyrienwarfare.addon.combat.entity.EntityCannonBasic;
import valkyrienwarfare.addon.combat.render.EntityCannonBasicRenderFactory;
import valkyrienwarfare.addon.combat.ValkyrienWarfareCombat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLStateEvent;

public class ClientProxyCombat extends CommonProxyCombat {

	@Override
	public void preInit(FMLStateEvent e) {
		super.preInit(e);
		OBJLoader.INSTANCE.addDomain(ValkyrienWarfareCombat.INSTANCE.getModID().toLowerCase());
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
		registerItemModel(ValkyrienWarfareCombat.INSTANCE.basicCannonSpawner);
		registerItemModel(ValkyrienWarfareCombat.INSTANCE.cannonBall);
		registerItemModel(ValkyrienWarfareCombat.INSTANCE.powderPouch);
		registerItemModel(ValkyrienWarfareCombat.INSTANCE.explosiveArrow);
	}

	private void registerItemModel(Item toRegister) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(toRegister, 0, new ModelResourceLocation(ValkyrienWarfareCombat.INSTANCE.getModID() + ":" + toRegister.getUnlocalizedName().substring(5), "inventory"));
		;
	}
}
