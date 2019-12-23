package org.valkyrienskies.mod.common.util;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.valkyrienskies.mod.common.multithreaded.VSThread;

public class VSPreconditions {

    public static void assertPhysicsThread() {
        assert Thread.currentThread() instanceof VSThread :
            "This method may only be called on the physics thread";
    }

    public static void assertClientThread() {
       assert Minecraft.getMinecraft().isCallingFromMinecraftThread() :
           "This method may only be called on the client thread";
    }

    public static void assertServerThread() {
        assert FMLCommonHandler.instance().getMinecraftServerInstance()
            .isCallingFromMinecraftThread() :
            "This method may only be called on the server thread";
    }

}
