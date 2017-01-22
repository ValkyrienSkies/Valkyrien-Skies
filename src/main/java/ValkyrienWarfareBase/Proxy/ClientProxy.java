package ValkyrienWarfareBase.Proxy;

import ValkyrienWarfareBase.EventsClient;
import ValkyrienWarfareBase.KeyHandler;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Math.Quaternion;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.Render.PhysObjectRenderFactory;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	KeyHandler keyEvents = new KeyHandler();
	public static ICamera lastCamera;

	@Override
	public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);
		OBJLoader.INSTANCE.addDomain(ValkyrienWarfareMod.MODID.toLowerCase());
		RenderingRegistry.registerEntityRenderingHandler(PhysicsWrapperEntity.class, new PhysObjectRenderFactory());
	}

	@Override
	public void init(FMLInitializationEvent e) {
		super.init(e);
		MinecraftForge.EVENT_BUS.register(new EventsClient());
		MinecraftForge.EVENT_BUS.register(keyEvents);
		registerBlockItem(ValkyrienWarfareMod.physicsInfuser);
		registerBlockItem(ValkyrienWarfareMod.physicsInfuserCreative);
	}

	@Override
	public void postInit(FMLPostInitializationEvent e) {
		super.postInit(e);
	}

	private void registerBlockItem(Block toRegister) {
		Item item = Item.getItemFromBlock(toRegister);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(ValkyrienWarfareMod.MODID + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	@Override
	public void updateShipPartialTicks(PhysicsWrapperEntity entity) {
		double partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
		// entity.wrapping.renderer.updateTranslation(partialTicks);
		Vector centerOfRotation = entity.wrapping.centerCoord;
		if (entity.wrapping.renderer == null) {
			return;
		}
		entity.wrapping.renderer.curPartialTick = partialTicks;

		double moddedX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
		double moddedY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
		double moddedZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
		double p0 = Minecraft.getMinecraft().thePlayer.lastTickPosX + (Minecraft.getMinecraft().thePlayer.posX - Minecraft.getMinecraft().thePlayer.lastTickPosX) * (double) partialTicks;
		double p1 = Minecraft.getMinecraft().thePlayer.lastTickPosY + (Minecraft.getMinecraft().thePlayer.posY - Minecraft.getMinecraft().thePlayer.lastTickPosY) * (double) partialTicks;
		double p2 = Minecraft.getMinecraft().thePlayer.lastTickPosZ + (Minecraft.getMinecraft().thePlayer.posZ - Minecraft.getMinecraft().thePlayer.lastTickPosZ) * (double) partialTicks;

		Quaternion smoothRotation = entity.wrapping.renderer.getSmoothRotationQuat(partialTicks);
		double[] radians = smoothRotation.toRadians();

		double moddedPitch = Math.toDegrees(radians[0]);
		double moddedYaw = Math.toDegrees(radians[1]);
		double moddedRoll = Math.toDegrees(radians[2]);

		entity.wrapping.coordTransform.updateRenderMatrices(moddedX, moddedY, moddedZ, moddedPitch, moddedYaw, moddedRoll);
	}
}
