package ValkyrienWarfareCombat.Proxy;

import ValkyrienWarfareCombat.ValkyrienWarfareCombatMod;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

public class ClientProxyCombat extends CommonProxyCombat{

	@Override
	public void preInit(FMLPreInitializationEvent e) {
    	OBJLoader.INSTANCE.addDomain(ValkyrienWarfareCombatMod.MODID.toLowerCase());
    }

	@Override
    public void init(FMLInitializationEvent e) {

    }

	@Override
    public void postInit(FMLPostInitializationEvent e) {

    }
}
