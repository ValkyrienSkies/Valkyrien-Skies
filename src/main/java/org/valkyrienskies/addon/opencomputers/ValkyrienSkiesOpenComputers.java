package org.valkyrienskies.addon.opencomputers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.valkyrienskies.addon.opencomputers.block.GPSBlock;
import org.valkyrienskies.addon.opencomputers.tileentity.GPSTileEntity;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienwarfare.api.addons.Module;

@Mod(
    name = ValkyrienSkiesOpenComputers.MOD_NAME,
    modid = ValkyrienSkiesOpenComputers.MOD_ID,
    version = ValkyrienSkiesOpenComputers.MOD_VERSION,
    dependencies = "required-after:" + ValkyrienSkiesMod.MOD_ID
)
public class ValkyrienSkiesOpenComputers {

    // MOD INFO CONSTANTS
    static final String MOD_ID = "vs_open_computers";
    static final String MOD_NAME = "Valkyrien Skies Open Computers";
    static final String MOD_VERSION = ValkyrienSkiesMod.MOD_VERSION;

    // MOD INSTANCE
    @Instance(MOD_ID)
    public static ValkyrienSkiesOpenComputers INSTANCE = new ValkyrienSkiesOpenComputers();

    // MOD CLASS MEMBERS
    private Block gpsBlock;

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        gpsBlock = new GPSBlock().setTranslationKey("gpsblock")
            .setRegistryName(MOD_ID, "gpsblock")
            .setCreativeTab(ValkyrienSkiesMod.VS_CREATIVE_TAB);

        event.getRegistry().register(gpsBlock);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        Module.registerItemBlock(event, gpsBlock);
    }

    @SubscribeEvent
    public void registerTileEntities() {
        GameRegistry.registerTileEntity(GPSTileEntity.class, "tilegps");
    }
}
