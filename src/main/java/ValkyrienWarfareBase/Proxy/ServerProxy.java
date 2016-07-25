package ValkyrienWarfareBase.Proxy;

import ValkyrienWarfareBase.Commands.PhysicsGravityCommand;
import ValkyrienWarfareBase.Commands.PhysicsIterCommand;
import ValkyrienWarfareBase.Commands.PhysicsSpeedCommand;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy extends CommonProxy{

	@Override
	public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);
    }

	@Override
    public void init(FMLInitializationEvent e) {
		super.init(e);
    }

	@Override
    public void postInit(FMLPostInitializationEvent e) {
		super.postInit(e);
    }

}
