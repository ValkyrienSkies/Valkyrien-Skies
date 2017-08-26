package ValkyrienWarfareBase.Proxy;

import ValkyrienWarfareBase.API.Addons.Module;
import ValkyrienWarfareBase.API.Addons.ModuleProxy;
import ValkyrienWarfareBase.ChunkManagement.DimensionPhysicsChunkManager;
import ValkyrienWarfareBase.EventsCommon;
import ValkyrienWarfareBase.PhysicsManagement.DimensionPhysObjectManager;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent e) {
		for (Module addon : ValkyrienWarfareMod.addons) {
			ModuleProxy proxy = addon.getCommonProxy();
			if (proxy != null)  {
				proxy.preInit(e);
			}
		}
	}
	
	public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(new EventsCommon());
		ValkyrienWarfareMod.chunkManager = new DimensionPhysicsChunkManager();
		ValkyrienWarfareMod.physicsManager = new DimensionPhysObjectManager();
		
		for (Module addon : ValkyrienWarfareMod.addons) {
			ModuleProxy proxy = addon.getCommonProxy();
			if (proxy != null)  {
				proxy.init(e);
			}
		}
	}
	
	public void postInit(FMLPostInitializationEvent e) {
		for (Module addon : ValkyrienWarfareMod.addons) {
			ModuleProxy proxy = addon.getCommonProxy();
			if (proxy != null)  {
				proxy.postInit(e);
			}
		}
	}
	
	public void updateShipPartialTicks(PhysicsWrapperEntity wrapper) {
		
	}
	
	public void registerCommands(MinecraftServer server) {
		
	}
	
}
