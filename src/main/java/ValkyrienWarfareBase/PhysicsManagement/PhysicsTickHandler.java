package ValkyrienWarfareBase.PhysicsManagement;

import java.util.ArrayList;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class PhysicsTickHandler{
	
	public static void onWorldTickStart(World world){
		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
		
		ArrayList<PhysicsWrapperEntity> physicsEntities = manager.physicsEntities;
		
		for(PhysicsWrapperEntity wrapper:physicsEntities){
			wrapper.wrapping.updateChunkCache();
		}
		
		int iters = manager.physIter;
		double newPhysSpeed = manager.physSpeed;
		for(int pass = 0;pass<iters;pass++){
			//Run PRE-Col
			for(PhysicsWrapperEntity wrapper:physicsEntities){
				wrapper.wrapping.physicsProcessor.rawPhysTickPreCol(newPhysSpeed, iters);
			}
			
			if(ValkyrienWarfareMod.multiThreadedPhysics){
				try {
					ValkyrienWarfareMod.MultiThreadExecutor.invokeAll(manager.physCollisonCallables);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else{
				for(PhysicsWrapperEntity wrapper:physicsEntities){
					wrapper.wrapping.physicsProcessor.processWorldCollision();
				}
				for(PhysicsWrapperEntity wrapper:physicsEntities){
					wrapper.wrapping.physicsProcessor.rawPhysTickPostCol();
				}
			}
		}
		
		for(PhysicsWrapperEntity wrapper:physicsEntities){
			wrapper.wrapping.coordTransform.sendPositionToPlayers();
		}
		
	}

	public static void onWorldTickEnd(World world){
		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
		
	}

	public static void onClientTickStart(){
		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(Minecraft.getMinecraft().theWorld);
		
	}
	
	public static void onClientTickEnd(){
		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(Minecraft.getMinecraft().theWorld);
		
	}

}
