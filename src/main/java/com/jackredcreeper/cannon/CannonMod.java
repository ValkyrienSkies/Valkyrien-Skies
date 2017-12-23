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

package com.jackredcreeper.cannon;

import com.jackredcreeper.cannon.init.ModBlocks;
import com.jackredcreeper.cannon.init.ModItems;
import com.jackredcreeper.cannon.proxy.CommonProxy;
import com.jackredcreeper.cannon.tileentity.TileEntityCannon;
import com.jackredcreeper.cannon.world.ExplosionHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = CannonModReference.MOD_ID, version = CannonModReference.MOD_ID, acceptedMinecraftVersions = "[1.11.2]")
public class CannonMod {

	@Instance(CannonModReference.MOD_ID)
	public static CannonMod instance = new CannonMod();

	@SidedProxy(clientSide = CannonModReference.CLIENT, serverSide = CannonModReference.SERVER)
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModItems.init();
		ModItems.register();

		ModBlocks.init();
		ModBlocks.register();

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();

		MinecraftForge.EVENT_BUS.register(new ExplosionHandler());

		//Craft

		GameRegistry.registerTileEntity(TileEntityCannon.class, CannonModReference.MOD_ID + "TileEntityCannon");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}


}
