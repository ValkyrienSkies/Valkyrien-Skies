package valkyrienwarfare.mod.multithreaded;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.collision.optimization.ShipCollisionTask;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.physics.management.WorldPhysObjectManager;

public class VWThread extends Thread {

    private final World hostWorld;
    // The ships we will be ticking physics for every tick, and sending those
    // updates to players.
    private final List<PhysicsWrapperEntity> ships;
    private final long MS_PER_TICK = 50;
    private int positionTickID;

    public VWThread(World host) {
        super("VW World Thread: " + host.getProviderName());
        hostWorld = host;
        ships = new ArrayList<PhysicsWrapperEntity>();
        positionTickID = 0;
    }

    @Override
    public void run() {
        // System.out.println("Thread running");
        while (true) {
            long start = System.currentTimeMillis();
            runGameLoop();
            try {
                long sleepTime = start + MS_PER_TICK - System.currentTimeMillis();
                // Sending a negative number would cause a crash.
                if (sleepTime > 0) {
                    sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void runGameLoop() {
        if (hostWorld.getMinecraftServer().isServerRunning() && !Minecraft.getMinecraft().isGamePaused()) {
            physicsTick();
        }
    }

    // The whole time need to be careful the game thread isn't messing with these
    // values
    private void physicsTick() {
        // System.out.println("Physics ticking");
        // First tick the physics
        // physics.tick();
        // Then tick the collision
        // collision.tick();
        // Finally update the position/rotation
        // ships.updateTransforms();

        // And then send an update to all the players
        // ships.sendToPlayers();

        // TODO: Temporary fix:
        WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(hostWorld);
        List<PhysicsWrapperEntity> physicsEntities = manager.getTickablePhysicsEntities();
        ships.clear();
        ships.addAll(physicsEntities);
        // System.out.println(ships.size());
        for (int i = 0; i < ValkyrienWarfareMod.physIter; i++) {
            tickThePhysicsAndCollision();
            tickTheTransformUpdates();
        }
        tickSendUpdatesToPlayers();
    }

    private void tickThePhysicsAndCollision() {
        double newPhysSpeed = ValkyrienWarfareMod.physSpeed;
        Vector newGravity = ValkyrienWarfareMod.gravity;
        int iters = ValkyrienWarfareMod.physIter;

        List<ShipCollisionTask> collisionTasks = new ArrayList<ShipCollisionTask>(ships.size() * 2);

        for (PhysicsWrapperEntity wrapper : ships) {
            if (!wrapper.firstUpdate) {
                // Update the physics simulation
                wrapper.wrapping.physicsProcessor.rawPhysTickPreCol(newPhysSpeed, iters);
                // Update the collision task if necessary
                wrapper.wrapping.physicsProcessor.worldCollision.tickUpdatingTheCollisionCache();
                // Take the big collision and split into tiny ones
                wrapper.wrapping.physicsProcessor.worldCollision.splitIntoCollisionTasks(collisionTasks);
            }
        }

        try {
            // The individual collision tasks will sort through a lot of data to find
            // collision points
            ValkyrienWarfareMod.PHYSICS_THREADS_EXECUTOR.invokeAll(collisionTasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Then those collision points have to be processed in series afterwards, all in
        // this thread.
        for (ShipCollisionTask task : collisionTasks) {
            PhysicsWrapperEntity wrapper = task.getToTask().getParent().wrapper;
            if (!wrapper.firstUpdate) {
                task.getToTask().processCollisionTask(task);
            }
        }
    }

    // TODO: Try to synchronize this better with the main game thread, otherwise we
    // could end up with instability.
    private void tickTheTransformUpdates() {
        for (PhysicsWrapperEntity wrapper : ships) {
            if (!wrapper.firstUpdate) {
                wrapper.wrapping.physicsProcessor.rawPhysTickPostCol();
            } else {
                wrapper.wrapping.coordTransform.updateAllTransforms();
            }
        }
    }

    private void tickSendUpdatesToPlayers() {
        for (PhysicsWrapperEntity wrapper : ships) {
            wrapper.wrapping.coordTransform.sendPositionToPlayers(positionTickID);
        }
        positionTickID++;
    }

    public void kill() {
        System.out.println("VW Thread Killed");
        stop();
        ships.clear();
        this.destroy();
    }
}
