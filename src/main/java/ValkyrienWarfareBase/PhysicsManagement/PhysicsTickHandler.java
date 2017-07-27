package ValkyrienWarfareBase.PhysicsManagement;

import java.util.ArrayList;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Interaction.EntityDraggable;
import ValkyrienWarfareBase.Optimization.ShipCollisionTask;
import net.minecraft.world.World;

public class PhysicsTickHandler {

    public static void onWorldTickStart(World world) {
        WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);

        ArrayList<PhysicsWrapperEntity> toUnload = (ArrayList<PhysicsWrapperEntity>) manager.physicsEntitiesToUnload.clone();
        for (PhysicsWrapperEntity wrapper : toUnload) {
            manager.onUnload(wrapper);
        }

        ArrayList<PhysicsWrapperEntity> physicsEntities = manager.getTickablePhysicsEntities();

        if (!ValkyrienWarfareMod.doSplitting) {
            for (PhysicsWrapperEntity wrapper : physicsEntities) {
                wrapper.wrapping.coordTransform.setPrevMatrices();
                wrapper.wrapping.updateChunkCache();
                // Collections.shuffle(wrapper.wrapping.physicsProcessor.activeForcePositions);
            }
        } else {
//			boolean didSplitOccur = false; for(PhysicsWrapperEntity wrapper:physicsEntities){ if(wrapper.wrapping.processPotentialSplitting()){ didSplitOccur = true; } } if(didSplitOccur){ while(didSplitOccur){ didSplitOccur = false; ArrayList oldPhysicsEntities = physicsEntities; ArrayList<PhysicsWrapperEntity> newPhysicsEntities = (ArrayList<PhysicsWrapperEntity>) manager.physicsEntities.clone(); newPhysicsEntities.removeAll(oldPhysicsEntities); if(newPhysicsEntities.size()!=0){ for(PhysicsWrapperEntity wrapper:newPhysicsEntities){ if(wrapper.wrapping.processPotentialSplitting()){ didSplitOccur = true; } } } } physicsEntities = (ArrayList<PhysicsWrapperEntity>) manager.physicsEntities.clone(); } for(PhysicsWrapperEntity wrapper:physicsEntities){ wrapper.wrapping.coordTransform.setPrevMatrices(); wrapper.wrapping.updateChunkCache(); // Collections.shuffle(wrapper.wrapping.physicsProcessor.activeForcePositions); }
        }

        int iters = ValkyrienWarfareMod.physIter;
        double newPhysSpeed = ValkyrienWarfareMod.physSpeed;
        Vector newGravity = ValkyrienWarfareMod.gravity;
        for (int pass = 0; pass < iters; pass++) {
            // Run PRE-Col
        	runPhysicsIteration(physicsEntities, manager);
        }

    }

    public static void onWorldTickEnd(World world) {
        WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
        ArrayList<PhysicsWrapperEntity> physicsEntities = manager.getTickablePhysicsEntities();

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            wrapper.wrapping.coordTransform.sendPositionToPlayers();
        }
        EntityDraggable.tickAddedVelocityForWorld(world);

        for (PhysicsWrapperEntity wrapperEnt : physicsEntities) {
            wrapperEnt.wrapping.onPostTick();
        }
    }

    public static void runPhysicsIteration(ArrayList<PhysicsWrapperEntity> physicsEntities, WorldPhysObjectManager manager) {
    	double newPhysSpeed = ValkyrienWarfareMod.physSpeed;
        Vector newGravity = ValkyrienWarfareMod.gravity;
        int iters = ValkyrienWarfareMod.physIter;

        ArrayList<ShipCollisionTask> collisionTasks = new ArrayList<ShipCollisionTask>(physicsEntities.size() * 2);

    	for (PhysicsWrapperEntity wrapper : physicsEntities) {
        	if(!wrapper.firstUpdate){
        		wrapper.wrapping.physicsProcessor.gravity = newGravity;
            	wrapper.wrapping.physicsProcessor.rawPhysTickPreCol(newPhysSpeed, iters);

            	wrapper.wrapping.physicsProcessor.worldCollision.tickUpdatingTheCollisionCache();

            	wrapper.wrapping.physicsProcessor.worldCollision.splitIntoCollisionTasks(collisionTasks);
        	}
        }

    	try {
            ValkyrienWarfareMod.MultiThreadExecutor.invokeAll(collisionTasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    	for(ShipCollisionTask task : collisionTasks) {
    		PhysicsWrapperEntity wrapper = task.toTask.parent.wrapper;
    		if(!wrapper.firstUpdate) {
    			task.toTask.processCollisionTask(task);
    		}
    	}

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
        	if(!wrapper.firstUpdate){
        		wrapper.wrapping.physicsProcessor.rawPhysTickPostCol();
        	}else{
        		wrapper.wrapping.coordTransform.updateAllTransforms();
        	}
        }

    }

}
