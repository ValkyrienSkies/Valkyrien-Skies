package ValkyrienWarfareCombat.Proxy;

import ValkyrienWarfareCombat.ValkyrienWarfareCombatMod;
import ValkyrienWarfareCombat.Entity.EntityCannonBall;
import ValkyrienWarfareCombat.Entity.EntityCannonBasic;
import ValkyrienWarfareCombat.Render.EntityCannonBasicRenderFactory;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxyCombat extends CommonProxyCombat{

	@Override
	public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);
    	OBJLoader.INSTANCE.addDomain(ValkyrienWarfareCombatMod.MODID.toLowerCase());
    	RenderingRegistry.registerEntityRenderingHandler(EntityCannonBasic.class,new EntityCannonBasicRenderFactory());
    	RenderingRegistry.registerEntityRenderingHandler(EntityCannonBall.class,new EntityCannonBasicRenderFactory.EntityCannonBallRenderFactory());
    }

	@Override
    public void init(FMLInitializationEvent e) {
		super.init(e);
    }

	@Override
    public void postInit(FMLPostInitializationEvent e) {
		super.postInit(e);
    }
}
