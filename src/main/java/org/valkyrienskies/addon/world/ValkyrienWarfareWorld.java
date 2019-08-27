/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.addon.world;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.valkyrienskies.addon.world.block.BlockEthereumOre;
import org.valkyrienskies.addon.world.capability.ICapabilityAntiGravity;
import org.valkyrienskies.addon.world.capability.ImplCapabilityAntiGravity;
import org.valkyrienskies.addon.world.capability.StorageAntiGravity;
import org.valkyrienskies.addon.world.worldgen.ValkyrienWarfareWorldGen;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import valkyrienwarfare.api.addons.Module;

@Mod(
        name = ValkyrienWarfareWorld.MOD_NAME,
        modid = ValkyrienWarfareWorld.MOD_ID,
        version = ValkyrienWarfareWorld.MOD_VERSION,
        dependencies = "required-after:" + ValkyrienSkiesMod.MOD_ID
)
@Mod.EventBusSubscriber(modid = ValkyrienWarfareWorld.MOD_ID)
public class ValkyrienWarfareWorld {

    private static final Logger logger = LogManager.getLogger(ValkyrienWarfareWorld.class);

    public static final String MOD_ID = "vs_world";
    static final String MOD_NAME = "Valkyrien Skies World";
    static final String MOD_VERSION = ValkyrienSkiesMod.MOD_VERSION;

    @Instance(MOD_ID)
    public static ValkyrienWarfareWorld INSTANCE;

    @CapabilityInject(ICapabilityAntiGravity.class)
    public static final Capability<ICapabilityAntiGravity> ANTI_GRAVITY_CAPABILITY = null;
    private static final WorldEventsCommon worldEventsCommon = new WorldEventsCommon();

    public Block ethereumOre;
    public Item ethereumCrystal;
    public static boolean OREGEN_ENABLED = true;
    
    @EventHandler
    protected void init(FMLInitializationEvent event) {
        EntityRegistry.registerModEntity(
                new ResourceLocation(ValkyrienSkiesMod.MOD_ID, "FallingUpBlockEntity"),
                EntityFallingUpBlock.class, 
                "FallingUpBlockEntity", 
                75, ValkyrienSkiesMod.INSTANCE, 80, 1, true);
        
        MinecraftForge.EVENT_BUS.register(worldEventsCommon);
        GameRegistry.registerWorldGenerator(new ValkyrienWarfareWorldGen(), 1);
    }

    @EventHandler
    private void doPreInit(FMLPreInitializationEvent event) {
        registerCapabilities();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        logger.debug("Registering blocks...");

        INSTANCE.ethereumOre = new BlockEthereumOre(Material.ROCK).setHardness(3f)
                .setTranslationKey("ethereum_ore")
                .setRegistryName(MOD_ID, "ethereum_ore")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);

        event.getRegistry().register(INSTANCE.ethereumOre);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        INSTANCE.ethereumCrystal = new ItemEthereumCrystal().setTranslationKey("ethereumcrystal")
                .setRegistryName(MOD_ID, "ethereum_crystal")
                .setCreativeTab(ValkyrienSkiesMod.vwTab)
                .setMaxStackSize(16);

        event.getRegistry().register(INSTANCE.ethereumCrystal);

        Module.registerItemBlock(event, INSTANCE.ethereumOre);
    }

    private void registerCapabilities() {
        CapabilityManager.INSTANCE.register(ICapabilityAntiGravity.class, new StorageAntiGravity(), ImplCapabilityAntiGravity.class);
    }
}