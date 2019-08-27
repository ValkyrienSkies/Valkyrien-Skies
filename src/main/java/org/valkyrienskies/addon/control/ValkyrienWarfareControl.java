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

package org.valkyrienskies.addon.control;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.valkyrienskies.addon.control.block.multiblocks.*;
import org.valkyrienskies.addon.control.block.torque.TileEntityRotationTrainAxle;
import org.valkyrienskies.addon.control.capability.ICapabilityLastRelay;
import org.valkyrienskies.addon.control.capability.ImplCapabilityLastRelay;
import org.valkyrienskies.addon.control.capability.StorageLastRelay;
import org.valkyrienskies.addon.control.item.ItemRelayWire;
import org.valkyrienskies.addon.control.item.ItemWrench;
import org.valkyrienskies.addon.control.network.*;
import org.valkyrienskies.addon.control.piloting.PilotControlsMessage;
import org.valkyrienskies.addon.control.piloting.PilotControlsMessageHandler;
import org.valkyrienskies.addon.control.tileentity.*;
import org.valkyrienskies.addon.world.ValkyrienWarfareWorld;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import valkyrienwarfare.api.addons.Module;

@Mod(
        name = ValkyrienWarfareControl.MOD_NAME,
        modid = ValkyrienWarfareControl.MOD_ID,
        version = ValkyrienWarfareControl.MOD_VERSION,
        dependencies = "required-after:" + ValkyrienWarfareWorld.MOD_ID
)
@EventBusSubscriber
public class ValkyrienWarfareControl {

    public static final String MOD_ID = "vs_control";
    static final String MOD_NAME = "Valkyrien Skies Control";
    static final String MOD_VERSION = ValkyrienSkiesMod.MOD_VERSION;

    @Instance(MOD_ID)
    public static ValkyrienWarfareControl INSTANCE;

    @CapabilityInject(ICapabilityLastRelay.class)
    public static final Capability<ICapabilityLastRelay> lastRelayCapability = null;

    public static SimpleNetworkWrapper controlNetwork;
    public final BlocksValkyrienWarfareControl vwControlBlocks = new BlocksValkyrienWarfareControl();
    public Item relayWire;
    public Item multiBlockWrench;


    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        vwControlBlocks.registerBlocks(event);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        relayWire = new ItemRelayWire().setTranslationKey("relaywire")
                .setRegistryName(MOD_ID, "relaywire")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);
        multiBlockWrench = new ItemWrench().setTranslationKey("vw_wrench")
                .setRegistryName(MOD_ID, "vw_wrench")
                .setCreativeTab(ValkyrienSkiesMod.vwTab);

        event.getRegistry().register(relayWire);
        event.getRegistry().register(multiBlockWrench);

        vwControlBlocks.registerBlockItems(event);
        // This doesn't really belong here, but whatever.
        MultiblockRegistry.registerAllPossibleSchematicVariants(EthereumEngineMultiblockSchematic.class);
        MultiblockRegistry.registerAllPossibleSchematicVariants(EthereumCompressorMultiblockSchematic.class);
        MultiblockRegistry.registerAllPossibleSchematicVariants(RudderAxleMultiblockSchematic.class);
        MultiblockRegistry.registerAllPossibleSchematicVariants(GiantPropellerMultiblockSchematic.class);
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        Module.registerRecipe(event, "recipe_pilots_chair",
                new ItemStack(vwControlBlocks.pilotsChair),
                "SLS",
                "EWE",
                " S ",
                'S', Items.STICK,
                'L', Items.LEATHER,
                'W', Item.getItemFromBlock(Blocks.LOG),
                'E', ValkyrienWarfareWorld.INSTANCE.ethereumCrystal);

        Module.registerRecipe(event, "recipe_basic_engine",
                new ItemStack(vwControlBlocks.basicEngine, 4),
                "I##",
                "IPP",
                "I##",
                '#', Item.getItemFromBlock(Blocks.PLANKS),
                'P', Item.getItemFromBlock(Blocks.PISTON),
                'I', Items.IRON_INGOT);

        Module.registerRecipe(event, "recipe_advanced_engine1", new ItemStack(vwControlBlocks.advancedEngine, 4), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.STONE), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        Module.registerRecipe(event, "recipe_advanced_engine2", new ItemStack(vwControlBlocks.advancedEngine, 2), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.COBBLESTONE), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        Module.registerRecipe(event, "recipe_elite_engine", new ItemStack(vwControlBlocks.eliteEngine, 4), "III", "IPP", "III", 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
        Module.registerRecipe(event, "recipe_ultimate_engine", new ItemStack(vwControlBlocks.ultimateEngine, 4), "I##", "IPP", "I##", '#', Item.getItemFromBlock(Blocks.OBSIDIAN), 'P', Item.getItemFromBlock(Blocks.PISTON), 'I', Items.IRON_INGOT);
    }

    protected void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityPilotsChair.class, "tilemanualshipcontroller");
        GameRegistry.registerTileEntity(TileEntityNodeRelay.class, "tilethrustrelay");
        GameRegistry.registerTileEntity(TileEntityShipHelm.class, "tileshiphelm");
        GameRegistry.registerTileEntity(TileEntityShipTelegraph.class, "tileshiptelegraph");
        GameRegistry.registerTileEntity(TileEntityPropellerEngine.class, "tilepropellerengine");
        GameRegistry.registerTileEntity(TileEntityGyroscopeStabilizer.class, "tilegyroscope_stabilizer");
        GameRegistry.registerTileEntity(TileEntityLiftValve.class, "tileliftvalve");
        GameRegistry.registerTileEntity(TileEntityNetworkDisplay.class, "tilenetworkdisplay");
        GameRegistry.registerTileEntity(TileEntityLiftControl.class, "tileliftcontrol");

        GameRegistry.registerTileEntity(TileEntityGyroscopeDampener.class, "tilegyroscope_dampener");
        GameRegistry.registerTileEntity(TileEntityEthereumEnginePart.class, "tile_big_engine_part");
        GameRegistry.registerTileEntity(TileEntityGearbox.class, "tile_gearbox");
        GameRegistry.registerTileEntity(TileEntityEthereumCompressorPart.class, "tile_ethereum_compressor_part");
        GameRegistry.registerTileEntity(TileEntityRudderAxlePart.class, "tile_rudder_axle_part");
        GameRegistry.registerTileEntity(TileEntityGiantPropellerPart.class, "tile_giant_propeller_part");
        GameRegistry.registerTileEntity(TileEntityRotationTrainAxle.class, "tile_rotation_train_axle");

        GameRegistry.registerTileEntity(TileEntityPassengerChair.class, new ResourceLocation(MOD_ID, "tile_passengers_chair"));
    }

    protected void registerNetworks() {
        controlNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("controlnetwork");
        controlNetwork.registerMessage(PilotControlsMessageHandler.class, PilotControlsMessage.class, 2, Side.SERVER);
        controlNetwork.registerMessage(MessageStartPilotingHandler.class, MessageStartPiloting.class, 3, Side.CLIENT);
        controlNetwork.registerMessage(MessageStopPilotingHandler.class, MessageStopPiloting.class, 4, Side.CLIENT);
        controlNetwork.registerMessage(MessagePlayerStoppedPilotingHandler.class, MessagePlayerStoppedPiloting.class, 5, Side.SERVER);
    }

    protected void registerCapabilities() {
        CapabilityManager.INSTANCE.register(ICapabilityLastRelay.class, new StorageLastRelay(),
                ImplCapabilityLastRelay::new);
    }
}
