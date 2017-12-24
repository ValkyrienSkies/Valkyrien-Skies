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

package valkyrienwarfare.addon.world;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.world.block.BlockEtheriumOre;
import valkyrienwarfare.addon.world.block.BlockQuartzFence;
import valkyrienwarfare.addon.world.block.BlockSkyTempleController;
import valkyrienwarfare.addon.world.proxy.ClientProxyWorld;
import valkyrienwarfare.addon.world.proxy.CommonProxyWorld;
import valkyrienwarfare.addon.world.tileentity.TileEntitySkyTempleController;
import valkyrienwarfare.addon.world.worldgen.ValkyrienWarfareWorldGen;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.api.addons.VWAddon;

@VWAddon
public class ValkyrienWarfareWorld extends Module<ValkyrienWarfareWorldGen> {
	
	public ValkyrienWarfareWorld()   {
		super("VW_World", new CommonProxyWorld(), "valkyrienwarfareworld");
		if(ValkyrienWarfareMod.INSTANCE.isRunningOnClient()) {
			this.setClientProxy(new ClientProxyWorld());
		}
		INSTANCE = this;
	}
	private static final WorldEventsCommon worldEventsCommon = new WorldEventsCommon();
	public static ValkyrienWarfareWorld INSTANCE;
	public static Block etheriumOre;
	public static Block skydungeon_controller;
	public static Block quartz_fence;
	public static Item etheriumCrystal;

	private static void registerItemBlock(Block block) {
		GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
	}

	@Override
	protected void preInit(FMLStateEvent event) {
	}

	@Override
	protected void init(FMLStateEvent event) {
		EntityRegistry.registerModEntity(new ResourceLocation(ValkyrienWarfareMod.MODID, "FallingUpBlockEntity"), EntityFallingUpBlock.class, "FallingUpBlockEntity", 75, ValkyrienWarfareMod.INSTANCE, 80, 1, true);
		MinecraftForge.EVENT_BUS.register(worldEventsCommon);

		GameRegistry.registerWorldGenerator(new ValkyrienWarfareWorldGen(), 1);
	}

	@Override
	protected void postInit(FMLStateEvent event) {
	}
	
	@Override
	protected void registerBlocks() {
		etheriumOre = new BlockEtheriumOre(Material.ROCK).setHardness(3f).setUnlocalizedName("etheriumore").setRegistryName(getModID(), "etheriumore").setCreativeTab(ValkyrienWarfareMod.vwTab);
		skydungeon_controller = new BlockSkyTempleController(Material.GLASS).setHardness(15f).setUnlocalizedName("skydungeon_controller").setRegistryName(getModID(), "skydungeon_controller").setCreativeTab(ValkyrienWarfareMod.vwTab);
		quartz_fence = new BlockQuartzFence(Material.GLASS).setHardness(8f).setUnlocalizedName("quartz_fence").setRegistryName(getModID(), "quartz_fence").setCreativeTab(ValkyrienWarfareMod.vwTab);

		GameRegistry.register(etheriumOre);
		GameRegistry.register(skydungeon_controller);
		GameRegistry.register(quartz_fence);

		registerItemBlock(etheriumOre);
		registerItemBlock(skydungeon_controller);
		registerItemBlock(quartz_fence);
	}
	
	@Override
	protected void registerItems() {
		etheriumCrystal = new ItemEtheriumCrystal().setUnlocalizedName("etheriumcrystal").setRegistryName(getModID(), "etheriumcrystal").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(16);

		GameRegistry.register(etheriumCrystal);
	}
	
	@Override
	protected void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEntitySkyTempleController.class, "skydungeon_controller");
	}

}