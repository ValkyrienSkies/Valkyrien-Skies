package ValkyrienWarfareBase.Proxy;

import ValkyrienWarfareBase.EventsClient;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.Render.PhysObjectRenderFactory;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy{

	EventsClient eventsClient = new EventsClient();
	
	@Override
	public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);
		RenderingRegistry.registerEntityRenderingHandler(PhysicsWrapperEntity.class,new PhysObjectRenderFactory());
    }

	@Override
    public void init(FMLInitializationEvent e) {
		super.init(e);
		MinecraftForge.EVENT_BUS.register(eventsClient);
        registerBlockItem(ValkyrienWarfareMod.physicsInfuser);
    }

	@Override
    public void postInit(FMLPostInitializationEvent e) {
		super.postInit(e);
    }

	private void registerBlockItem(Block toRegister){
		Item item = Item.getItemFromBlock(toRegister);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(ValkyrienWarfareMod.MODID + ":" + item.getUnlocalizedName(), "inventory"));
	}

}
