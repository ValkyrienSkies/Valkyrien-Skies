package valkyrienwarfare.mod.multithreaded;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
    private final static long MS_PER_TICK = 50;
    private final static long MAX_LOST_TIME = 1000;
    private int positionTickID;
    // Used to give each VW thread a unique name
    private static int threadID = 0;
    private boolean threadRunning;

    public VWThread(World host) {
        super("VW World Thread: " + threadID);
        threadID++;
        this.hostWorld = host;
        this.ships = new ArrayList<PhysicsWrapperEntity>();
        this.positionTickID = 0;
        threadRunning = true;
    }

    @Override
    public void run() {
        // System.out.println("Thread running");
        // Used to make up for any lost time when we tick
        long lostTickTime = 0;
        while (threadRunning) {
            // Limit the tick smoothing to just one second (1000ms), if lostTickTime becomes
            // too large then physics would move too quickly after the lag source was
            // removed.
            if (lostTickTime > MAX_LOST_TIME) {
                lostTickTime %= MAX_LOST_TIME;
            }
            long start = System.currentTimeMillis();
            runGameLoop();
            try {
                long sleepTime = start + MS_PER_TICK - System.currentTimeMillis();
                // Sending a negative number would cause a crash.
                if (sleepTime > 0) {
                    if (sleepTime > lostTickTime) {
                        sleepTime -= lostTickTime;
                        lostTickTime = 0;
                        sleep(sleepTime);
                    } else {
                        lostTickTime -= sleepTime;
                    }
                } else {
                    // We were late in processing this tick, add it to the lost tick time.
                    lostTickTime -= sleepTime;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void runGameLoop() {
        MinecraftServer mcServer = hostWorld.getMinecraftServer();
        if (mcServer.isServerRunning()) {
            if (mcServer.isDedicatedServer()) {
                // Always tick the physics
                physicsTick();
            } else {
                // Only tick the physics if the game isn't paused
                if (!isSinglePlayerPaused()) {
                    physicsTick();
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private boolean isSinglePlayerPaused() {
        return Minecraft.getMinecraft().isGamePaused();
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
                try {
                    wrapper.wrapping.physicsProcessor.rawPhysTickPostCol();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else {
                wrapper.wrapping.coordTransform.updateAllTransforms(false);
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
        System.out.println("VW Physics Thread Killed");
        threadRunning = false;
        stop();
    }
}
