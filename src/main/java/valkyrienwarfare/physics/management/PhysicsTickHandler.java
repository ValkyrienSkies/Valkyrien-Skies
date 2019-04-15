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
import valkyrienwarfare.mod.multithreaded.PhysicsShipTransform;
import valkyrienwarfare.mod.physmanagement.interaction.EntityDraggable;

import java.util.ArrayList;
import java.util.List;

public class PhysicsTickHandler {

    public static void onWorldTickStart(World world) {
        WorldPhysObjectManager manager = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getManagerForWorld(world);

        List<PhysicsWrapperEntity> toUnload = new ArrayList<PhysicsWrapperEntity>(manager.physicsEntitiesToUnload);
        for (PhysicsWrapperEntity wrapper : toUnload) {
            manager.onUnload(wrapper);
        }

        List<PhysicsWrapperEntity> physicsEntities = manager.getTickablePhysicsEntities();

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            wrapper.getPhysicsObject().getShipTransformationManager().updatePrevTickTransform();

            if (wrapper.getPhysicsObject().getShipTransformationManager().getCurrentPhysicsTransform() instanceof PhysicsShipTransform) {
                // Here we poll our transforms from the physics tick, and apply the latest one
                // to the game tick.
                // This is (for the most part) the only place in the code that bridges the
                // physics tick with the game tick, and so all of the game tick code that
                // depends on ship movement should go right here! Will possibly be moved to the
                // end of the game tick instead.
                PhysicsShipTransform physTransform = (PhysicsShipTransform) wrapper.getPhysicsObject().getShipTransformationManager()
                        .getCurrentPhysicsTransform();

                wrapper.physicsUpdateLastTickPositions();
                wrapper.setPhysicsEntityPositionAndRotation(physTransform.getPosX(), physTransform.getPosY(), physTransform.getPosZ(), physTransform.getPitch(), physTransform.getYaw(), physTransform.getRoll());
                wrapper.getPhysicsObject().getShipTransformationManager().updateAllTransforms(true, true);
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
        WorldPhysObjectManager manager = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getManagerForWorld(world);
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

}
