/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.physics.management;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.physmanagement.interaction.EntityDraggable;
import valkyrienwarfare.physics.collision.optimization.ShipCollisionTask;

public class PhysicsTickHandler {

	public static void onWorldTickStart(World world) {
		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);

		List<PhysicsWrapperEntity> toUnload = new ArrayList<PhysicsWrapperEntity>(manager.physicsEntitiesToUnload);
		for (PhysicsWrapperEntity wrapper : toUnload) {
			manager.onUnload(wrapper);
		}

        List<PhysicsWrapperEntity> physicsEntities = manager.getTickablePhysicsEntities();

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            wrapper.wrapping.coordTransform.setPrevMatrices();
            wrapper.wrapping.updateChunkCache();
            // Collections.shuffle(wrapper.wrapping.physicsProcessor.activeForcePositions);
        }

        int iters = ValkyrienWarfareMod.physIter;
		PhysicsTickThreadTask physicsThreadTask = new PhysicsTickThreadTask(iters, physicsEntities, manager);

		// ValkyrienWarfareMod.PhysicsMasterThread.invokeAll(new ArrayList<PhysicsW>)

		try {
			// ValkyrienWarfareMod.PhysicsMasterThread.shutdown();

			// ValkyrienWarfareMod.PhysicsMasterThread.execute(new Runnable() {
			// @Override
			// public void run() {
			// try {
			// physicsThreadTask.call();
			// }catch(Exception e) {
			//
			// }
			// }
			// });
			// System.out.println(manager.hasPhysicsThreadFinished);
			// physicsThreadTask.call();
			manager.physicsThreadStatus = ValkyrienWarfareMod.PhysicsMasterThread.submit(physicsThreadTask);
			//
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void onWorldTickEnd(World world) {
		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
		List<PhysicsWrapperEntity> physicsEntities = manager.getTickablePhysicsEntities();

		if (manager.physicsThreadStatus != null && !manager.physicsThreadStatus.isDone()) {
			try {
				// Wait for the physicsThread to return before moving on.
				manager.physicsThreadStatus.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (PhysicsWrapperEntity wrapper : physicsEntities) {
			wrapper.wrapping.coordTransform.sendPositionToPlayers();
		}
		EntityDraggable.tickAddedVelocityForWorld(world);
		for (PhysicsWrapperEntity wrapperEnt : physicsEntities) {
			wrapperEnt.wrapping.onPostTick();
		}
	}

	public static void runPhysicsIteration(List<PhysicsWrapperEntity> physicsEntities,
			WorldPhysObjectManager manager) {
		double newPhysSpeed = ValkyrienWarfareMod.physSpeed;
		Vector newGravity = ValkyrienWarfareMod.gravity;
		int iters = ValkyrienWarfareMod.physIter;

		List<ShipCollisionTask> collisionTasks = new ArrayList<ShipCollisionTask>(physicsEntities.size() * 2);

		for (PhysicsWrapperEntity wrapper : physicsEntities) {
			if (!wrapper.firstUpdate) {
				wrapper.wrapping.physicsProcessor.gravity = newGravity;
				wrapper.wrapping.physicsProcessor.rawPhysTickPreCol(newPhysSpeed, iters);

				wrapper.wrapping.physicsProcessor.worldCollision.tickUpdatingTheCollisionCache();

				wrapper.wrapping.physicsProcessor.worldCollision.splitIntoCollisionTasks(collisionTasks);
			}
		}

		try {
		    // TODO: Right here!
			ValkyrienWarfareMod.MultiThreadExecutor.invokeAll(collisionTasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (ShipCollisionTask task : collisionTasks) {
			PhysicsWrapperEntity wrapper = task.getToTask().getParent().wrapper;
			if (!wrapper.firstUpdate) {
				task.getToTask().processCollisionTask(task);
			}
		}

		for (PhysicsWrapperEntity wrapper : physicsEntities) {
			if (!wrapper.firstUpdate) {
				wrapper.wrapping.physicsProcessor.rawPhysTickPostCol();
			} else {
				wrapper.wrapping.coordTransform.updateAllTransforms();
			}
		}

	}

	private static class PhysicsTickThreadTask implements Callable<Void> {

		private final int iters;
		private final List physicsEntities;
		private final WorldPhysObjectManager manager;

		public PhysicsTickThreadTask(int iters, List physicsEntities, WorldPhysObjectManager manager) {
			this.iters = iters;
			this.physicsEntities = physicsEntities;
			this.manager = manager;
		}

		@Override
		public Void call() throws Exception {
			for (int pass = 0; pass < iters; pass++) {
				// Run PRE-Col
				runPhysicsIteration(physicsEntities, manager);
			}
			return null;
		}

	}

}
