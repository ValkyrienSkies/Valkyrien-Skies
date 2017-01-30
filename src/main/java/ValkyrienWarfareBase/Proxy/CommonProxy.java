package ValkyrienWarfareBase.Proxy;

import ValkyrienWarfareBase.EventsCommon;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.ChunkManagement.DimensionPhysicsChunkManager;
import ValkyrienWarfareBase.PhysicsManagement.DimensionPhysObjectManager;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent e) {

	}

	public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(new EventsCommon());
		ValkyrienWarfareMod.chunkManager = new DimensionPhysicsChunkManager();
		ValkyrienWarfareMod.physicsManager = new DimensionPhysObjectManager();
	}

	public void postInit(FMLPostInitializationEvent e) {

	}

	public void updateShipPartialTicks(PhysicsWrapperEntity wrapper) {

	}

	public void registerCommands(MinecraftServer server) {

	}

}
