package ValkyrienWarfareBase.PhysicsManagement;

import java.util.ArrayList;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.world.World;

public class PhysicsTickHandler{
	
	public static void onWorldTickStart(World world){
		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
		
		ArrayList<PhysicsWrapperEntity> toUnload = (ArrayList<PhysicsWrapperEntity>) manager.physicsEntitiesToUnload.clone();
		manager.physicsEntitiesToUnload.clear();
		
		for(PhysicsWrapperEntity wrapper:toUnload){
			manager.onUnload(wrapper);
		}
		
		//Do this to prevent a ConcurrentModificationException from other threads spawning entities (ChunkLoading thread does this)
		ArrayList<PhysicsWrapperEntity> physicsEntities = (ArrayList<PhysicsWrapperEntity>) manager.physicsEntities.clone();
		
		for(PhysicsWrapperEntity wrapper:physicsEntities){
			wrapper.wrapping.coordTransform.setPrevMatrices();
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
			wrapper.wrapping.moveEntities();
		}
		
	}

	public static void onWorldTickEnd(World world){
		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
		
	}



}
