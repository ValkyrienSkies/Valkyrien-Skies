/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.api.addons;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import valkyrienwarfare.ValkyrienWarfareMod;

public abstract class Module<ImplName> {
	private String name;
	private boolean donePreInit = false, doneInit = false, donePostInit = false;
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
	
	public final void doPreInit(FMLStateEvent event)  {
		if (!donePreInit)   {
			setupConfig();
			registerBlocks();
			registerItems();
			registerEntities();
			registerCapabilities();
			preInit(event);
			donePreInit = true;
		}
	}
	
	public final void doInit(FMLStateEvent event)  {
		if (!doneInit)   {
			registerTileEntities();
			registerRecipes();
			registerNetworks();
			init(event);
			doneInit = true;
		}
	}

	public final void doPostInit(FMLStateEvent event)	{
		if (!donePostInit)	{
			postInit(event);
			donePostInit = true;
		}
	}

	protected void setupConfig()	{

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

	protected abstract void preInit(FMLStateEvent event);

	protected abstract void init(FMLStateEvent event);

	protected abstract void postInit(FMLStateEvent event);
	
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
