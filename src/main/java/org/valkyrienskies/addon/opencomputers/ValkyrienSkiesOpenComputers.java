package org.valkyrienskies.addon.opencomputers;

import java.util.ArrayList;
import java.util.List;

import org.valkyrienskies.addon.opencomputers.block.BlockGPS;
import org.valkyrienskies.addon.opencomputers.tileentity.TileEntityGPS;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(
    name = ValkyrienSkiesOpenComputers.MOD_NAME,
    modid = ValkyrienSkiesOpenComputers.MOD_ID,
    version = ValkyrienSkiesOpenComputers.MOD_VERSION,
    dependencies = "required-after:" + ValkyrienSkiesMod.MOD_ID
)
public class ValkyrienSkiesOpenComputers {
    // Used for registering stuff
    public static final List<Block> BLOCKS = new ArrayList<Block>();
    public static final List<Item> ITEMS = new ArrayList<Item>();

    // MOD INFO CONSTANTS
    static final String MOD_ID = "vs_opencomputers";
    static final String MOD_NAME = "Valkyrien Skies Open Computers";
    static final String MOD_VERSION = ValkyrienSkiesMod.MOD_VERSION;

    // MOD INSTANCE
    @Instance(MOD_ID)
    public static ValkyrienSkiesOpenComputers INSTANCE = new ValkyrienSkiesOpenComputers();

    // MOD CLASS MEMBERS
    private Block gps;

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        gps = new BlockGPS();

        Block[] blockArray = BLOCKS.toArray(new Block[0]);
        event.getRegistry().registerAll(blockArray);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ITEMS.toArray(new Item[0]));
    }

    @SubscribeEvent
    public void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityGPS.class, "gps_tileentity");
    }
}
