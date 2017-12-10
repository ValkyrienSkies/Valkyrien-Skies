package valkyrienwarfare.api.addons;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import valkyrienwarfare.ValkyrienWarfareMod;

public abstract class Module<ImplName> {
	private String name;
	private boolean registeredStuffPreInit = false, registeredStuffInit = false;
	private ModuleProxy common, client, server; //tODO: call these
	private String modid;
	
	public Module(String name, ModuleProxy common, ModuleProxy client, ModuleProxy server)  {
		this(name, common, ValkyrienWarfareMod.MODID);
	}
	
	public Module(String name, ModuleProxy common, String modid)  {
		this.name = name;
		this.common = common;
		this.modid = modid;
	}
	
	public final void setServerProxy(ModuleProxy server) {
		this.server = server;
	}
	
	public final void setClientProxy(ModuleProxy client) {
		this.client = client;
	}
	
	public final void doRegisteringStuffPreInit()  {
		if (!registeredStuffPreInit)   {
			registerBlocks();
			registerItems();
			registerEntities();
			registerCapabilities();
			registeredStuffPreInit = true;
		}
	}
	
	public final void doRegisteringStuffInit()  {
		if (!registeredStuffInit)   {
			registerTileEntities();
			registerRecipes();
			registerNetworks();
			registeredStuffInit = true;
		}
	}
	
	protected void registerItems()  {
		
	}
	
	protected void registerBlocks() {
		
	}
	
	protected void registerRecipes()    {
		
	}
	
	protected void registerEntities()   {
		
	}
	
	protected void registerTileEntities()   {
		
	}
	
	protected void registerNetworks()   {
		
	}
	
	protected void registerCapabilities()   {
		
	}
	
	public final ModuleProxy getClientProxy()   {
		return client;
	}
	
	public final ModuleProxy getServerProxy()   {
		return server;
	}
	
	public final ModuleProxy getCommonProxy()   {
		return common;
	}
	
	public abstract void preInit(FMLStateEvent event);
	
	public abstract void init(FMLStateEvent event);
	
	public abstract void postInit(FMLStateEvent event);
	
	protected final void registerBlock(Block b)   {
		ValkyrienWarfareMod.registerBlock(b);
	}
	
	protected final void registerItem(Item i)   {
		GameRegistry.register(i);
	}
	
	public final String getModID()  {
		return modid;
	}
}
