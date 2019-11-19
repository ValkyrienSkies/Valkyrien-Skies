/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.addon.world;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.valkyrienskies.addon.world.block.BlockValkyriumOre;
import org.valkyrienskies.addon.world.capability.ICapabilityAntiGravity;
import org.valkyrienskies.addon.world.capability.ImplCapabilityAntiGravity;
import org.valkyrienskies.addon.world.capability.StorageAntiGravity;
import org.valkyrienskies.addon.world.proxy.CommonProxyWorld;
import org.valkyrienskies.addon.world.worldgen.ValkyrienSkiesWorldGen;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

@Mod(
    name = ValkyrienSkiesWorld.MOD_NAME,
    modid = ValkyrienSkiesWorld.MOD_ID,
    version = ValkyrienSkiesWorld.MOD_VERSION,
    dependencies = "required-after:" + ValkyrienSkiesMod.MOD_ID
)
@Mod.EventBusSubscriber(modid = ValkyrienSkiesWorld.MOD_ID)
@Log4j2
public class ValkyrienSkiesWorld {
	// Used for registering stuff
	public static final List<Block> BLOCKS = new ArrayList<Block>();
	public static final List<Item> ITEMS = new ArrayList<Item>();

    // MOD INFO CONSTANTS
    public static final String MOD_ID = "vs_world";
    static final String MOD_NAME = "Valkyrien Skies World";
    static final String MOD_VERSION = ValkyrienSkiesMod.MOD_VERSION;

    // MOD INSTANCE
    @Instance(MOD_ID)
    public static ValkyrienSkiesWorld INSTANCE;

    @SidedProxy(
        clientSide = "org.valkyrienskies.addon.world.proxy.ClientProxyWorld",
        serverSide = "org.valkyrienskies.addon.world.proxy.CommonProxyWorld")
    private static CommonProxyWorld proxy;

    @CapabilityInject(ICapabilityAntiGravity.class)
    public static final Capability<ICapabilityAntiGravity> ANTI_GRAVITY_CAPABILITY = null;

    // MOD CLASS MEMBERS
    private static final WorldEventsCommon worldEventsCommon = new WorldEventsCommon();
    public static boolean OREGEN_ENABLED = true;
    public Block valkyriumOre;
    public Item valkyriumCrystal;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        log.debug("Registering blocks...");

        INSTANCE.valkyriumOre = new BlockValkyriumOre();

		// Actual registering
		Block[] blockArray = BLOCKS.toArray(new Block[0]);
		event.getRegistry().registerAll(blockArray);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        INSTANCE.valkyriumCrystal = new ItemValkyriumCrystal();

		event.getRegistry().registerAll(ITEMS.toArray(new Item[0]));
    }

    @EventHandler
    private void preInit(FMLPreInitializationEvent event) {
        registerCapabilities();
        proxy.preInit(event);
    }

    @EventHandler
    protected void init(FMLInitializationEvent event) {
        EntityRegistry.registerModEntity(
            new ResourceLocation(ValkyrienSkiesWorld.MOD_ID, "fall_up_block_entity"),
            EntityFallingUpBlock.class,
            "fall_up_block_entity",
            75, ValkyrienSkiesWorld.INSTANCE, 80, 1, true);

        MinecraftForge.EVENT_BUS.register(worldEventsCommon);
        GameRegistry.registerWorldGenerator(new ValkyrienSkiesWorldGen(), 1);
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    private void registerCapabilities() {
        CapabilityManager.INSTANCE.register(ICapabilityAntiGravity.class, new StorageAntiGravity(),
            ImplCapabilityAntiGravity::new);
    }
}
