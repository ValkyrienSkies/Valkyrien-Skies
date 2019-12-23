package org.valkyrienskies.mod.common.physics.management;

import net.minecraft.world.World;

public class PhysicsTickHandler {

    public static void onWorldTickStart(World world) {
        /*
        WorldPhysObjectManager manager = ValkyrienSkiesMod.VS_PHYSICS_MANAGER
            .getManagerForWorld(world);

        List<PhysicsWrapperEntity> physicsEntities = manager.getTickablePhysicsEntities();

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            if (wrapper.getPhysicsObject().getShipTransformationManager()
                .getCurrentPhysicsTransform() instanceof PhysicsShipTransform) {
                // Here we poll our transforms from the physics tick, and apply the latest one
                // to the game tick.
                // This is (for the most part) the only place in the code that bridges the
                // physics tick with the game tick, and so all of the game tick code that
                // depends on ship movement should go right here! Will possibly be moved to the
                // end of the game tick instead.
                PhysicsShipTransform physTransform = (PhysicsShipTransform) wrapper
                    .getPhysicsObject().getShipTransformationManager()
                    .getCurrentPhysicsTransform();

                wrapper.physicsUpdateLastTickPositions();
                wrapper.setPhysicsEntityPositionAndRotation(physTransform.getPosX(),
                    physTransform.getPosY(), physTransform.getPosZ(), physTransform.getPitch(),
                    physTransform.getYaw(), physTransform.getRoll());
                wrapper.getPhysicsObject()
                    .getShipTransformationManager()
                        .updateAllTransforms(false, true);
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
        /*
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
        /*
        EntityDraggable.tickAddedVelocityForWorld(world);
        for (PhysicsWrapperEntity wrapperEnt : physicsEntities) {
            wrapperEnt.getPhysicsObject().onPostTick();
        }
        
         */
    }

}
