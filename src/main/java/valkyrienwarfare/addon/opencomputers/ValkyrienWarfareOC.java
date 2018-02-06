package valkyrienwarfare.addon.opencomputers;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.opencomputers.block.GPSBlock;
import valkyrienwarfare.addon.opencomputers.proxy.ClientProxyOC;
import valkyrienwarfare.addon.opencomputers.proxy.CommonProxyOC;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.api.addons.VWAddon;

@VWAddon
public class ValkyrienWarfareOC extends Module<ValkyrienWarfareOC> {
    public static ValkyrienWarfareOC INSTANCE;
    public Block gpsBlock;

    public ValkyrienWarfareOC() {
        super("VW_OpenComputers", new CommonProxyOC(), "valkrienwarfareoc");
        if (ValkyrienWarfareMod.INSTANCE.isRunningOnClient()) {
            setClientProxy(new ClientProxyOC());
        }
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
        gpsBlock = new GPSBlock();

        event.getRegistry().register(gpsBlock);
    }
}
