package ValkyrienWarfareBase.PhysicsManagement;

import java.util.ArrayList;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.client.Minecraft;

public class PhysicsTickHandlerClient {

	public static void onClientTickStart(){
		if(Minecraft.getMinecraft().thePlayer!=null&&Minecraft.getMinecraft().isGamePaused()){
			WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(Minecraft.getMinecraft().theWorld);
			ArrayList<PhysicsWrapperEntity> physicsEntities = manager.physicsEntities;
			
			for(PhysicsWrapperEntity wrapper:physicsEntities){
				wrapper.wrapping.onPreTick();
			}
		}
	}
	
	public static void onClientTickEnd(){
		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(Minecraft.getMinecraft().theWorld);
		
	}
}
