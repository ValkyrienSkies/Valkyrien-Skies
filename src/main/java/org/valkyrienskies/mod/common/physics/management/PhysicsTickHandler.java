/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mod.common.physics.management;

import java.util.List;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.multithreaded.PhysicsShipTransform;
import org.valkyrienskies.mod.common.physmanagement.interaction.EntityDraggable;

public class PhysicsTickHandler {

    public static void onWorldTickStart(World world) {
        WorldPhysObjectManager manager = ValkyrienSkiesMod.VS_PHYSICS_MANAGER
            .getManagerForWorld(world);

        List<PhysicsWrapperEntity> physicsEntities = manager.getTickablePhysicsEntities();

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            if (wrapper.getPhysicsObject().shipTransformationManager()
                .getCurrentPhysicsTransform() instanceof PhysicsShipTransform) {
                // Here we poll our transforms from the physics tick, and apply the latest one
                // to the game tick.
                // This is (for the most part) the only place in the code that bridges the
                // physics tick with the game tick, and so all of the game tick code that
                // depends on ship movement should go right here! Will possibly be moved to the
                // end of the game tick instead.
                PhysicsShipTransform physTransform = (PhysicsShipTransform) wrapper
                    .getPhysicsObject().shipTransformationManager()
                    .getCurrentPhysicsTransform();

                wrapper.physicsUpdateLastTickPositions();
                wrapper.setPhysicsEntityPositionAndRotation(physTransform.getPosX(),
                    physTransform.getPosY(), physTransform.getPosZ(), physTransform.getPitch(),
                    physTransform.getYaw(), physTransform.getRoll());
                wrapper.getPhysicsObject()
                    .shipTransformationManager()
                    .updateAllTransforms(false, true, true);
            }

            wrapper.getPhysicsObject().updateChunkCache();
        }

        /*
         * All moved off the game tick thread, there is simply no other way to fix the
         * physics.
         */

        // PhysicsTickThreadTask physicsThreadTask = new
        // PhysicsTickThreadTask(ValkyrienSkiesMod.physIter, physicsEntities,
        // manager);

        // try { manager.setPhysicsThread(ValkyrienSkiesMod.PHYSICS_THREADS.submit(
        // physicsThreadTask)); } catch (Exception e) { e.printStackTrace(); }

    }

    public static void onWorldTickEnd(World world) {
        WorldPhysObjectManager manager = ValkyrienSkiesMod.VS_PHYSICS_MANAGER
            .getManagerForWorld(world);
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
