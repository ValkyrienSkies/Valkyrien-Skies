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

import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.multithreaded.PhysicsShipTransform;
import valkyrienwarfare.mod.physmanagement.interaction.EntityDraggable;
import valkyrienwarfare.physics.collision.optimization.ShipCollisionTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class PhysicsTickHandler {

    public static void onWorldTickStart(World world) {
        WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);

        List<PhysicsWrapperEntity> toUnload = new ArrayList<PhysicsWrapperEntity>(manager.physicsEntitiesToUnload);
        for (PhysicsWrapperEntity wrapper : toUnload) {
            manager.onUnload(wrapper);
        }

        List<PhysicsWrapperEntity> physicsEntities = manager.getTickablePhysicsEntities();

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            wrapper.getPhysicsObject().coordTransform.updatePrevTickTransform();

            if (wrapper.getPhysicsObject().coordTransform.getCurrentPhysicsTransform() instanceof PhysicsShipTransform) {
                // Here we poll our transforms from the physics tick, and apply the latest one
                // to the game tick.
                // This is (for the most part) the only place in the code that bridges the
                // physics tick with the game tick, and so all of the game tick code that
                // depends on ship movement should go right here! Will possibly be moved to the
                // end of the game tick instead.
                PhysicsShipTransform physTransform = (PhysicsShipTransform) wrapper.getPhysicsObject().coordTransform
                        .getCurrentPhysicsTransform();

                wrapper.posX = physTransform.getPosX();
                wrapper.posY = physTransform.getPosY();
                wrapper.posZ = physTransform.getPosZ();
                wrapper.setPitch(physTransform.getPitch());
                wrapper.setYaw(physTransform.getYaw());
                wrapper.setRoll(physTransform.getRoll());

                wrapper.getPhysicsObject().coordTransform.updateAllTransforms(true, true);
            }

            wrapper.getPhysicsObject().updateChunkCache();
        }

        /*
         * All moved off the game tick thread, there is simply no other way to fix the
         * physics.
         */

        // PhysicsTickThreadTask physicsThreadTask = new
        // PhysicsTickThreadTask(ValkyrienWarfareMod.physIter, physicsEntities,
        // manager);

        // try { manager.setPhysicsThread(ValkyrienWarfareMod.PHYSICS_THREADS.submit(
        // physicsThreadTask)); } catch (Exception e) { e.printStackTrace(); }

    }

    public static void onWorldTickEnd(World world) {
        WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
        List<PhysicsWrapperEntity> physicsEntities = manager.getTickablePhysicsEntities();
        // manager.awaitPhysics();

        /*
         * Also moving this off the game tick thread, players need consistency within
         * subspaces.
         */

        // for (PhysicsWrapperEntity wrapper : physicsEntities) {
        // wrapper.wrapping.coordTransform.sendPositionToPlayers();
        // }

        // Remember only to run this from the game tick thread.
        EntityDraggable.tickAddedVelocityForWorld(world);
        for (PhysicsWrapperEntity wrapperEnt : physicsEntities) {
            wrapperEnt.getPhysicsObject().onPostTick();
        }
    }

    public static void runPhysicsIteration(List<PhysicsWrapperEntity> physicsEntities, WorldPhysObjectManager manager) {
        double newPhysSpeed = ValkyrienWarfareMod.physSpeed;
        Vector newGravity = ValkyrienWarfareMod.gravity;

        List<ShipCollisionTask> collisionTasks = new ArrayList<ShipCollisionTask>(physicsEntities.size() * 2);

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            if (!wrapper.firstUpdate) {
                wrapper.getPhysicsObject().physicsProcessor.rawPhysTickPreCol(newPhysSpeed);
                wrapper.getPhysicsObject().physicsProcessor.worldCollision.tickUpdatingTheCollisionCache();
                wrapper.getPhysicsObject().physicsProcessor.worldCollision.splitIntoCollisionTasks(collisionTasks);
            }
        }

        try {
            // TODO: Right here!
            ValkyrienWarfareMod.PHYSICS_THREADS_EXECUTOR.invokeAll(collisionTasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (ShipCollisionTask task : collisionTasks) {
            PhysicsWrapperEntity wrapper = task.getToTask().getParent().getWrapperEntity();
            if (!wrapper.firstUpdate) {
                task.getToTask().processCollisionTask(task);
            }
        }

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            if (!wrapper.firstUpdate) {
                wrapper.getPhysicsObject().physicsProcessor.rawPhysTickPostCol();
            } else {
                wrapper.getPhysicsObject().coordTransform.updateAllTransforms(false, false);
            }
        }

    }

    private static class PhysicsTickThreadTask implements Callable<Void> {

        private final List physicsEntities;
        private final WorldPhysObjectManager manager;

        public PhysicsTickThreadTask(List physicsEntities, WorldPhysObjectManager manager) {
            this.physicsEntities = physicsEntities;
            this.manager = manager;
        }

        @Override
        public Void call() throws Exception {
            // Run PRE-Col
            runPhysicsIteration(physicsEntities, manager);
            return null;
        }

    }

}
