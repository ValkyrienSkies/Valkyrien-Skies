package valkyrienwarfare.addon.opencomputers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.opencomputers.block.GPSBlock;
import valkyrienwarfare.addon.opencomputers.proxy.ClientProxyOC;
import valkyrienwarfare.addon.opencomputers.proxy.CommonProxyOC;
import valkyrienwarfare.addon.opencomputers.tileentity.GPSTileEntity;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.api.addons.VWAddon;

@VWAddon
public class ValkyrienWarfareOC extends Module<ValkyrienWarfareOC> {
    public static ValkyrienWarfareOC INSTANCE;
    public Block gpsBlock;

    public ValkyrienWarfareOC() {
        super("VW_OpenComputers", new CommonProxyOC(), "valkyrienwarfareoc");
        if (ValkyrienWarfareMod.INSTANCE.isRunningOnClient()) {
            setClientProxy(new ClientProxyOC());
        }

        INSTANCE = this;
    }

    @Override
    protected void preInit(FMLStateEvent event) {

    }

    @Override
    protected void init(FMLStateEvent event) {

    }

    @Override
    protected void postInit(FMLStateEvent event) {

    }

    @Override
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        gpsBlock = new GPSBlock().setUnlocalizedName("gpsblock").setRegistryName(getModID(), "gpsblock").setCreativeTab(ValkyrienWarfareMod.vwTab);;

        event.getRegistry().register(gpsBlock);
    }

    @Override
	public void registerItems(RegistryEvent.Register<Item> event) {
		registerItemBlock(event, gpsBlock);
	}

    @Override
    public void registerTileEntities() {
        GameRegistry.registerTileEntity(GPSTileEntity.class, "tilegps");
    }
}
